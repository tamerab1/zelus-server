package com.zenyte.plugins.item;

import com.zenyte.game.model.item.pluginextensions.ItemPlugin;
import com.zenyte.game.world.entity.masks.Graphics;
import com.zenyte.game.world.entity.player.dialogue.Dialogue;

public class Sigil_titanium extends ItemPlugin {
    private static final Graphics attune = new Graphics(1993);
    private static final Graphics unattune = new Graphics(1970);

    @Override
    public void handle() {
        bind("Attune", (player, item, slotId) -> {
            if (item.getId() == 28523) {
                if (player.getInventory().containsItem(28522, 1)) { // Check if the player already has the attuned sigil
                    player.sendMessage("You already have an attuned sigil of this type.");
                    return;
                }

                long remainingTime = player.getSigilRemainingTime("sigil_titanium");
                boolean hasBeenAttuned = player.isSigilAttuned(28523); // Check if the sigil has been attuned

                if (!hasBeenAttuned) {
                    if (remainingTime > 0 && remainingTime < 3000) {
                        player.sendMessage("The sigil has too little time remaining to be attuned. Recharge it first.");
                        return;
                    }
                    player.setSigilAttuned(28523, true);
                    player.startSigilTimerForFirstTime("sigil_titanium");
                    player.sendMessage("The sigil has been attuned for the first time! 6 hours granted.");
                    player.getDialogueManager().start(new Dialogue(player) {
                        @Override
                        public void buildDialogue() {
                            plain("<col=FF0040>Attuned Sigil of Ninja!</col><br>The sigil has been attuned!");
                            player.getInventory().deleteItem(slotId, item);
                            player.getInventory().addItem(28522, 1);

                            String timeLeft = player.getFormattedRemainingTime("sigil_titanium");
                            player.sendMessage("Time remaining: " + timeLeft);
                            player.setGraphics(attune);
                        }
                    });
                } else {
                    if (remainingTime > 0 && remainingTime < 3000) {
                        player.sendMessage("The sigil has too little time remaining to be attuned. Recharge it first.");
                        return;
                    }
                    player.startSigilTimerForRecharge("sigil_titanium", 0L);
                    player.sendMessage("The sigil has been attuned again.");
                    player.getDialogueManager().start(new Dialogue(player) {
                        @Override
                        public void buildDialogue() {
                            plain("<col=FF0040>Attuned Sigil of titanium!</col><br>The sigil has been attuned!");
                            player.getInventory().deleteItem(slotId, item);
                            player.getInventory().addItem(28522, 1);
                            String timeLeft = player.getFormattedRemainingTime("sigil_titanium");
                            player.sendMessage("Time remaining: " + timeLeft);
                            player.setGraphics(attune);
                        }
                    });
                }
            }
        });
        bind("Unattune", (player, item, slotId) -> {
                if (item.getId() == 28522) {
                    long remainingTime = player.getSigilRemainingTime("sigil_titanium");
                    if (remainingTime == 0) {
                        player.sendMessage("The sigil has run out of time! Please use 10,000 Blood Money to recharge it.");
                    }
                    player.stopSigilTimer("sigil_titanium");
                    player.getDialogueManager().start(new Dialogue(player) {
                        @Override
                        public void buildDialogue() {
                            plain("<col=FF0040>Unattuned Sigil of titanium!</col>");
                            player.getInventory().deleteItem(item);
                            player.getInventory().addItem(28523, 1);
                            player.setGraphics(unattune);

                        }
                    });
                }
            });
            bind("Inspect", (player, item, slotId) -> {
                if (item.getId() == 28522 || item.getId() == 28523) {
                    String remainingTimeMessage = player.getFormattedRemainingTime("sigil_titanium");
                    player.sendMessage("The Sigil of titanium has " + remainingTimeMessage);
                } else {
                    player.sendMessage("This sigil is not recognized.");
                }
            });
        }
        @Override
        public int[] getItems() {
            return new int[]{28523, 28522};
        }
    }
