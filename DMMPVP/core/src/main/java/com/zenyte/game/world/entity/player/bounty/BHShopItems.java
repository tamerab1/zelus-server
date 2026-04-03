package com.zenyte.game.world.entity.player.bounty;

import com.zenyte.game.item.Item;

/**
 * Alle items die in de BH shop beschikbaar zijn.
 */
public enum BHShopItems {

    // Weapons
    W0(BHCategory.WEAPON, 14484, 5000),   // Dragon claws, 5000 punten
    W1(BHCategory.WEAPON, 4151, 2000),    // Whip, 2000 punten

    // Armour
    A0(BHCategory.ARMOUR, 11832, 3000),   // Bandos chestplate
    A1(BHCategory.ARMOUR, 11834, 3000),   // Bandos tassets

    // Scrolls
    S0(BHCategory.SCROLLS, 12846, 1000);  // Scroll of imbuing

    public final BHCategory category;
    public final int id;
    public final int cost;

    BHShopItems(BHCategory category, int id, int cost) {
        this.category = category;
        this.id = id;
        this.cost = cost;
    }

    public Item asItem() {
        return new Item(id, 1);
    }
}
