package com.zenyte.plugins.renewednpc;

import com.zenyte.game.GameInterface;
import com.zenyte.game.content.globalshop.GlobalShopInterface;
import com.zenyte.game.item.Item;
import com.zenyte.game.model.shop.Shop;
import com.zenyte.game.world.entity.npc.actions.NPCPlugin;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.game.world.entity.player.container.impl.Inventory;
import com.zenyte.game.world.entity.player.dialogue.Dialogue;
import com.zenyte.plugins.dialogue.PlainChat;
import com.near_reality.game.content.bountyhunter.BountyHunterController;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import mgi.types.config.items.ItemDefinitions;
import mgi.utilities.StringFormatUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Kris | 07/05/2019 19:42
 * @author Andys1814 | 08/04/2022
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
public final class EdgevilleEmblemTrader extends NPCPlugin {

    public static boolean enabled = true;

    private static final int BLOOD_MONEY = 13307;

    @Override
    public void handle() {
        bind("Exchange emblems", (player, npc) -> {
            if (!enabled) {
                player.sendMessage("Emblem trading is currently disabled.");
                return;
            }
            BountyHunterController.processEmblemExchange(player);
        });
        bind("BH Shop", (player, npc) -> {
            GameInterface.GLOBAL_SHOP.open(player);
        });

        bind("Talk-to", (player, npc) -> {
            if (!enabled) {
                player.sendMessage("Emblem trading is currently disabled.");
                return;
            }


            player.getDialogueManager().start(new Dialogue(player, npc) {
                @Override
                public void buildDialogue() {
                    npc("Hello, wanderer.");
                    npc("Don't suppose you've come across any strange... emblems or artifacts along your journey?");
                    if (hasEmblem(player)) {
                        player("Yes, yes I have.");
                        npc("Would you like to sell them all to me for " + StringFormatUtil.format(getEmblemsReward(player)) + " Blood money?");
                        options("Sell all mysterious emblems?", new DialogueOption("Sell all emblems.", () -> sellEmblems(player)), new DialogueOption("No, keep the emblems."));
                        return;
                    }
                    player("Not that I've seen.");
                    npc("If you do, please do let me know. I'll reward you handsomely.");
                    final ObjectArrayList<Dialogue.DialogueOption> optionList = new ObjectArrayList<>();
                    optionList.add(new DialogueOption("What rewards have you got?", () -> Shop.get("PVP SHOP", player.isIronman(), player).open(player)));
                    if (player.getVariables().isSkulled()) {
                        optionList.add(new DialogueOption("Can you make my PK skull last longer?", () -> promptSkull(player)));
                    } else {
                        optionList.add(new DialogueOption("Can I have a PK skull, please?", () -> promptSkull(player)));
                    }
                    optionList.add(new DialogueOption("That's nice.", key(25)));
                    options(TITLE, optionList.toArray(new DialogueOption[0]));
                    player(25, "That's nice.");
                }
            });
        });
        bind("Rewards", (player, npc) -> {
            GameInterface.GLOBAL_SHOP.open(player);
        });
    }

    public static void promptSkull(@NotNull final Player player) {
        player.getDialogueManager().finish();
        player.getDialogueManager().start(new Dialogue(player) {
            @Override
            public void buildDialogue() {
                options("Obtain a PK skull?", new DialogueOption("Yes, skull me.", () -> player.getVariables().setSkull(true)), new DialogueOption("No, don't skull me."));
            }
        });
    }

    private boolean hasEmblem(@NotNull final Player player) {
        final Inventory inventory = player.getInventory();
        for (int i = 0; i < 28; i++) {
            final Item item = inventory.getItem(i);
            if (item == null) {
                continue;
            }
            int id = item.getId();
            if ((id >= 12746 && id <= 12756 && id != 12747) ||
                    id == 21807 || id == 21810 || id == 21813 || id == 22299 || id == 22302 || id == 22305) {
                return true;
            }
        }
        return false;
    }

    private int getEmblemsReward(@NotNull final Player player) {
        final Inventory inventory = player.getInventory();
        int total = 0;
        for (int i = 0; i < 28; i++) {
            final Item item = inventory.getItem(i);
            if (item == null) {
                continue;
            }
            int id = item.getId();
            if ((id >= 12746 && id <= 12756 && id != 12747) ||
                    id == 21807 || id == 21810 || id == 21813 || id == 22299 || id == 22302 || id == 22305) {
                total += item.getAmount() * Emblem.getReward(id);
            }
        }
        return total;
    }

    private void sellEmblems(@NotNull final Player player) {
        final Inventory inventory = player.getInventory();
        int total = 0;
        for (int i = 0; i < 28; i++) {
            final Item item = inventory.getItem(i);
            if (item == null) {
                continue;
            }
            int id = item.getId();
            if ((id >= 12746 && id <= 12756 && id != 12747) ||
                    id == 21807 || id == 21810 || id == 21813 || id == 22299 || id == 22302 || id == 22305) {
                final int count = inventory.deleteItem(item).getSucceededAmount();
                total += count * Emblem.getReward(id);
            }
        }
        player.getInventory().addOrDrop(BLOOD_MONEY, total);
        player.getDialogueManager().start(new PlainChat(player, "The Emblem Trader awards you with " + total + " Blood Money in exchange for your emblem(s)."));
    }

    @Override
    public int[] getNPCs() {
        return new int[]{308};
    }


    private enum Emblem {
        TIER_ONE(12746, 15),
        TIER_TWO(12748, 20),
        TIER_THREE(12749, 30),
        TIER_FOUR(12750, 45),
        TIER_FIVE(12751, 60),
        TIER_SIX(12752, 100),
        TIER_SEVEN(12753, 130),
        TIER_EIGHT(12754, 180),
        TIER_NINE(12755, 250),
        TIER_TEN(12756, 350),
        NEW_EMBLEM_ONE(21807, 150),  // Voorbeeld beloning
        NEW_EMBLEM_TWO(21810, 250),
        NEW_EMBLEM_THREE(21813, 500),
        NEW_EMBLEM_FOUR(22299, 1000),
        NEW_EMBLEM_FIVE(22302, 1500),
        NEW_EMBLEM_SIX(22305, 2500);

        private static final Emblem[] values = values();
        private final int id;
        private final int bloodMoneyReward;

        private static int getReward(final int item) {
            final int unnoted = ItemDefinitions.getOrThrow(item).getUnnotedOrDefault();
            for (final EdgevilleEmblemTrader.Emblem value : values) {
                if (value.id == unnoted) {
                    return value.bloodMoneyReward;
                }
            }
            throw new IllegalStateException();
        }

        Emblem(int id, int bloodMoneyReward) {
            this.id = id;
            this.bloodMoneyReward = bloodMoneyReward;
        }
    }
}