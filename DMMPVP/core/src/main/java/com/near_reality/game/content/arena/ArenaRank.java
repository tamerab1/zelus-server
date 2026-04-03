package com.near_reality.game.content.arena;

/**
 * Defines the ranked tiers for the 1v1 Arena system.
 * Elo thresholds are inclusive lower-bounds; players start at Bronze (1000 Elo).
 */
public enum ArenaRank {

    BRONZE    ("Bronze",     0,    1149, "cd7f32"),
    SILVER    ("Silver",  1150,    1299, "c0c0c0"),
    GOLD      ("Gold",    1300,    1449, "ffd700"),
    PLATINUM  ("Platinum",1450,    1599, "00e5ff"),
    DIAMOND   ("Diamond", 1600,    1799, "b9f2ff"),
    CHALLENGER("Challenger",1800, Integer.MAX_VALUE, "ff4500");

    private final String displayName;
    private final int minElo;
    private final int maxElo;
    /** Hex colour used in broadcasts. */
    private final String colour;

    ArenaRank(String displayName, int minElo, int maxElo, String colour) {
        this.displayName = displayName;
        this.minElo      = minElo;
        this.maxElo      = maxElo;
        this.colour      = colour;
    }

    public String getDisplayName() { return displayName; }
    public int    getMinElo()      { return minElo; }
    public int    getMaxElo()      { return maxElo; }
    public String getColour()      { return colour; }

    /**
     * Returns the rank that corresponds to the supplied Elo rating.
     */
    public static ArenaRank forElo(int elo) {
        for (ArenaRank rank : values()) {
            if (elo >= rank.minElo && elo <= rank.maxElo) {
                return rank;
            }
        }
        return BRONZE;
    }
}
