package com.zenyte.game.content.CTF;

import com.zenyte.game.item.Item;
import com.zenyte.game.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class Flagloottable {

    public static List<Item> generateLoot() {
        List<Item> loot = new ArrayList<>();
        loot.add(new Item(13307, Utils.random(500, 2500))); // Coins

        if (Utils.random(100) < 5) { // 5% kans op zeldzaam item
            loot.add(new Item(32203)); // Dragon boots
        }

        return loot;
    }
}