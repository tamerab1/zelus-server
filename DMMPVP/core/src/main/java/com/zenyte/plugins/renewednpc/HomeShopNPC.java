package com.zenyte.plugins.renewednpc;

import com.near_reality.api.service.vote.VotePlayerAttributesKt;
import com.near_reality.game.item.CustomItemId;
import com.zenyte.game.GameInterface;
import com.zenyte.game.item.Item;
import com.zenyte.game.item.ItemId;
import com.zenyte.game.util.Colour;
import com.zenyte.game.world.entity.npc.NPC;
import com.zenyte.game.world.entity.npc.NpcId;
import com.zenyte.game.world.entity.npc.actions.NPCPlugin;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.game.world.entity.player.container.impl.Inventory;
import com.zenyte.game.world.entity.player.dialogue.impl.NPCChat;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Tommeh | 16-12-2018 | 20:58
 * @see <a href="https://www.rune-server.ee/members/tommeh/">Rune-Server profile</a>}
 */
public class HomeShopNPC extends NPCPlugin {

    private static final Map<Integer, String> SHOPS = new HashMap<>() {

        {
            put(NpcId.ARNAS, "Melee Armoury Shop");
            put(NpcId.TRISTAN, "Melee Store");
            put(NpcId.FAE, "Ranged Store");
            put(NpcId.BABA_YAGA, "Magic Store");
            put(NpcId.JOHN_16007, "Consumables Store");
            put(NpcId.HERQUIN, "Tools Store");
        }
    };

    @Override
    public void handle() {
        bind("Trade", new OptionHandler() {

            @Override
            public void handle(Player player, NPC npc) {
                player.stopAll();
                player.faceEntity(npc);
                if (npc.getId() == NpcId.TRISTAN && player.getNumericAttribute("demon_kills").intValue() == 100) {
                    player.openShop("Melee Store<Alternative>");
                } else {
                    player.openShop(SHOPS.get(npc.getId()));
                }
            }

            @Override
            public void execute(final Player player, final NPC npc) {
                player.stopAll();
//                player.setFaceEntity(npc);
                handle(player, npc);
//                npc.setInteractingWith(player);
            }
        });

        bind("Shops", new OptionHandler() {

            @Override
            public void handle(final Player player, final NPC npc) {
                GameInterface.GLOBAL_SHOP.open(player);
                player.sendMessage("You currently have " + Colour.RED.wrap(VotePlayerAttributesKt.getTotalVoteCredits(player)) + " vote points.");
            }

            @Override
            public void execute(final Player player, final NPC npc) {
                player.stopAll();
                player.setFaceEntity(npc);
                handle(player, npc);
            }
        });
        bind("Loyalty shop", new OptionHandler() {

            @Override
            public void handle(final Player player, final NPC npc) {
                player.openShop("Loyalty Shop");
                player.sendMessage("You currently have " + Colour.RED.wrap(player.getLoyaltyManager().getLoyaltyPoints()) + " loyalty points.");
            }

            @Override
            public void execute(final Player player, final NPC npc) {
                player.stopAll();
                player.setFaceEntity(npc);
                handle(player, npc);
            }
        });
        bind("Donator Shop", new OptionHandler() {

            @Override
            public void handle(final Player player, final NPC npc) {
                player.openShop("Donator Shop");
                player.sendMessage("You currently have " + Colour.RED.wrap(player.getDonorPoints()) + " donator points.");
            }

            @Override
            public void execute(final Player player, final NPC npc) {
                player.stopAll();
                player.setFaceEntity(npc);
                handle(player, npc);
            }
        });
        bind("Buy-teletabs", (player, npc) -> {
            final int purchases = player.getVariables().getTeletabPurchases();
            if (purchases >= 1000) {
                player.getDialogueManager().start(new NPCChat(player, npc.getId(), "You've already purchased the " +
                        "daily maximum of 1,000 teletabs from me!"));
                return;
            }
            final Inventory inventory = player.getInventory();
            if (!inventory.hasFreeSlots() && !inventory.containsItem(CustomItemId.NR_TABLET, 1)) {
                player.getDialogueManager().start(new NPCChat(player, npc.getId(), "You need some free inventory space to purchase these!"));
                return;
            }
            final int price = 175;
            final int coins = inventory.getAmountOf(ItemId.COINS_995);
            final int maxAmountPurchaseable = coins / price;
            final int count = inventory.getAmountOf(CustomItemId.NR_TABLET);
            final int maxAvailablePurchase = Math.min(Math.min(maxAmountPurchaseable, 1000 - purchases), Integer.MAX_VALUE - count);
            if (maxAvailablePurchase <= 0) {
                player.getDialogueManager().start(new NPCChat(player, npc.getId(), "You can't afford any teletabs!"));
                return;
            }
            player.sendInputInt("How many would you like to purchase for " + price + " each? (0-" + maxAvailablePurchase + ")", value -> {
                final int currentCoins = inventory.getAmountOf(ItemId.COINS_995);
                final int currentMaxAmountPurchaseable = currentCoins / price;
                final int currentCount = inventory.getAmountOf(CustomItemId.NR_TABLET);
                final int currentMaxAvailablePurchase = Math.min(Math.min(currentMaxAmountPurchaseable, 1000 - purchases), Integer.MAX_VALUE - currentCount);
                final int purchaseQuantity = Math.min(value, currentMaxAvailablePurchase);
                if (purchaseQuantity <= 0) {
                    return;
                }
                player.getVariables().setTeletabPurchases(purchases + purchaseQuantity);
                inventory.deleteItem(new Item(ItemId.COINS_995, purchaseQuantity * price));
                inventory.addOrDrop(new Item(CustomItemId.NR_TABLET, purchaseQuantity));
                player.getDialogueManager().start(new NPCChat(player, npc.getId(), "Pleasure doing business with you!"));
            });
        });
        bind("Jewellery", (player, npc) -> player.openShop("Jewellery Store"));
    }

    @Override
    public int[] getNPCs() {
        return new int[] { NpcId.ARNAS, NpcId.ROBIN_HOOD, NpcId.FAE, NpcId.JOHN_16007, NpcId.JACKIE, NpcId.FRANK, NpcId.TRISTAN, NpcId.HERQUIN, NpcId.BABA_YAGA };
    }
}
