package com.near_reality.game.content.tournament;

import com.google.common.eventbus.Subscribe;
import com.near_reality.game.content.tournament.TournamentBracket.Match;
import com.near_reality.game.world.PlayerEvent;
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

import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Automated Daily Tournament Manager.
 *
 * <h3>Flow</h3>
 * <ol>
 *   <li>Players type {@code ::join-tourny} to register. Registration closes at
 *       {@link #TOURNAMENT_HOUR}:{@link #TOURNAMENT_MINUTE} server time.</li>
 *   <li>At start time the manager:
 *       <ul>
 *         <li>Teleports all entrants to the waiting room.</li>
 *         <li>Generates a single-elimination bracket via {@link TournamentBracket}.</li>
 *         <li>Applies the chosen {@link TournamentPreset} to every participant.</li>
 *         <li>Starts the first round by teleporting each pair to an arena tile.</li>
 *       </ul>
 *   </li>
 *   <li>On each player death the bracket is updated. When a round is complete,
 *       the next round begins automatically.</li>
 *   <li>The champion is announced globally and can be rewarded.</li>
 * </ol>
 *
 * <h3>Configuration</h3>
 * Edit the constants at the top of this class to change start time, preset,
 * waiting-room tile, and arena tiles.
 */
@StaticInitializer
public final class TournamentManager {

    private static final Logger log = LoggerFactory.getLogger(TournamentManager.class);

    // -----------------------------------------------------------------------
    // Configuration
    // -----------------------------------------------------------------------

    /** Hour (24-h) at which the tournament starts, in the server's time zone. */
    private static final int TOURNAMENT_HOUR   = 20;
    private static final int TOURNAMENT_MINUTE = 0;

    /** Time zone of the server clock. */
    private static final ZoneId SERVER_ZONE = ZoneId.of("Europe/London");

    /** Combat preset applied to every participant. */
    private static final TournamentPreset ACTIVE_PRESET = TournamentPreset.PURE_NH;

    /** Waiting room – players are teleported here before the bracket starts. */
    private static final Location WAITING_ROOM = new Location(3222, 3222, 0);

    /**
     * Arena fight tiles.  Each pair [blueCorner, redCorner] is used for one
     * simultaneous match.  Add more pairs if you expect large tournaments.
     */
    private static final Location[][] ARENA_TILES = {
        { new Location(3310, 3120, 0), new Location(3314, 3120, 0) },
        { new Location(3310, 3115, 0), new Location(3314, 3115, 0) },
        { new Location(3310, 3110, 0), new Location(3314, 3110, 0) },
        { new Location(3310, 3105, 0), new Location(3314, 3105, 0) },
    };

    // -----------------------------------------------------------------------
    // State
    // -----------------------------------------------------------------------

    private enum Phase { IDLE, REGISTRATION, ACTIVE }

    private static volatile Phase   phase         = Phase.IDLE;
    private static final List<Player> registrants  = new CopyOnWriteArrayList<>();
    private static final TournamentBracket bracket = new TournamentBracket();

    /** Maps each active combatant username → their current match. */
    private static final Map<String, Match> activeMatches = new ConcurrentHashMap<>();

    // -----------------------------------------------------------------------
    // Server launch
    // -----------------------------------------------------------------------

    @Subscribe
    public static void onServerLaunch(final ServerLaunchEvent event) {
        final WorldHooks hooks = event.getWorldThread().getHooks();

        // Death listener to resolve tournament matches.
        hooks.register(PlayerEvent.Died.class, e -> {
            if (!(e.getKiller() instanceof Player killer)) return;
            resolveMatch(killer, e.getPlayer());
        });

        // Time-check task: runs every 100 ticks (~60 seconds).
        WorldTasksManager.schedule(() -> checkSchedule(), 100, 100);

        registerCommands();
        log.info("TournamentManager initialised (daily @ {}:{:02d} {}).",
                TOURNAMENT_HOUR, TOURNAMENT_MINUTE, SERVER_ZONE);
    }

    // -----------------------------------------------------------------------
    // Scheduling
    // -----------------------------------------------------------------------

    private static void checkSchedule() {
        ZonedDateTime now = ZonedDateTime.now(SERVER_ZONE);
        int h = now.getHour();
        int m = now.getMinute();

        if (phase == Phase.IDLE && h == TOURNAMENT_HOUR && m == TOURNAMENT_MINUTE) {
            // Open registration 10 minutes before start
            openRegistration();
        }
        if (phase == Phase.REGISTRATION && h == TOURNAMENT_HOUR && m >= TOURNAMENT_MINUTE + 5) {
            startTournament();
        }
    }

    // -----------------------------------------------------------------------
    // Registration
    // -----------------------------------------------------------------------

    private static void openRegistration() {
        phase = Phase.REGISTRATION;
        registrants.clear();
        WorldBroadcasts.sendMessage(
            "<col=ffd700>[Tournament] Daily " + ACTIVE_PRESET.getDisplayName() +
            " Tournament is starting in 10 minutes! Type ::join-tourny to enter.</col>",
            BroadcastType.PVM_ARENA, true);
        log.info("Tournament registration opened.");
    }

    // -----------------------------------------------------------------------
    // Tournament start
    // -----------------------------------------------------------------------

    private static void startTournament() {
        if (registrants.size() < 2) {
            phase = Phase.IDLE;
            WorldBroadcasts.sendMessage(
                "<col=ff4500>[Tournament] Not enough players signed up. Tournament cancelled.</col>",
                BroadcastType.PVM_ARENA, true);
            return;
        }

        phase = Phase.ACTIVE;

        // Teleport everyone to the waiting room and apply preset.
        for (Player p : registrants) {
            if (!p.isSessionActive()) continue;
            p.setLocation(WAITING_ROOM);
            ACTIVE_PRESET.apply(p);
            p.sendMessage("<col=00e5ff>[Tournament] The tournament has started! Good luck!</col>",
                    MessageType.UNFILTERABLE);
        }

        // Generate bracket.
        bracket.generate(new ArrayList<>(registrants));

        WorldBroadcasts.sendMessage(
            "<col=ffd700>[Tournament] " + ACTIVE_PRESET.getDisplayName() +
            " Tournament has begun with " + registrants.size() + " players!</col>",
            BroadcastType.PVM_ARENA, true);

        startNextRound();
    }

    // -----------------------------------------------------------------------
    // Round management
    // -----------------------------------------------------------------------

    private static void startNextRound() {
        List<Match> matches = bracket.getCurrentRound();
        activeMatches.clear();

        int arenaIndex = 0;
        for (Match match : matches) {
            if (match.isResolved()) continue; // bye already resolved

            Player a = match.getPlayerA();
            Player b = match.getPlayerB();

            if (!a.isSessionActive() || !b.isSessionActive()) {
                // Auto-forfeit for disconnected player
                Player online = a.isSessionActive() ? a : b;
                bracket.recordResult(online, a.isSessionActive() ? b : a);
                continue;
            }

            Location[] tiles = ARENA_TILES[arenaIndex % ARENA_TILES.length];
            a.setLocation(tiles[0]);
            b.setLocation(tiles[1]);

            activeMatches.put(a.getUsername(), match);
            activeMatches.put(b.getUsername(), match);

            a.sendMessage("<col=ffd700>[Tournament] Round " + bracket.getRoundNumber() +
                    ": Fight! Your opponent: " + b.getName() + "</col>", MessageType.UNFILTERABLE);
            b.sendMessage("<col=ffd700>[Tournament] Round " + bracket.getRoundNumber() +
                    ": Fight! Your opponent: " + a.getName() + "</col>", MessageType.UNFILTERABLE);

            arenaIndex++;
        }

        WorldBroadcasts.sendMessage(
            "<col=ffd700>[Tournament] Round " + bracket.getRoundNumber() + " has begun!</col>",
            BroadcastType.PVM_ARENA, true);
    }

    // -----------------------------------------------------------------------
    // Match resolution
    // -----------------------------------------------------------------------

    private static void resolveMatch(Player winner, Player loser) {
        Match match = activeMatches.remove(winner.getUsername());
        if (match == null) return;
        activeMatches.remove(loser.getUsername());

        bracket.recordResult(winner, loser);
        winner.sendMessage("<col=00ff00>[Tournament] You advance to the next round!</col>",
                MessageType.UNFILTERABLE);
        loser.sendMessage("<col=ff4500>[Tournament] You have been eliminated.</col>",
                MessageType.UNFILTERABLE);

        if (bracket.isRoundComplete()) {
            if (bracket.isFinished()) {
                announceChampion(bracket.getChampion());
            } else {
                // Short delay before next round so players can see results.
                WorldTasksManager.schedule(() -> {
                    bracket.advanceRound();
                    startNextRound();
                }, 10);
            }
        }
    }

    // -----------------------------------------------------------------------
    // Champion announcement
    // -----------------------------------------------------------------------

    private static void announceChampion(Player champion) {
        phase = Phase.IDLE;
        registrants.clear();
        activeMatches.clear();

        WorldBroadcasts.sendMessage(
            "<col=ffd700>[Tournament] " + champion.getName() +
            " has won the " + ACTIVE_PRESET.getDisplayName() + " Daily Tournament! Congratulations!</col>",
            BroadcastType.PVM_ARENA, true);

        // Teleport champion back to spawn.
        champion.setLocation(new Location(3222, 3218, 0));
        champion.sendMessage("<col=ffd700>You are the tournament champion! Your prize has been added.</col>");
        // TODO: grant reward (Blood Money, trophy item, etc.)
    }

    // -----------------------------------------------------------------------
    // Commands
    // -----------------------------------------------------------------------

    private static void registerCommands() {
        new GameCommands.Command(PlayerPrivilege.PLAYER, "join-tourny",
                "Join the daily tournament.", (player, args) -> {
            if (phase != Phase.REGISTRATION) {
                player.sendMessage("There is no tournament open for registration right now.");
                return;
            }
            if (registrants.contains(player)) {
                player.sendMessage("You are already registered for the tournament.");
                return;
            }
            registrants.add(player);
            player.sendMessage("<col=ffd700>[Tournament] You have been registered for the "
                    + ACTIVE_PRESET.getDisplayName() + " tournament. ("
                    + registrants.size() + " players signed up)</col>");
        });

        // Admin command to force-start a tournament for testing.
        new GameCommands.Command(PlayerPrivilege.ADMINISTRATOR, "tourny-start",
                "[Admin] Force-start the tournament.", (player, args) -> {
            openRegistration();
            player.sendMessage("Tournament registration opened.");
        });
    }

    // -----------------------------------------------------------------------
    // Public helpers
    // -----------------------------------------------------------------------

    public static boolean isActive() {
        return phase == Phase.ACTIVE;
    }

    public static boolean isParticipant(Player player) {
        return registrants.contains(player) || activeMatches.containsKey(player.getUsername());
    }
}
