package com.zenyte.game.content;

import com.near_reality.game.item.CustomItemId;

public enum DonatorPin {
    DONATOR_PIN_10(CustomItemId.DONATOR_PIN_10, 130, 10),
    DONATOR_PIN_25(CustomItemId.DONATOR_PIN_25, 325, 25),
    DONATOR_PIN_50(CustomItemId.DONATOR_PIN_50, 650, 50),
    DONATOR_PIN_100(CustomItemId.DONATOR_PIN_100, 1300, 100);

    private int itemId;
    private int credits;
    private int amount;

    DonatorPin(int itemId, int credits, int amount) {
        this.itemId = itemId;
        this.credits = credits;
        this.amount = amount;
    }

    public int getItemId() {
        return itemId;
    }

    public int getCredits() {
        return credits;
    }

    public int getAmount() {
        return amount;
    }
    public static DonatorPin forId(int id) {
        for (DonatorPin value : values()) {
            if (value.itemId == id)
                return value;
        }
        return null;
    }
}