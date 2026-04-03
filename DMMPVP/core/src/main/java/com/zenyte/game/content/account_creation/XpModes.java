package com.zenyte.game.content.account_creation;

public enum XpModes {

    EASY(0, 180, 70, 0.0F),
    MODERATE(1, 50, 20, 2.0F),
    HARDCORE(2, 20, 10, 5.0F),
    EXTREME(3, 5, 3, 8.0F);

    private final int index;
    private final int combatRate;
    private final int skillingRate;
    private final float dropBoost;

    XpModes(int index, int combatRate, int skillingRate, float dropBoost) {
        this.index = index;
        this.combatRate = combatRate;
        this.skillingRate = skillingRate;
        this.dropBoost = dropBoost;
    }

    public int getIndex() {
        return index;
    }

    public int getCombatRate() {
        return combatRate;
    }

    public int getSkillingRate() {
        return skillingRate;
    }

    public float getDropBoost() {
        return dropBoost;
    }
}
