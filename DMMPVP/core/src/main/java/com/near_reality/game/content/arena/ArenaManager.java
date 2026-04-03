package com.near_reality.game.content.arena;

import com.google.common.eventbus.Subscribe;
import com.near_reality.game.world.PlayerEvent;
import com.near_reality.game.world.WorldEventListener;
import com.near_reality.game.world.WorldHooks;
import com.zenyte.game.task.WorldTasksManager;
import com.zenyte.game.world.World;
import com.zenyte.game.world.broadcasts.BroadcastType;
import com.zenyte.game.world.broadcasts.WorldBroadcasts;
import com.zenyte.game.world.entity.Location;
import com.zenyte.game.world.entity.player.GameCommands;
import com.zenyte.game.world.entity.player.MessageType;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.game.world.entity.player.privilege.PlayerPrivilege;
import com.zenyte.plugins.events.ServerLaunchEvent;
import com.zenyte.utils.StaticInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Manages the 1v1 ranked PvP arena (matchmaking, Elo, brackets).
 *
 * <h3>How it works</h3>
 * <ol>
 *   <li>Players type {@code ::arena} to join the matchmaking queue.</li>
 *   <li>Every 10 ticks the manager pairs up queued players with similar Elo.</li>
 *   <li>Each matched pair is teleported to the arena, and the fight begins.</li>
 *   <li>On death the {@link PlayerEvent.Died} hook fires; Elo is updated and
 *       persisted to the {@code arena_rankings} SQL table.</li>
 * </ol>
 *
 * <h3>SQL – run once on the database</h3>
 * <pre>{@code
 * CREATE TABLE IF NOT EXISTS arena_rankings (
 *     username        VARCHAR(12)  NOT NULL PRIMARY KEY,
 *     elo             INT          NOT NULL DEFAULT 1000,
 *     wins            INT          NOT NULL DEFAULT 0,
 *     losses          INT          NOT NULL DEFAULT 0,
 *     rank_name       VARCHAR(16)  NOT NULL DEFAULT 'Bronze',
 *     last_updated    TIMESTAMP    NOT NULL DEFAULT NOW()
 * );
 *
 * -- Index used by the React leaderboard endpoint (ORDER BY elo DESC)
 * CREATE INDEX IF NOT EXISTS idx_arena_elo ON arena_rankings (elo DESC);
 * }</pre>
 */
@StaticInitializer
public final class ArenaManager {

    // -----------------------------------------------------------------------
    // Constants
    // -----------------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(ArenaManager.class);

    /** Starting Elo for every new player. */
    public static final int DEFAULT_ELO = 1000;

    /** K-factor: controls how many points change hands per match. */
    private static final int K_FACTOR = 32;

    /** Maximum Elo gap allowed between two queued players before they are
     *  forcibly paired anyway (prevents long waits at extreme ratings). */
    private static final int MAX_ELO_GAP_FOR_FORCE_MATCH = 300;

    /** Ticks between each matchmaking scan (~6 seconds at 600 ms/tick). */
    private static final int MATCHMAKING_INTERVAL_TICKS = 10;

    /** Arena waiting-room tile (adjust to a free spot in your map). */
    private static final Location ARENA_SPAWN_BLUE = new Location(3367, 3167, 0);
    private static final Location ARENA_SPAWN_RED  = new Location(3370, 3167, 0);

    // -----------------------------------------------------------------------
    // State
    // -----------------------------------------------------------------------

    /** Players waiting for a match, in insertion order. */
    private static final Deque<Player> queue = new ConcurrentLinkedDeque<>();

    /**
     * Active matches: maps each combatant's username → their opponent's username.
     * Both directions are stored so look-ups are O(1).
     */
    private static final Map<String, String> activeMatches = new ConcurrentHashMap<>();

    // -----------------------------------------------------------------------
    // Startup & event wiring
    // -----------------------------------------------------------------------

    @Subscribe
    public static void onServerLaunch(final ServerLaunchEvent event) {
        final WorldHooks hooks = event.getWorldThread().getHooks();

        // Listen for player deaths to resolve arena matches.
        final WorldEventListener<PlayerEvent.Died> onDeath = deathEvent -> {
            final Player victim = deathEvent.getPlayer();
            if (!(deathEvent.getKiller() instanceof Player killer)) {
                return;
            }
            resolveMatch(killer, victim);
        };
        hooks.register(PlayerEvent.Died.class, onDeath);

        // Periodic matchmaking scan.
        WorldTasksManager.schedule(() -> {
            if (queue.size() >= 2) {
                runMatchmaking();
            }
        }, MATCHMAKING_INTERVAL_TICKS, MATCHMAKING_INTERVAL_TICKS);

        registerCommands();
        log.info("ArenaManager initialised.");
    }

    // -----------------------------------------------------------------------
    // Command registration
    // -----------------------------------------------------------------------

    private static void registerCommands() {
        // ::arena – join or leave the matchmaking queue
        new GameCommands.Command(PlayerPrivilege.PLAYER, "arena",
                "Join/leave the 1v1 ranked arena queue.", (player, args) -> {
            if (isInMatch(player)) {
                player.sendMessage("You are currently in an arena match. Finish your fight first.");
                return;
            }
            if (queue.contains(player)) {
                queue.remove(player);
                player.sendMessage("<col=ff4500>You have left the arena queue.</col>");
            } else {
                queue.add(player);
                player.sendMessage("<col=00e5ff>You have joined the arena queue. " +
                        "Position: " + queue.size() + ". Current Elo: " + getElo(player) + ".</col>");
            }
        });

        // ::arenarank – display your current rank & Elo
        new GameCommands.Command(PlayerPrivilege.PLAYER, "arenarank",
                "Show your current arena rank and Elo.", (player, args) -> {
            int elo  = getElo(player);
            ArenaRank rank = ArenaRank.forElo(elo);
            player.sendMessage("<col=" + rank.getColour() + ">Arena Rank: " + rank.getDisplayName()
                    + " | Elo: " + elo + "</col>");
        });
    }

    // -----------------------------------------------------------------------
    // Matchmaking
    // -----------------------------------------------------------------------

    /**
     * Pairs the two players in the queue whose Elo ratings are closest.
     * Falls back to FIFO pairing when all players are within a comfortable gap.
     */
    private static void runMatchmaking() {
        // Simple approach: iterate queue pairs, find the smallest Elo gap.
        Player[] waiting = queue.toArray(new Player[0]);
        int bestGap   = Integer.MAX_VALUE;
        int bestI     = -1;
        int bestJ     = -1;

        for (int i = 0; i < waiting.length; i++) {
            if (!waiting[i].isSessionActive()) { queue.remove(waiting[i]); continue; }
            for (int j = i + 1; j < waiting.length; j++) {
                if (!waiting[j].isSessionActive()) { queue.remove(waiting[j]); continue; }
                int gap = Math.abs(getElo(waiting[i]) - getElo(waiting[j]));
                if (gap < bestGap) {
                    bestGap = gap;
                    bestI   = i;
                    bestJ   = j;
                }
            }
        }

        if (bestI < 0 || bestJ < 0) return;

        // Only force-match when the best gap is acceptable or the queue is large.
        if (bestGap > MAX_ELO_GAP_FOR_FORCE_MATCH && queue.size() < 4) return;

        Player p1 = waiting[bestI];
        Player p2 = waiting[bestJ];
        queue.remove(p1);
        queue.remove(p2);

        startMatch(p1, p2);
    }

    /**
     * Teleports both players to the arena and registers their active match.
     */
    private static void startMatch(Player p1, Player p2) {
        activeMatches.put(p1.getUsername(), p2.getUsername());
        activeMatches.put(p2.getUsername(), p1.getUsername());

        p1.setLocation(ARENA_SPAWN_BLUE);
        p2.setLocation(ARENA_SPAWN_RED);

        String announce = "<col=00e5ff>[Arena]</col> Match started: <col=ffffff>"
                + p1.getName() + "</col> (Elo " + getElo(p1) + ") vs <col=ffffff>"
                + p2.getName() + "</col> (Elo " + getElo(p2) + ")";

        p1.sendMessage(announce, MessageType.UNFILTERABLE);
        p2.sendMessage(announce, MessageType.UNFILTERABLE);
    }

    // -----------------------------------------------------------------------
    // Match resolution & Elo update
    // -----------------------------------------------------------------------

    /**
     * Called when a death occurs. Checks if this was an arena match and, if so,
     * adjusts Elo for both players and persists the result.
     */
    private static void resolveMatch(Player winner, Player loser) {
        if (!activeMatches.containsKey(winner.getUsername())) return;
        final String expectedOpponent = activeMatches.get(winner.getUsername());
        if (!expectedOpponent.equalsIgnoreCase(loser.getUsername())) return;

        activeMatches.remove(winner.getUsername());
        activeMatches.remove(loser.getUsername());

        int winnerElo = getElo(winner);
        int loserElo  = getElo(loser);

        int[] newElos = calculateElo(winnerElo, loserElo);
        int newWinnerElo = newElos[0];
        int newLoserElo  = newElos[1];

        int winnerDelta = newWinnerElo - winnerElo;
        int loserDelta  = newLoserElo  - loserElo;

        ArenaRank winnerRank = ArenaRank.forElo(newWinnerElo);
        ArenaRank loserRank  = ArenaRank.forElo(newLoserElo);

        winner.sendMessage("<col=" + winnerRank.getColour() + ">[Arena] You won! Elo: "
                + winnerElo + " → " + newWinnerElo + " (+" + winnerDelta + ")  Rank: "
                + winnerRank.getDisplayName() + "</col>");

        loser.sendMessage("<col=ff4500>[Arena] You lost. Elo: "
                + loserElo + " → " + newLoserElo + " (" + loserDelta + ")  Rank: "
                + loserRank.getDisplayName() + "</col>");

        persistElo(winner.getUsername(), newWinnerElo, true);
        persistElo(loser.getUsername(),  newLoserElo,  false);

        // Broadcast rank-up
        if (winnerRank.ordinal() > ArenaRank.forElo(winnerElo).ordinal()) {
            WorldBroadcasts.sendMessage(
                    "<col=" + winnerRank.getColour() + ">[Arena] " + winner.getName()
                            + " has reached " + winnerRank.getDisplayName() + " rank!</col>",
                    BroadcastType.PVM_ARENA, false);
        }
    }

    // -----------------------------------------------------------------------
    // Elo formula  (standard FIDE)
    // -----------------------------------------------------------------------

    /**
     * Calculates new Elo ratings after a match.
     *
     * @param winnerElo the winner's current Elo
     * @param loserElo  the loser's current Elo
     * @return int[]{newWinnerElo, newLoserElo}
     */
    public static int[] calculateElo(int winnerElo, int loserElo) {
        // Expected score for winner: E_w = 1 / (1 + 10^((loserElo - winnerElo) / 400))
        double expectedWin  = 1.0 / (1.0 + Math.pow(10.0, (loserElo  - winnerElo)  / 400.0));
        double expectedLose = 1.0 / (1.0 + Math.pow(10.0, (winnerElo - loserElo) / 400.0));

        int newWinner = (int) Math.round(winnerElo + K_FACTOR * (1.0 - expectedWin));
        int newLoser  = (int) Math.round(loserElo  + K_FACTOR * (0.0 - expectedLose));

        // Floor at 100 so no player can ever fall below that.
        newLoser = Math.max(newLoser, 100);

        return new int[]{ newWinner, newLoser };
    }

    // -----------------------------------------------------------------------
    // Elo persistence (PostgreSQL via GameDatabase HikariCP pool)
    // -----------------------------------------------------------------------

    /**
     * Upserts the player's Elo into the {@code arena_rankings} table.
     * Runs on a background thread to avoid blocking the game loop.
     */
    private static void persistElo(String username, int elo, boolean won) {
        final ArenaRank rank = ArenaRank.forElo(elo);
        Thread.ofVirtual().start(() -> {
            try (Connection conn = getConnection()) {
                if (conn == null) return;
                final String sql =
                    "INSERT INTO arena_rankings (username, elo, wins, losses, rank_name, last_updated) " +
                    "VALUES (?, ?, ?, ?, ?, NOW()) " +
                    "ON CONFLICT (username) DO UPDATE SET " +
                    "  elo          = EXCLUDED.elo, " +
                    "  wins         = arena_rankings.wins   + EXCLUDED.wins, " +
                    "  losses       = arena_rankings.losses + EXCLUDED.losses, " +
                    "  rank_name    = EXCLUDED.rank_name, " +
                    "  last_updated = NOW()";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, username);
                    ps.setInt(2, elo);
                    ps.setInt(3, won ? 1 : 0);
                    ps.setInt(4, won ? 0 : 1);
                    ps.setString(5, rank.getDisplayName());
                    ps.executeUpdate();
                }
            } catch (SQLException e) {
                log.error("Failed to persist Elo for {}", username, e);
            }
        });
    }

    /**
     * Fetches the player's current Elo from the database.
     * Returns {@link #DEFAULT_ELO} if no record exists yet.
     */
    public static int getEloFromDb(String username) {
        try (Connection conn = getConnection()) {
            if (conn == null) return DEFAULT_ELO;
            final String sql = "SELECT elo FROM arena_rankings WHERE username = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getInt("elo");
                }
            }
        } catch (SQLException e) {
            log.error("Failed to fetch Elo for {}", username, e);
        }
        return DEFAULT_ELO;
    }

    /**
     * Convenience method – reads Elo from the player's saved numeric attribute
     * so the DB is not hit every command call. Falls back to DB on first load.
     */
    public static int getElo(Player player) {
        int cached = player.getNumericAttribute("arena_elo").intValue();
        if (cached == 0) {
            cached = getEloFromDb(player.getUsername());
            player.getTemporaryAttributes().put("arena_elo", cached);
        }
        return cached;
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    public static boolean isInMatch(Player player) {
        return activeMatches.containsKey(player.getUsername());
    }

    /**
     * Returns a JDBC {@link Connection} sourced from the existing HikariCP main
     * pool via {@link com.near_reality.api.dao.Db}.
     * Replace the hard-coded fallback URL with your server's connection string.
     */
    private static Connection getConnection() {
        try {
            // Update these credentials to match your world profile / database config.
            return DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/nearreality", "game_user", "changeme");
        } catch (SQLException e) {
            log.error("JDBC connection failed for arena_rankings", e);
            return null;
        }
    }
}
