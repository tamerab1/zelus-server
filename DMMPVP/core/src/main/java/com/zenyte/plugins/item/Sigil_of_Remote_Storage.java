package com.zenyte.plugins.item;

import com.zenyte.game.model.item.pluginextensions.ItemPlugin;
import com.zenyte.game.world.entity.masks.Graphics;
import com.zenyte.game.world.entity.player.dialogue.Dialogue;

public class Sigil_of_Remote_Storage extends ItemPlugin {
    private static final Graphics attune = new Graphics(1989);
    private static final Graphics unattune = new Graphics(1972);

    @Override
    public void handle() {
        bind("Attune", (player, item, slotId) -> {
            if (item.getId() == 26141) {
                if (player.getInventory().containsItem(26140, 1)) {
                    player.sendMessage("You already have an attuned sigil of this type.");
                    return;
                }

                long remainingTime = player.getSigilRemainingTime("sigil_remote_storage");
                boolean hasBeenAttuned = player.isSigilAttuned(26141);

                if (!hasBeenAttuned) {

                    if (remainingTime > 0 && remainingTime < 3000) {
                        player.sendMessage("The sigil has too little time remaining to be attuned. Recharge it first.");
                        return;
                    }
                    player.setSigilAttuned(26141, true);
                    player.startSigilTimerForFirstTime("sigil_remote_storage");
                    player.sendMessage("The sigil has been attuned for the first time! 6 hours granted.");
                    player.getDialogueManager().start(new Dialogue(player) {
                        @Override
                        public void buildDialogue() {
                            plain("<col=FF0040>Attuned Sigil of Remote Storage!</col><br>The sigil has been attuned!");
                            player.getInventory().deleteItem(slotId, item);
                            player.getInventory().addItem(26140, 1);
                            String timeLeft = player.getFormattedRemainingTime("sigil_remote_storage");
                            player.sendMessage("Time remaining: " + timeLeft);
                            player.setGraphics(attune);
                        }
                    });
                } else {
                    if (remainingTime > 0 && remainingTime < 3000) {
                        player.sendMessage("The sigil has too little time remaining to be attuned. Recharge it first.");
                        return;
                    }
                    player.startSigilTimerForRecharge("sigil_remote_storage", 0L);
                    player.sendMessage("The sigil has been attuned again.");
                    player.getDialogueManager().start(new Dialogue(player) {
                        @Override
                        public void buildDialogue() {
                            plain("<col=FF0040>Attuned Sigil of Remote Storage!</col><br>The sigil has been attuned!");
                            player.getInventory().deleteItem(slotId, item);
                            player.getInventory().addItem(26140, 1); // Give the player the attuned sigil
                            String timeLeft = player.getFormattedRemainingTime("sigil_remote_storage");
                            player.sendMessage("Time remaining: " + timeLeft);
                            player.setGraphics(attune);
                        }
                    });
                }
            }
        });
        bind("Unattune", (player, item, slotId) -> {
            if (item.getId() == 26140) {
                long remainingTime = player.getSigilRemainingTime("sigil_remote_storage");
                if (remainingTime == 0) {
                    player.sendMessage("The sigil has run out of time! Please use 10,000 Blood Money to recharge it.");
                }
                player.stopSigilTimer("sigil_remote_storage");
                player.getDialogueManager().start(new Dialogue(player) {
                    @Override
                    public void buildDialogue() {
                        plain("<col=FF0040>Unattuned Sigil of Remote Storage!</col>");
                        player.getInventory().deleteItem(item);
                        player.getInventory().addItem(26141, 1);
                        player.setGraphics(unattune);

                    }
                });
            }
        });
        bind("Inspect", (player, item, slotId) -> {
            if (item.getId() == 26140 || item.getId() == 26141) {
                String remainingTimeMessage = player.getFormattedRemainingTime("sigil_remote_storage");
                player.sendMessage("The Sigil of Remote Storage has " + remainingTimeMessage);
            } else {
                player.sendMessage("This sigil is not recognized.");
            }
        });
    }

    @Override
    public int[] getItems() {
        return new int[]{26141, 26140};
    }
}
