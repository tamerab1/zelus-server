package com.zenyte.plugins.renewednpc;

import com.zenyte.game.world.entity.npc.NpcId;
import com.zenyte.game.world.entity.npc.actions.NPCPlugin;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.game.world.entity.player.dialogue.Dialogue;
import com.zenyte.game.item.Item;

/**
 * @author Kris
 */
public class Quartermaster extends NPCPlugin {

    private static final int BLOOD_MONEY_ID = 13307;
    private static final int COINS_ID = 995;
    private static final int CONVERSION_RATE = 3500; // 1 Blood Money = 3500 Coins

    @Override
    public void handle() {
        bind("Talk-to", (player, npc) -> player.getDialogueManager().start(new Dialogue(player, npc) {
            @Override
            public void buildDialogue() {
                npc("Hello there, adventurer. I can exchange Blood Money for coins, or vice versa. What would you like to do?");

                options(TITLE,
                        new DialogueOption("Change Blood Money for Coins.", key(5)),
                        new DialogueOption("Change Coins for Blood Money.", key(10)),
                        new DialogueOption("Nevermind.", key(50)));

                // BLOOD MONEY → COINS
                npc(5, "For every 1 Blood Money, you will receive 3,500 coins.");
                options(TITLE,
                        new DialogueOption("Yes, exchange my Blood Money.", () -> exchangeBloodMoneyForCoins(player)),
                        new DialogueOption("No, thanks."));

                // COINS → BLOOD MONEY
                npc(10, "For every 3,500 coins, you will receive 1 Blood Money.");
                options(TITLE,
                        new DialogueOption("Yes, exchange my Coins.", () -> exchangeCoinsForBloodMoney(player)),
                        new DialogueOption("No, thanks."));

                npc(50, "Alright, let me know if you change your mind.");
            }
        }));
    }

    @Override
    public int[] getNPCs() {
        return new int[]{NpcId.QUARTERMASTER}; // Toegevoegd NPC ID 3220
    }

    private void exchangeBloodMoneyForCoins(Player player) {
        if (!player.getInventory().containsItem(BLOOD_MONEY_ID)) {
            player.sendMessage("You don't have any Blood Money to exchange.");
            return;
        }

        int bloodMoneyAmount = player.getInventory().getAmountOf(BLOOD_MONEY_ID);
        long totalCoinsToGive = (long) bloodMoneyAmount * CONVERSION_RATE;
        int currentCoins = player.getInventory().getAmountOf(COINS_ID);
        int maxCoinsCanHold = Integer.MAX_VALUE - currentCoins;
        int coinsToGive = (totalCoinsToGive > maxCoinsCanHold) ? maxCoinsCanHold : (int) totalCoinsToGive;
        long remainingCoins = totalCoinsToGive - coinsToGive;
        int platinumTokensToGive = (int) (remainingCoins / 1000);

        int spaceNeeded = 2;
        if (player.getInventory().containsItem(COINS_ID)) spaceNeeded--;
        if (player.getInventory().containsItem(13204)) spaceNeeded--;

        player.getInventory().deleteItem(new Item(BLOOD_MONEY_ID, bloodMoneyAmount));

        if (player.getInventory().getFreeSlots() >= spaceNeeded) {
            if (coinsToGive > 0) player.getInventory().addItem(new Item(COINS_ID, coinsToGive));
            if (platinumTokensToGive > 0) player.getInventory().addItem(new Item(13204, platinumTokensToGive));
        } else {
            if (coinsToGive > 0) player.getBank().add(new Item(COINS_ID, coinsToGive));
            if (platinumTokensToGive > 0) player.getBank().add(new Item(13204, platinumTokensToGive));
            player.sendMessage("Your inventory was full, so the exchanged currency was sent to your bank.");
        }

        String message = "You have exchanged " + bloodMoneyAmount + " Blood Money for ";
        if (coinsToGive > 0) message += coinsToGive + " coins";
        if (platinumTokensToGive > 0) message += (coinsToGive > 0 ? " and " : "") + platinumTokensToGive + " Platinum Tokens";
        player.sendMessage(message + ".");
    }




    private void exchangeCoinsForBloodMoney(Player player) {
        if (!player.getInventory().containsItem(COINS_ID, CONVERSION_RATE)) {
            player.sendMessage("You don't have enough coins to exchange.");
            return;
        }

        int coinAmount = player.getInventory().getAmountOf(COINS_ID);
        int bloodMoneyToGive = coinAmount / CONVERSION_RATE;

        int spaceNeeded = 1;
        if (player.getInventory().containsItem(BLOOD_MONEY_ID)) spaceNeeded--;

        player.getInventory().deleteItem(new Item(COINS_ID, bloodMoneyToGive * CONVERSION_RATE));

        if (player.getInventory().getFreeSlots() >= spaceNeeded) {
            player.getInventory().addItem(new Item(BLOOD_MONEY_ID, bloodMoneyToGive));
        } else {
            player.getBank().add(new Item(BLOOD_MONEY_ID, bloodMoneyToGive));
            player.sendMessage("Your inventory was full, so the Blood Money was sent to your bank.");
        }

        player.sendMessage("You have exchanged " + (bloodMoneyToGive * CONVERSION_RATE) + " coins for " + bloodMoneyToGive + " Blood Money.");
    }

}
