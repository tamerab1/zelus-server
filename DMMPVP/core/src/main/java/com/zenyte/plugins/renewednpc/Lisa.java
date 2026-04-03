package com.zenyte.plugins.renewednpc;

import com.zenyte.game.item.Item;
import com.zenyte.game.world.entity.npc.actions.NPCPlugin;
import com.zenyte.game.world.entity.player.dialogue.Dialogue;
import com.zenyte.game.world.entity.npc.NpcId;

public class Lisa extends NPCPlugin {

    // Definieer de kosten voor elke set
    private static final int MELEE_ARMOUR_SET_COST = 500_000; // 500.000 munten
    private static final int RANGED_ARMOUR_SET_COST = 300_000; // 400.000 munten
    private static final int hybrid_ARMOUR_SET_COST = 500_000; // 450.000 munten

    // Definieer de items voor elke set
    private static final Item[] MELEE_ARMOUR_SET = {
            new Item(1079),   // Rune platelegs
            new Item(1127),   // Rune platebody
            new Item(7462),   // Barrow's gloves
            new Item(8850),   // Rune defender
            new Item(10828),  // Helm of neitiznot
            new Item(3105),   // Climbing boots
            new Item(2550),   // Ring of recoil
            new Item(4587),   // Dragon scimitar
            new Item(6568),   // Obsidian cape
            new Item(12695),  // Placeholder voor super combat pot
            new Item(6585),   // fury
            new Item(3024),   // Super restore potion
            new Item(385, 25),    // Shark
            new Item(5698),   // Dragon dagger
            new Item(24621, 15)   // 15x Vengeance sack
    };

    private static final Item[] RANGED_ARMOUR_SET = {
            new Item(10828),  // Placeholder voor Ranged helm
            new Item(1131),  // Placeholder voor Ranged body
            new Item(2497),  // Placeholder voor Ranged legs
            new Item(7462),  // Placeholder voor Ranged gloves
            new Item(20578),  // Placeholder voor Ranged boots
            new Item(6585),  // Placeholder voor Ranged amulet
            new Item(2550),  // Placeholder voor Ranged ring
            new Item(12788),  // Placeholder voor Ranged weapon
            new Item(10499),  // Placeholder voor Ranged cape
            new Item(21326, 100),  // Placeholder voor Ranged arrows/bolts
            new Item(2444),  // Placeholder voor Ranged potion
            new Item(2436),  // Placeholder voor strenght pot
            new Item(2440),  // Placeholder voor attack pot
            new Item(2442),  // Placeholder voor def pot
            new Item(3024, 2),  // Placeholder voor Ranged potion
            new Item(386, 25),  // Placeholder voor shark
            new Item(4153),   // Placeholder voor secundair wapen
            new Item(24621, 15)   // 15x Vengeance sack
    };

    private static final Item[] hybrid_ARMOUR_SET = {
            new Item(10828),  // Placeholder voor Magic hat
            new Item(4091),  // Placeholder voor Magic robe top
            new Item(4093),  // Placeholder voor Magic robe bottom
            new Item(7462),  // Placeholder voor barrows gloves
            new Item(20578),  // Placeholder voor Magic boots
            new Item(6585),  // Placeholder voor Magic amulet
            new Item(2550),  // Placeholder voor Magic ring
            new Item(2412),  // Placeholder voor Magic cape
            new Item(560, 400),  // death rune
            new Item(565, 200),  // death rune
            new Item(555, 600),  // death rune
            new Item(3040),  // Placeholder voor Magic potion
            new Item(12695),  // Placeholder voor super combat pot
            new Item(3024, 2),  // Placeholder voor super restore
            new Item(386, 25),  // Placeholder voor shark
            new Item(4675),   // Ancient staff
            new Item(9185),   // Rune crossbow
            new Item(9144, 100),   // Rune bolts
            new Item(2503),   // Rune bolts
            new Item(8850),   // Rune def
            new Item(4587),   // dragon scim
            new Item(5698),   // dragon def p++
            new Item(12829),   // spirit shield
            new Item(1079)   // spirit shield

    };

    @Override
    public void handle() {
        bind("Talk-to", (player, npc) -> {
            player.getDialogueManager().start(new Dialogue(player, npc) {

                @Override
                public void buildDialogue() {
                    npc("Hello! How can I assist you today?");
                    options("Select an option",
                            "I would like a melee armor set.",
                            "I would like a Ranged armor set.",
                            "I would like a Magic armor set.",
                            "Nothing, thanks.")
                            .onOptionOne(() -> setKey(5))
                            .onOptionTwo(() -> setKey(15))
                            .onOptionThree(() -> setKey(25))
                            .onOptionFour(() -> setKey(30));

                    // MELEE Armor Set
                    player(5, "I would like a melee armor set.");
                    npc("Are you sure you want the armor for melee? It costs 500,000 coins.");
                    options("Confirm",
                            "Yes, I want to buy the melee armor set.",
                            "No, I've changed my mind.")
                            .onOptionOne(() -> {
                                if (player.getInventory().containsItem(995, MELEE_ARMOUR_SET_COST)) { // 995 is het item-ID voor munten
                                    player.getInventory().deleteItem(995, MELEE_ARMOUR_SET_COST); // Verwijder de munten uit de inventaris
                                    for (Item item : MELEE_ARMOUR_SET) {
                                        player.getInventory().addOrDrop(item);
                                    }
                                    player.sendMessage("Here is your melee armor set. Good luck!");
                                } else {
                                    player.sendMessage("You don't have enough coins to buy the melee armor set.");
                                }
                            })
                            .onOptionTwo(() -> npc("Alright, let me know if you change your mind."));

                    // Ranged melee
                    player(15, "I would like a Ranged melee armor set.");
                    npc("Are you sure you want the Ranged armor set? It costs 300,000 coins.");
                    options("Confirm",
                            "Yes, I want to buy the Ranged melee armor set.",
                            "No, I've changed my mind.")
                            .onOptionOne(() -> {
                                if (player.getInventory().containsItem(995, RANGED_ARMOUR_SET_COST)) {
                                    player.getInventory().deleteItem(995, RANGED_ARMOUR_SET_COST);
                                    for (Item item : RANGED_ARMOUR_SET) {
                                        player.getInventory().addOrDrop(item);
                                    }
                                    player.sendMessage("Here is your Ranged melee armor set. Good luck!");
                                } else {
                                    player.sendMessage("You don't have enough coins to buy the Ranged armor set.");
                                }
                            })
                            .onOptionTwo(() -> npc("Alright, let me know if you change your mind."));

                    // Hybrid
                    player(25, "I would like a Hybrid armor set.");
                    npc("Are you sure you want the hybrid armor set? It costs 500,000 coins.");
                    options("Confirm",
                            "Yes, I want to buy the hybrid armor set.",
                            "No, I've changed my mind.")
                            .onOptionOne(() -> {
                                if (player.getInventory().containsItem(995, hybrid_ARMOUR_SET_COST)) {
                                    player.getInventory().deleteItem(995, hybrid_ARMOUR_SET_COST);
                                    for (Item item : hybrid_ARMOUR_SET) {
                                        player.getInventory().addOrDrop(item);
                                    }
                                    player.sendMessage("Here is your hybrid armor set. Good luck!");
                                } else {
                                    player.sendMessage("You don't have enough coins to buy the Hybrid armor set.");
                                }
                            })
                            .onOptionTwo(() -> npc("Alright, let me know if you change your mind."));

                    player(10, "Nothing, thanks.");
                    npc("Alright, have a great day!");
                }
            });
        });
    }

    @Override
    public int[] getNPCs() {
        return new int[]{NpcId.LISA}; // Replace 'NpcId.LISA' with the correct NPC ID
    }
}