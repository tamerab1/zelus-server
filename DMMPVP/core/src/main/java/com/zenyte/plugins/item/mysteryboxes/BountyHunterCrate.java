package com.zenyte.plugins.item.mysteryboxes;

import com.zenyte.game.GameInterface;
import com.zenyte.game.item.Item;
import com.zenyte.game.model.item.pluginextensions.ItemPlugin;
import com.zenyte.game.util.Utils;
import com.zenyte.game.world.World;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.game.world.entity.player.MessageType;


import java.util.*;

public class BountyHunterCrate extends ItemPlugin {
    private static final Random rand = new Random();
    private static final Map<Integer, List<MysteryItem>> rewards = new HashMap<>();

    // Static initializer → gevuld zodra de class geladen wordt
    static {
        rewards.put(1, List.of(
                new MysteryItem(13307, 2500, 2500, 2500) // 2500 blood money
        ));
        rewards.put(2, List.of(
                new MysteryItem(13307, 5000, 5000, 2500)
        ));
        rewards.put(3, List.of(
                new MysteryItem(13307, 10000, 10000, 2500)
        ));
        rewards.put(4, List.of(
                new MysteryItem(13307, 15000, 15000, 2500)
        ));
        rewards.put(5, List.of(
                new MysteryItem(13307, 20000, 20000, 2500)
        ));
        rewards.put(6, List.of(
                new MysteryItem(13307, 25000, 25000, 2500)
        ));
        rewards.put(7, List.of(
                new MysteryItem(13307, 30000, 30000, 2500)
        ));
        rewards.put(8, List.of(
                new MysteryItem(13307, 35000, 35000, 2500)
        ));
        rewards.put(9, List.of(
                new MysteryItem(13307, 40000, 40000, 2500)
        ));
    }

    @Override
    public void handle() {
        // bestaande open optie
        bind("Open", (player, item, container, slotId) -> openBox(player, item.getId()));

        // nieuwe examine optie
        bind("Rewards", (player, item, container, slotId) -> {
            int tier = ((item.getId() - 28082) / 2) + 1;
            openCrateInfoInterface(player, tier);
        });
    }

    private void openCrateInfoInterface(Player player, int tier) {
        player.getInterfaceHandler().sendInterface(GameInterface.BHREWARDS);

    }



    @Override
    public int[] getItems() {
        // alle crate itemIds → 28082, 28084 … 28098
        return new int[]{28082, 28084, 28086, 28088, 28090, 28092, 28094, 28096, 28098};
    }


    public static void openBox(Player player, int itemId) {
        int tier = ((itemId - 28082) / 2) + 1;

        Item box = new Item(itemId, 1);
        if (!player.getInventory().deleteItem(box).isFailure()) {

            List<ChanceItem> chanceItems = new ArrayList<>();
            int bloodMin = 0, bloodMax = 0;

            switch (tier) {
                case 1 -> {
                    bloodMin = 800; bloodMax = 1500;
                    chanceItems = List.of(
                            new ChanceItem(24565, 15), // emblem t1
                            new ChanceItem(6199, 10),  // mystery box
                            new ChanceItem(20595, 8),  // hood
                            new ChanceItem(20520, 6),  // robe
                            new ChanceItem(20517, 6),  // top
                            new ChanceItem(32070, 5)   // donator pin 10
                    );
                }
                case 2 -> {
                    bloodMin = 1500; bloodMax = 3000;
                    chanceItems = List.of(
                            new ChanceItem(24567, 15), // emblem t2
                            new ChanceItem(7498, 10),  // xp lamp / mystery box
                            new ChanceItem(24419, 8),  // helm
                            new ChanceItem(24421, 6),  // robe
                            new ChanceItem(24420, 6),  // top
                            new ChanceItem(32071, 5)   // donator pin 25
                    );
                }
                case 3 -> {
                    bloodMin = 3000; bloodMax = 5000;
                    chanceItems = List.of(
                            new ChanceItem(24569, 15), // emblem t3
                            new ChanceItem(2100, 10), // Twisted Buckler
                            new ChanceItem(22326, 8), // Justiciar Helm
                            new ChanceItem(22328, 6), // Justiciar legs
                            new ChanceItem(22327, 6), // Justiciar chest
                            new ChanceItem(32072, 5)  // Donator pin 50
                    );
                }
                case 4 -> {
                    bloodMin = 5000; bloodMax = 7500;
                    chanceItems = List.of(
                            new ChanceItem(24571, 15), // Emblem t4
                            new ChanceItem(13652, 10), // D claws
                            new ChanceItem(22625, 8), // Statius helm
                            new ChanceItem(22631, 6), // Statius legs
                            new ChanceItem(22628, 6), // Statius chest
                            new ChanceItem(32073, 5)  // Donator pin 100
                    );
                }
                case 5 -> {
                    bloodMin = 7500; bloodMax = 10000;
                    chanceItems = List.of(
                            new ChanceItem(24573, 15), // T5
                            new ChanceItem(32164, 10), // Super mystery box
                            new ChanceItem(32212, 8), // Skilling box
                            new ChanceItem(22616, 6), // VEsta chest
                            new ChanceItem(22619, 6), // Vesta skirt
                            new ChanceItem(22610, 5)  // Vesta spear
                    );
                }
                case 6 -> {
                    bloodMin = 10000; bloodMax = 12500;
                    chanceItems = List.of(
                            new ChanceItem(24575, 15), // T6
                            new ChanceItem(19553, 10), // Torture
                            new ChanceItem(22650, 8), // Zuriel hood
                            new ChanceItem(22656, 6), // Zuriel bottom
                            new ChanceItem(22653, 6), // Zuriel top
                            new ChanceItem(22622, 5)  // Statius warhammer
                    );
                }
                case 7 -> {
                    bloodMin = 12500; bloodMax = 15000;
                    chanceItems = List.of(
                            new ChanceItem(24577, 15), // T7
                            new ChanceItem(21012, 10), // Dragonhunter cross
                            new ChanceItem(21003, 8), // E maul
                            new ChanceItem(19564, 6), // Seed pod
                            new ChanceItem(21015, 6), // Bulwark
                            new ChanceItem(23995, 5)  // Blade of saeldor
                    );
                }
                case 8 -> {
                    bloodMin = 15000; bloodMax = 17500;
                    chanceItems = List.of(
                            new ChanceItem(24579, 15), // T8
                            new ChanceItem(32203, 10), // PVP mystery box
                            new ChanceItem(21018, 8), // Ancestral hat
                            new ChanceItem(21024, 6), // Ancestral bottom
                            new ChanceItem(21021, 6), // Ancestral top
                            new ChanceItem(22324, 5)  // Rapier
                    );
                }
                case 9 -> {
                    bloodMin = 17500; bloodMax = 20000;
                    chanceItems = List.of(
                            new ChanceItem(24581, 15), // T9
                            new ChanceItem(32165, 10), // Ultimate m box
                            new ChanceItem(25985, 8), // Elidinis ward
                            new ChanceItem(21859, 6), // Wise old man santa hat
                            new ChanceItem(28307, 6), // Ultor ring
                            new ChanceItem(27690, 5)  // Voidwaker
                    );
                }
                default -> {
                    bloodMin = 1000; bloodMax = 2000; // fallback
                }
            }

            // 2. Altijd blood money
            Item bloodMoney = new Item(13307, Utils.random(bloodMin, bloodMax));
            player.getInventory().addOrDrop(bloodMoney);

            // 3. 50% kans op extra item
            if (!chanceItems.isEmpty() && Utils.random(1, 100) <= 50) {
                int roll = Utils.random(0, 99);
                int cumulative = 0;
                for (ChanceItem ci : chanceItems) {
                    cumulative += ci.chance;
                    if (roll < cumulative) {
                        Item reward = new Item(ci.id, 1);
                        reward.setAttribute("bh-crate", "tier-" + tier);
                        player.getInventory().addOrDrop(reward);

                        // Broadcast in chatbox (met icoon en shadow)
                        World.sendMessage(MessageType.UNFILTERABLE,
                                "<img=13><shad=000000>" + player.getName() +
                                        " has received <col=ff0000>" + reward.getName() +
                                        "</col> from a Tier " + tier + " Bounty Hunter Crate!</shad>");
                        break;
                    }
                }
            }

            // Eigen message naar speler
            player.sendMessage("You open the Bounty Hunter Crate (Tier " + tier + ") and receive your rewards!");
        }
    }



    // Helper class
    private static class ChanceItem {
        int id;
        int chance;
        ChanceItem(int id, int chance) {
            this.id = id;
            this.chance = chance;
        }
    }


}
