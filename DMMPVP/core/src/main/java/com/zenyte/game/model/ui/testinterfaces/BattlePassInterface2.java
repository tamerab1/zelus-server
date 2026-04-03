package com.zenyte.game.model.ui.testinterfaces;

import com.zenyte.game.GameInterface;
import com.zenyte.game.model.ui.Interface;
import com.zenyte.game.world.entity.player.Player;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;


public class BattlePassInterface2 extends Interface {

    @Override
    protected void attach() {
        put(10, "Level 1 Display");
        put(42, "Claim Level 1 Reward");
        put(52, "Level 2 Display");         // Nieuw
        put(50, "Claim Level 2 Reward");
        put(60, "Level 3 Display"); // Of een ander vrij component ID als 60 al bezet is
        put(58, "Claim Level 3 Reward");
        put(68, "Level 4 Display");
        put(66, "Claim Level 4 Reward");
        put(76, "Level 5 Display");
        put(74, "Claim Level 5 Reward");
        put(84, "Level 6 Display");
        put(82, "Claim Level 6 Reward");
        put(92, "Level 7 Display");
        put(90, "Claim Level 7 Reward");
        put(100, "Level 8 Display");
        put(98, "Claim Level 8 Reward");
        put(108, "Next Page"); // Rechterpijl
        put(106, "Previous Page"); // (optioneel) Linkerpijl
// Nieuw
    }

    @Override
    protected void build() {
        bind("Next Page", (player, slotId, itemId, option) -> {
            int currentPage = player.getBattlePassPage();
            if (currentPage < 3) {
                player.setBattlePassPage(currentPage + 1);
                open(player); // Refresh interface met nieuwe content
            } else {
                player.sendMessage("You're already on the last page.");
            }
        });

        bind("Previous Page", (player, slotId, itemId, option) -> {
            player.setBattlePassPage(1);
            player.getInterfaceHandler().sendInterface(GameInterface.BATTLEPASS);
        });


        bind("Level 1 Display", (player, slotId, itemId, option) -> {
            // Optioneel gedrag
        });

        bind("Claim Level 1 Reward", (player, slotId, itemId, option) -> {
            if (player.getBattlePassLevel() >= 1) {
                if (!player.getBooleanAttribute("claimedLevel1Reward")) {
                    player.getInventory().addItem(6199, 1); // Mystery Box
                    player.putBooleanAttribute("claimedLevel1Reward", true);
                    player.sendMessage("You've claimed the Level 1 reward!");
                    player.getPacketDispatcher().sendComponentText(5106, 44, "<col=aaaaaa>Level 1 (Claimed)</col>");
                    player.getPacketDispatcher().sendComponentText(5106, 42, "Claimed");
                } else {
                    player.sendMessage("You've already claimed this reward.");
                }
            } else {
                player.sendMessage("You must reach Battle Pass Level 1 to claim this reward.");
            }
        });

        // ✅ Nieuw gedrag voor level 2
        bind("Level 2 Display", (player, slotId, itemId, option) -> {
            // Optioneel gedrag
        });

        bind("Claim Level 2 Reward", (player, slotId, itemId, option) -> {
            if (player.getBattlePassLevel() >= 2) {
                if (!player.getBooleanAttribute("claimedLevel2Reward")) {
                    player.getInventory().addItem(13307, 10000);
                    player.putBooleanAttribute("claimedLevel2Reward", true);
                    player.sendMessage("You've claimed the Level 2 reward!");
                    player.getPacketDispatcher().sendComponentText(5106, 52, "<col=aaaaaa>Level 2 (Claimed)</col>");
                    player.getPacketDispatcher().sendComponentText(5106, 50, "Claimed");
                } else {
                    player.sendMessage("You've already claimed this reward.");
                }
            } else {
                player.sendMessage("You must reach Battle Pass Level 2 to claim this reward.");
            }
        });
        bind("Level 3 Display", (player, slotId, itemId, option) -> {
        });

        bind("Claim Level 3 Reward", (player, slotId, itemId, option) -> {
            if (player.getBattlePassLevel() >= 3) {
                if (!player.getBooleanAttribute("claimedLevel3Reward")) {
                    player.getInventory().addItem(28017, 1); // Reward: 1x item 28017
                    player.putBooleanAttribute("claimedLevel3Reward", true);
                    player.sendMessage("You've claimed the Level 3 reward!");
                    player.getPacketDispatcher().sendComponentText(5106, 60, "<col=aaaaaa>Level 3 (Claimed)</col>");
                    player.getPacketDispatcher().sendComponentText(5106, 58, "Claimed");
                } else {
                    player.sendMessage("You've already claimed this reward.");
                }
            } else {
                player.sendMessage("You must reach Battle Pass Level 3 to claim this reward.");
            }
        });
        bind("Level 4 Display", (player, slotId, itemId, option) -> {
            // Optioneel gedrag bij klikken op de display
        });

        bind("Level 4 Display", (player, slotId, itemId, option) -> {
            // Optioneel gedrag
        });

        bind("Claim Level 4 Reward", (player, slotId, itemId, option) -> {
            if (player.getBattlePassLevel() >= 4) {
                if (!player.getBooleanAttribute("claimedLevel4Reward")) {
                    player.getInventory().addItem(7498, 1); // Item ID: 7498
                    player.putBooleanAttribute("claimedLevel4Reward", true);
                    player.sendMessage("You've claimed the Level 4 reward!");
                    player.getPacketDispatcher().sendComponentText(5106, 68, "<col=aaaaaa>Level 4 (Claimed)</col>");
                    player.getPacketDispatcher().sendComponentText(5106, 66, "Claimed");
                } else {
                    player.sendMessage("You've already claimed this reward.");
                }
            } else {
                player.sendMessage("You must reach Battle Pass Level 4 to claim this reward.");
            }
        });
        bind("Level 5 Display", (player, slotId, itemId, option) -> {
            // Optioneel gedrag bij klikken op de display
        });

        bind("Claim Level 5 Reward", (player, slotId, itemId, option) -> {
            if (player.getBattlePassLevel() >= 5) {
                if (!player.getBooleanAttribute("claimedLevel5Reward")) {
                    player.getInventory().addItem(11235, 1); // Reward: 1x Dark Bow
                    player.putBooleanAttribute("claimedLevel5Reward", true);
                    player.sendMessage("You've claimed the Level 5 reward!");
                    player.getPacketDispatcher().sendComponentText(5106, 76, "<col=aaaaaa>Level 5 (Claimed)</col>");
                    player.getPacketDispatcher().sendComponentText(5106, 74, "Claimed");
                } else {
                    player.sendMessage("You've already claimed this reward.");
                }
            } else {
                player.sendMessage("You must reach Battle Pass Level 5 to claim this reward.");
            }
        });
// Level 6
        bind("Level 6 Display", (player, slotId, itemId, option) -> {
            // Optioneel gedrag
        });
        bind("Claim Level 6 Reward", (player, slotId, itemId, option) -> {
            if (player.getBattlePassLevel() >= 6) {
                if (!player.getBooleanAttribute("claimedLevel6Reward")) {
                    player.getInventory().addItem(11802, 1); // Armadyl Godsword
                    player.putBooleanAttribute("claimedLevel6Reward", true);
                    player.sendMessage("You've claimed the Level 6 reward!");
                    player.getPacketDispatcher().sendComponentText(5106, 84, "<col=aaaaaa>Level 6 (Claimed)</col>");
                    player.getPacketDispatcher().sendComponentText(5106, 82, "Claimed");
                } else {
                    player.sendMessage("You've already claimed this reward.");
                }
            } else {
                player.sendMessage("You must reach Battle Pass Level 6 to claim this reward.");
            }
        });

// Level 7
        bind("Level 7 Display", (player, slotId, itemId, option) -> {
            // Optioneel gedrag
        });
        bind("Claim Level 7 Reward", (player, slotId, itemId, option) -> {
            if (player.getBattlePassLevel() >= 7) {
                if (!player.getBooleanAttribute("claimedLevel7Reward")) {
                    player.getInventory().addItem(13652, 1); // Dragon Claws
                    player.putBooleanAttribute("claimedLevel7Reward", true);
                    player.sendMessage("You've claimed the Level 7 reward!");
                    player.getPacketDispatcher().sendComponentText(5106, 92, "<col=aaaaaa>Level 7 (Claimed)</col>");
                    player.getPacketDispatcher().sendComponentText(5106, 90, "Claimed");
                } else {
                    player.sendMessage("You've already claimed this reward.");
                }
            } else {
                player.sendMessage("You must reach Battle Pass Level 7 to claim this reward.");
            }
        });

// Level 8
        bind("Level 8 Display", (player, slotId, itemId, option) -> {
            // Optioneel gedrag
        });
        bind("Claim Level 8 Reward", (player, slotId, itemId, option) -> {
            if (player.getBattlePassLevel() >= 8) {
                if (!player.getBooleanAttribute("claimedLevel8Reward")) {
                    player.getInventory().addItem(27690, 1); // Volcanic Blade (voorbeeld)
                    player.putBooleanAttribute("claimedLevel8Reward", true);
                    player.sendMessage("You've claimed the Level 8 reward!");
                    player.getPacketDispatcher().sendComponentText(5106, 100, "<col=aaaaaa>Level 8 (Claimed)</col>");
                    player.getPacketDispatcher().sendComponentText(5106, 98, "Claimed");
                } else {
                    player.sendMessage("You've already claimed this reward.");
                }
            } else {
                player.sendMessage("You must reach Battle Pass Level 8 to claim this reward.");
            }
        });




    }

    @Override
    public void open(Player player) {
        int page = player.getBattlePassPage();
        player.getPacketDispatcher().sendClientScript(149, 5106, 46, 6199, 1);
        int interfaceId = 5106;
        player.getInterfaceHandler().sendInterface(getInterface());
        player.getPacketDispatcher().sendComponentText(interfaceId, 25, player.getUsername());
        LocalDate today = LocalDate.now();
        LocalDate endDate = LocalDate.of(2025, 10, 31); // Voorbeeld einddatum
        long daysLeft = ChronoUnit.DAYS.between(today, endDate);
        player.getPacketDispatcher().sendComponentText(5106, 38, "Battle Pass Ends: " + daysLeft + " days");

        int level = player.getBattlePassLevel();
        player.getPacketDispatcher().sendComponentText(interfaceId, 21, String.valueOf(level));

        // ----- LEVEL 1 UI -----
        boolean level1Claimed = player.getBooleanAttribute("claimedLevel1Reward");
        if (level1Claimed) {
            player.getPacketDispatcher().sendComponentText(5106, 44, "<col=aaaaaa>Level 1 (Claimed)</col>");
            player.getPacketDispatcher().sendComponentText(5106, 42, "Claimed");
        } else if (level >= 1) {
            player.getPacketDispatcher().sendComponentText(5106, 44, "<col=00ff00>Level 1</col>");
            player.getPacketDispatcher().sendComponentText(5106, 42, "Claim");
        } else {
            player.getPacketDispatcher().sendComponentText(5106, 44, "<col=ff0000>Level 1</col>");
            player.getPacketDispatcher().sendComponentText(5106, 42, "Locked");
        }

        // ----- LEVEL 2 UI -----
        boolean level2Claimed = player.getBooleanAttribute("claimedLevel2Reward");
        if (level2Claimed) {
            player.getPacketDispatcher().sendComponentText(5106, 52, "<col=aaaaaa>Level 2 (Claimed)</col>");
            player.getPacketDispatcher().sendComponentText(5106, 50, "Claimed");
        } else if (level >= 2) {
            player.getPacketDispatcher().sendComponentText(5106, 52, "<col=00ff00>Level 2</col>");
            player.getPacketDispatcher().sendComponentText(5106, 50, "Claim");
        } else {
            player.getPacketDispatcher().sendComponentText(5106, 52, "<col=ff0000>Level 2</col>");
            player.getPacketDispatcher().sendComponentText(5106, 50, "Locked");
        }

        // ----- LEVEL 3 UI -----
        boolean level3Claimed = player.getBooleanAttribute("claimedLevel3Reward");
        if (level3Claimed) {
            player.getPacketDispatcher().sendComponentText(5106, 60, "<col=aaaaaa>Level 3 (Claimed)</col>");
            player.getPacketDispatcher().sendComponentText(5106, 58, "Claimed");
        } else if (level >= 3) {
            player.getPacketDispatcher().sendComponentText(5106, 60, "<col=00ff00>Level 3</col>");
            player.getPacketDispatcher().sendComponentText(5106, 58, "Claim");
        } else {
            player.getPacketDispatcher().sendComponentText(5106, 60, "<col=ff0000>Level 3</col>");
            player.getPacketDispatcher().sendComponentText(5106, 58, "Locked");
        }
        // ----- LEVEL 4 UI -----
        boolean level4Claimed = player.getBooleanAttribute("claimedLevel4Reward");
        if (level4Claimed) {
            player.getPacketDispatcher().sendComponentText(5106, 68, "<col=aaaaaa>Level 4 (Claimed)</col>");
            player.getPacketDispatcher().sendComponentText(5106, 66, "Claimed");
        } else if (level >= 4) {
            player.getPacketDispatcher().sendComponentText(5106, 68, "<col=00ff00>Level 4</col>");
            player.getPacketDispatcher().sendComponentText(5106, 66, "Claim");
        } else {
            player.getPacketDispatcher().sendComponentText(5106, 68, "<col=ff0000>Level 4</col>");
            player.getPacketDispatcher().sendComponentText(5106, 66, "Locked");
        }
// ----- LEVEL 5 UI -----
        boolean level5Claimed = player.getBooleanAttribute("claimedLevel5Reward");
        if (level5Claimed) {
            player.getPacketDispatcher().sendComponentText(5106, 76, "<col=aaaaaa>Level 5 (Claimed)</col>");
            player.getPacketDispatcher().sendComponentText(5106, 74, "Claimed");
        } else if (level >= 5) {
            player.getPacketDispatcher().sendComponentText(5106, 76, "<col=00ff00>Level 5</col>");
            player.getPacketDispatcher().sendComponentText(5106, 74, "Claim");
        } else {
            player.getPacketDispatcher().sendComponentText(5106, 76, "<col=ff0000>Level 5</col>");
            player.getPacketDispatcher().sendComponentText(5106, 74, "Locked");
        }
// ----- LEVEL 6 UI -----
        boolean level6Claimed = player.getBooleanAttribute("claimedLevel6Reward");
        if (level6Claimed) {
            player.getPacketDispatcher().sendComponentText(5106, 84, "<col=aaaaaa>Level 6 (Claimed)</col>");
            player.getPacketDispatcher().sendComponentText(5106, 82, "Claimed");
        } else if (level >= 6) {
            player.getPacketDispatcher().sendComponentText(5106, 84, "<col=00ff00>Level 6</col>");
            player.getPacketDispatcher().sendComponentText(5106, 82, "Claim");
        } else {
            player.getPacketDispatcher().sendComponentText(5106, 84, "<col=ff0000>Level 6</col>");
            player.getPacketDispatcher().sendComponentText(5106, 82, "Locked");
        }

// ----- LEVEL 7 UI -----
        boolean level7Claimed = player.getBooleanAttribute("claimedLevel7Reward");
        if (level7Claimed) {
            player.getPacketDispatcher().sendComponentText(5106, 92, "<col=aaaaaa>Level 7 (Claimed)</col>");
            player.getPacketDispatcher().sendComponentText(5106, 90, "Claimed");
        } else if (level >= 7) {
            player.getPacketDispatcher().sendComponentText(5106, 92, "<col=00ff00>Level 7</col>");
            player.getPacketDispatcher().sendComponentText(5106, 90, "Claim");
        } else {
            player.getPacketDispatcher().sendComponentText(5106, 92, "<col=ff0000>Level 7</col>");
            player.getPacketDispatcher().sendComponentText(5106, 90, "Locked");
        }

// ----- LEVEL 8 UI -----
        boolean level8Claimed = player.getBooleanAttribute("claimedLevel8Reward");
        if (level8Claimed) {
            player.getPacketDispatcher().sendComponentText(5106, 100, "<col=aaaaaa>Level 8 (Claimed)</col>");
            player.getPacketDispatcher().sendComponentText(5106, 98, "Claimed");
        } else if (level >= 8) {
            player.getPacketDispatcher().sendComponentText(5106, 100, "<col=00ff00>Level 8</col>");
            player.getPacketDispatcher().sendComponentText(5106, 98, "Claim");
        } else {
            player.getPacketDispatcher().sendComponentText(5106, 100, "<col=ff0000>Level 8</col>");
            player.getPacketDispatcher().sendComponentText(5106, 98, "Locked");
        }


    }

    @Override
    public GameInterface getInterface() {
        return GameInterface.BATTLEPASS2;
    }
}
