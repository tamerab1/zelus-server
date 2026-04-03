package com.zenyte.plugins.renewednpc;

import com.zenyte.game.item.Item;
import com.zenyte.game.model.shop.Shop;
import com.zenyte.game.world.entity.npc.actions.NPCPlugin;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.game.world.entity.player.container.impl.Inventory;
import com.zenyte.game.world.entity.player.dialogue.Dialogue;
import com.zenyte.plugins.dialogue.PlainChat;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

/**
 * @author Andys1814
 */
public final class StarDialogue extends NPCPlugin {

    private static final int GOLDEN_NUGGET = 12012;
    private static final int REQUIRED_NUGGETS = 150;

    // Golden Prospector Armour Set items
    private static final Item[] GOLDEN_PROSPECTOR_SET = {
            new Item(25549), // Golden Prospector Helmet
            new Item(25551), // Golden Prospector Jacket
            new Item(25553), // Golden Prospector Legs
            new Item(25555)  // Golden Prospector Boots
    };

    @Override
    public void handle() {
        bind("Talk-to", (player, npc) -> {
            player.getDialogueManager().start(new Dialogue(player, npc) {
                @Override
                public void buildDialogue() {
                    npc("Hello there, miner!");
                    npc("I've got some great rewards for those who work hard mining golden nuggets.");

                    final ObjectArrayList<DialogueOption> optionList = new ObjectArrayList<>();
                    optionList.add(new DialogueOption("Open shop.", () -> Shop.get("Dusuri's Star Store", player.isIronman(), player).open(player)));
                    optionList.add(new DialogueOption("I got golden nuggets!", () -> checkNuggets(player)));
                    optionList.add(new DialogueOption("Never mind.", key(10)));

                    options("Select an Option", optionList.toArray(new DialogueOption[0]));
                    player(10, "Never mind.");
                }
            });
        });
    }

    private void checkNuggets(@NotNull final Player player) {
        player.getDialogueManager().finish();
        player.getDialogueManager().start(new Dialogue(player) {
            @Override
            public void buildDialogue() {
                npc("If you bring me " + REQUIRED_NUGGETS + " golden nuggets, I'll trade them for a full Golden Prospector Armour Set.");
                options("Trade 150 golden nuggets for the set?",
                        new DialogueOption("Yes, trade!", () -> tradeNuggets(player)),
                        new DialogueOption("No, not right now."));
            }
        });
    }

    private void tradeNuggets(@NotNull final Player player) {
        final Inventory inventory = player.getInventory();
        if (inventory.containsItem(GOLDEN_NUGGET, REQUIRED_NUGGETS)) {
            inventory.deleteItem(GOLDEN_NUGGET, REQUIRED_NUGGETS);
            inventory.addOrDrop(GOLDEN_PROSPECTOR_SET);
            player.getDialogueManager().start(new PlainChat(player, "Here is your Golden Prospector Armour Set! Enjoy your mining adventures."));
        } else {
            player.getDialogueManager().start(new PlainChat(player, "You don't have enough golden nuggets. Come back when you have " + REQUIRED_NUGGETS + "."));
        }
    }

    @Override
    public int[] getNPCs() {
        return new int[]{10630};
    }
}