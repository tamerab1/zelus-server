package mgi.custom.tourn;

import com.zenyte.game.item.ItemId;

public enum TournamentRewards {

    INFERNAL_CAPE(21295, 1, 500),
    BLOOD_MONEY(ItemId.BLOOD_MONEY, 25_000, 100),
    STAFF_OF_LIGHT(ItemId.STAFF_OF_LIGHT, 1, 50),
    SUPER_MYSTERY_BOX(ItemId.PVP_TOURNEY_MYSTERY_BOX, 1, 40),
    MYSTERY_BOX(ItemId.MYSTERY_BOX, 1, 10),
    IMBUED_SARADOMIN_CAPE(ItemId.IMBUED_SARADOMIN_CAPE, 1, 10),
    IMBUED_ZAMORAK_CAPE(ItemId.IMBUED_ZAMORAK_CAPE, 1, 10),
    IMBUED_GUTHIX_CAPE(ItemId.IMBUED_GUTHIX_CAPE, 1, 10),
    DHAROKS_ARMOUR_SET(ItemId.DHAROKS_ARMOUR_SET, 1, 10),
    VERACS_ARMOUR_SET(ItemId.VERACS_ARMOUR_SET, 1, 10),
    TORAGS_ARMOUR_SET(ItemId.TORAGS_ARMOUR_SET, 1, 10),
    AHRIMS_ARMOUR_SET(ItemId.AHRIMS_ARMOUR_SET, 1, 10),
    KARILS_ARMOUR_SET(ItemId.KARILS_ARMOUR_SET, 1, 10),
    ZULRAHS_SCALES(ItemId.ZULRAHS_SCALES, 100, 1),


    ;



    private final int itemId;
    private final int itemAmount;
    private final int cost;


    TournamentRewards(int itemId, int itemAmount, int cost) {
        this.itemId = itemId;
        this.itemAmount = itemAmount;
        this.cost = cost;
    }

    public int getCost() {
        return cost;
    }

    public int getItemAmount() {
        return itemAmount;
    }

    public int getItemId() {
        return itemId;
    }
}
