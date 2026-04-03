package com.near_reality.game.content.tournament;

import com.zenyte.game.world.entity.player.Player;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/**
 * Generates and manages a single-elimination bracket.
 *
 * <h3>How it works</h3>
 * <ol>
 *   <li>Call {@link #generate(List)} with all registered players.</li>
 *   <li>The list is shuffled and padded to the nearest power-of-two with byes.</li>
 *   <li>Each round, {@link #advanceRound()} is called by {@link TournamentManager}.</li>
 *   <li>After a fight, call {@link #recordResult(Player, Player)} to advance the winner.</li>
 *   <li>When {@link #isFinished()} returns {@code true}, {@link #getChampion()} holds the winner.</li>
 * </ol>
 */
public class TournamentBracket {

    /**
     * A single match between two players.
     * {@code playerB} may be {@code null} for a "bye" (playerA auto-advances).
     */
    public static class Match {
        private final Player playerA;
        private final Player playerB; // null = bye
        private Player winner;

        public Match(Player playerA, Player playerB) {
            this.playerA = playerA;
            this.playerB = playerB;
        }

        public Player getPlayerA() { return playerA; }
        public Player getPlayerB() { return playerB; }
        public Player getWinner()  { return winner; }
        public boolean isBye()     { return playerB == null; }
        public boolean isResolved(){ return winner != null; }

        void resolveBye() {
            if (isBye()) winner = playerA;
        }

        void resolve(Player winner) {
            this.winner = winner;
        }
    }

    // -----------------------------------------------------------------------

    private List<Match> currentRound  = new ArrayList<>();
    private int         roundNumber   = 0;
    private Player      champion      = null;

    // -----------------------------------------------------------------------
    // Construction
    // -----------------------------------------------------------------------

    /**
     * Generates the first-round bracket from {@code participants}.
     * The list is shuffled in-place, then padded to the next power of two
     * by inserting {@code null} bye slots.
     */
    public void generate(List<Player> participants) {
        if (participants.isEmpty()) throw new IllegalArgumentException("No participants.");

        // Shuffle for random seeding
        Collections.shuffle(participants);

        // Pad to power of two
        int size = nextPowerOfTwo(participants.size());
        List<Player> padded = new ArrayList<>(participants);
        while (padded.size() < size) padded.add(null);

        // Build round-1 matches
        currentRound.clear();
        for (int i = 0; i < padded.size(); i += 2) {
            currentRound.add(new Match(padded.get(i), padded.get(i + 1)));
        }

        roundNumber = 1;

        // Auto-resolve any byes
        currentRound.stream()
                     .filter(Match::isBye)
                     .forEach(Match::resolveBye);
    }

    // -----------------------------------------------------------------------
    // Match resolution
    // -----------------------------------------------------------------------

    /**
     * Records the outcome of a fight.
     *
     * @param winner the player who won
     * @param loser  the player who lost (used only to locate the correct match)
     */
    public void recordResult(Player winner, Player loser) {
        for (Match match : currentRound) {
            if (involves(match, winner, loser)) {
                match.resolve(winner);
                return;
            }
        }
    }

    /**
     * Returns {@code true} when all matches in the current round have been resolved.
     */
    public boolean isRoundComplete() {
        return currentRound.stream().allMatch(Match::isResolved);
    }

    /**
     * Advances to the next round by collecting winners from the current round
     * and building new matches.
     *
     * @return the new set of matches, or an empty list if the tournament is over
     */
    public List<Match> advanceRound() {
        if (!isRoundComplete()) return List.of();

        List<Player> winners = new ArrayList<>();
        for (Match m : currentRound) {
            if (m.getWinner() != null) winners.add(m.getWinner());
        }

        if (winners.size() == 1) {
            champion     = winners.get(0);
            currentRound = List.of();
            return List.of();
        }

        currentRound = new ArrayList<>();
        for (int i = 0; i < winners.size(); i += 2) {
            Player a = winners.get(i);
            Player b = (i + 1 < winners.size()) ? winners.get(i + 1) : null;
            Match m  = new Match(a, b);
            if (m.isBye()) m.resolveBye();
            currentRound.add(m);
        }

        roundNumber++;
        return List.copyOf(currentRound);
    }

    // -----------------------------------------------------------------------
    // Accessors
    // -----------------------------------------------------------------------

    public List<Match> getCurrentRound()  { return List.copyOf(currentRound); }
    public int         getRoundNumber()   { return roundNumber; }
    public boolean     isFinished()       { return champion != null; }
    public Player      getChampion()      { return champion; }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static boolean involves(Match match, Player a, Player b) {
        Player pa = match.getPlayerA();
        Player pb = match.getPlayerB();
        return (pa != null && pa.equals(a) && pb != null && pb.equals(b))
            || (pa != null && pa.equals(b) && pb != null && pb.equals(a));
    }

    private static int nextPowerOfTwo(int n) {
        int p = 1;
        while (p < n) p <<= 1;
        return p;
    }
}
