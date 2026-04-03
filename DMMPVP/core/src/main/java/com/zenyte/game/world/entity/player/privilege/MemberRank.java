package com.zenyte.game.world.entity.player.privilege;

import com.zenyte.utils.Ordinal;
import mgi.utilities.StringFormatUtil;

/**
 * MemberRank defines the player's membership rank based on total donations.
 */
@Ordinal
public enum MemberRank implements IPrivilege {

    NONE(0, 0.0, Crown.NONE, "000000", -1, -1, 0),
    PREMIUM(1, 0.01D, Crown.PREMIUM, "007e18", 200, 12, 25),
    EXPANSION(2, 0.02D, Crown.EXPANSION, "F8E239", 100, 10, 50),
    EXTREME(3, 0.04D, Crown.EXTREME, "FFFFFF", 75, 8, 200),
    RESPECTED(4, 0.06D, Crown.RESPECTED, "d80717", 50, 6, 400),
    LEGENDARY(5, 0.08D, Crown.LEGENDARY, "2188FF", 25, 4, 1000),
    MYTHICAL(6, 0.1D, Crown.MYTHICAL, "999999", 8, 2, 2500),
    UBER(7, 0.12D, Crown.UBER, "222222", 0, 0, 5000),
    /**
     * Amascut is a special rank added for `speckle` and `gim`.
     */
    AMASCUT(8, 0.12D, Crown.AMASCUT, "df782f", 0, 0, Integer.MAX_VALUE); // Special rank, no threshold

    public static final MemberRank[] values = values();
    private final int id;
    private final Crown crown;
    private final double dropRate;
    private final String yellColor;
    private final int yellDelay;
    private final int togglesChance;
    private final int requiredDonationAmount;

    MemberRank(int id, double dropRate, Crown crown, String yellColor, int yellDelay, int togglesChance, int requiredDonationAmount) {
        this.id = id;
        this.dropRate = dropRate;
        this.crown = crown;
        this.yellColor = yellColor;
        this.yellDelay = yellDelay;
        this.togglesChance = togglesChance;
        this.requiredDonationAmount = requiredDonationAmount;
    }

    public static MemberRank fromId(int memberRank) {
        return MemberRank.values[memberRank];
    }

    public boolean equalToOrGreaterThan(final MemberRank member) {
        return getId() >= member.getId();
    }

    @Override
    public String toString() {
        return StringFormatUtil.formatString(name().toLowerCase().replace("_", " "));
    }

    public int getId() {
        return id;
    }

    public String getYellColor() {
        return yellColor;
    }

    public int getYellDelay() {
        return yellDelay;
    }

    @Override
    public int priority() {
        return 0;
    }

    @Override
    public Crown crown() {
        return crown;
    }

    public double getDR() {
        return dropRate;
    }

    public int getTogglesChance() {
        return togglesChance;
    }

    public int getRequiredDonationAmount() {
        return requiredDonationAmount;
    }

    /**
     * Determines the rank based on the total donation amount.
     */
    public static MemberRank getRankForDonationAmount(int totalSpent) {
        for (int i = values.length - 1; i >= 0; i--) {
            if (totalSpent >= values[i].getRequiredDonationAmount()) {
                return values[i];
            }
        }
        return NONE; // Default to NONE if no rank matches
    }
}