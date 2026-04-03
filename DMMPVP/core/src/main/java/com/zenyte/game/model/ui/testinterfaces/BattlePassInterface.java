package com.zenyte.game.model.ui.testinterfaces;

import com.zenyte.game.GameInterface;
import com.zenyte.game.model.ui.Interface;
import com.zenyte.game.world.entity.player.Player;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class BattlePassInterface extends Interface {

    @Override
    protected void attach() {
        put(10, "Level 1 Display");
        put(42, "Claim Level 1 Reward");
        put(52, "Level 2 Display");
        put(50, "Claim Level 2 Reward");
        put(60, "Level 3 Display");
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
    }

    @Override
    protected void build() {
        // -------- Level 1 --------
        bind("Claim Level 1 Reward", (player, slotId, itemId, option) -> {
            if (player.getBattlePassLevel() >= 1) {
                if (!player.getBooleanAttribute("claimedLevel1Reward")) {
                    player.getInventory().addItem(6199, 1);
                    player.putBooleanAttribute("claimedLevel1Reward", true);
                    player.sendMessage("You've claimed the Level 1 reward!");
                    player.getPacketDispatcher().sendComponentText(5106, 44, "<col=aaaaaa>Level 1 (Claimed)</col>");
                    player.getPacketDispatcher().sendComponentText(5106, 42, "Claimed");
                } else {
                    player.sendMessage("You've already claimed this reward.");
                }
            } else {
                player.sendMessage("You must reach Battle Pass Level 1 and 5 wilderness kills to claim this reward.");
            }
        });

        // -------- Level 2 --------
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
                player.sendMessage("You must reach Battle Pass Level 2 and 10 wilderness kills to claim this reward.");
            }
        });

        // -------- Level 3 --------
        bind("Claim Level 3 Reward", (player, slotId, itemId, option) -> {
            if (player.getBattlePassLevel() >= 3) {
                if (!player.getBooleanAttribute("claimedLevel3Reward")) {
                    player.getInventory().addItem(28313, 1);
                    player.putBooleanAttribute("claimedLevel3Reward", true);
                    player.sendMessage("You've claimed the Level 3 reward!");
                    player.getPacketDispatcher().sendComponentText(5106, 60, "<col=aaaaaa>Level 3 (Claimed)</col>");
                    player.getPacketDispatcher().sendComponentText(5106, 58, "Claimed");
                } else {
                    player.sendMessage("You've already claimed this reward.");
                }
            } else {
                player.sendMessage("You must reach Battle Pass Level 3 and 25 wilderness kills to claim this reward.");
            }
        });

        // -------- Level 4 --------
        bind("Claim Level 4 Reward", (player, slotId, itemId, option) -> {
            if (player.getBattlePassLevel() >= 4) {
                if (!player.getBooleanAttribute("claimedLevel4Reward")) {
                    player.getInventory().addItem(7498, 1);
                    player.getInventory().addItem(13307, 25000);
                    player.putBooleanAttribute("claimedLevel4Reward", true);
                    player.sendMessage("You've claimed the Level 4 reward!");
                    player.getPacketDispatcher().sendComponentText(5106, 68, "<col=aaaaaa>Level 4 (Claimed)</col>");
                    player.getPacketDispatcher().sendComponentText(5106, 66, "Claimed");
                } else {
                    player.sendMessage("You've already claimed this reward.");
                }
            } else {
                player.sendMessage("You must reach Battle Pass Level 4 and 50 wilderness kills to claim this reward.");
            }
        });

        // -------- Level 5 --------
        bind("Claim Level 5 Reward", (player, slotId, itemId, option) -> {
            if (player.getBattlePassLevel() >= 5) {
                if (!player.getBooleanAttribute("claimedLevel5Reward")) {
                    player.getInventory().addItem(27690, 1);
                    player.getInventory().addItem(13307, 10000);
                    player.putBooleanAttribute("claimedLevel5Reward", true);
                    player.sendMessage("You've claimed the Level 5 reward!");
                    player.getPacketDispatcher().sendComponentText(5106, 76, "<col=aaaaaa>Level 5 (Claimed)</col>");
                    player.getPacketDispatcher().sendComponentText(5106, 74, "Claimed");
                } else {
                    player.sendMessage("You've already claimed this reward.");
                }
            } else {
                player.sendMessage("You must reach Battle Pass Level 5 and 75 wilderness kills to claim this reward.");
            }
        });

        // -------- Level 6 --------
        bind("Claim Level 6 Reward", (player, slotId, itemId, option) -> {
            if (player.getBattlePassLevel() >= 6) {
                if (!player.getBooleanAttribute("claimedLevel6Reward")) {
                    player.getInventory().addItem(13225, 1);
                    player.putBooleanAttribute("claimedLevel6Reward", true);
                    player.sendMessage("You've claimed the Level 6 reward!");
                    player.getPacketDispatcher().sendComponentText(5106, 84, "<col=aaaaaa>Level 6 (Claimed)</col>");
                    player.getPacketDispatcher().sendComponentText(5106, 82, "Claimed");
                } else {
                    player.sendMessage("You've already claimed this reward.");
                }
            } else {
                player.sendMessage("You must reach Battle Pass Level 6 and 100 wilderness kills to claim this reward.");
            }
        });

        // -------- Level 7 --------
        bind("Claim Level 7 Reward", (player, slotId, itemId, option) -> {
            if (player.getBattlePassLevel() >= 7) {
                if (!player.getBooleanAttribute("claimedLevel7Reward")) {
                    player.getInventory().addItem(11802, 1);
                    player.getInventory().addItem(32072, 1);
                    player.getInventory().addItem(32203, 1);// Dragon Claws
                    player.putBooleanAttribute("claimedLevel7Reward", true);
                    player.sendMessage("You've claimed the Level 7 reward!");
                    player.getPacketDispatcher().sendComponentText(5106, 92, "<col=aaaaaa>Level 7 (Claimed)</col>");
                    player.getPacketDispatcher().sendComponentText(5106, 90, "Claimed");
                } else {
                    player.sendMessage("You've already claimed this reward.");
                }
            } else {
                player.sendMessage("You must reach Battle Pass Level 7 and 150 wilderness kills to claim this reward.");
            }
        });

        // -------- Level 8 --------
        bind("Claim Level 8 Reward", (player, slotId, itemId, option) -> {
            if (player.getBattlePassLevel() >= 8) {
                if (!player.getBooleanAttribute("claimedLevel8Reward")) {
                    player.getInventory().addItem(1042, 1);
                    player.getInventory().addItem(13307, 50000);// Volcanic Blade
                    player.putBooleanAttribute("claimedLevel8Reward", true);
                    player.sendMessage("You've claimed the Level 8 reward!");
                    player.getPacketDispatcher().sendComponentText(5106, 100, "<col=aaaaaa>Level 8 (Claimed)</col>");
                    player.getPacketDispatcher().sendComponentText(5106, 98, "Claimed");
                } else {
                    player.sendMessage("You've already claimed this reward.");
                }
            } else {
                player.sendMessage("You must reach Battle Pass Level 8 250 wilderness kills to claim this reward.");
            }
        });
    }

    @Override
    public void open(Player player) {
        int interfaceId = 5106;
        player.getInterfaceHandler().sendInterface(getInterface());

        // Username + dagen over
        player.getPacketDispatcher().sendComponentText(interfaceId, 25, player.getUsername());
        long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.of(2025, 10, 31));
        player.getPacketDispatcher().sendComponentText(interfaceId, 38, "Battle Pass Ends: " + daysLeft + " days");

        int level = player.getBattlePassLevel();
        player.getPacketDispatcher().sendComponentText(interfaceId, 21, String.valueOf(level));

        // Dynamische status updates
        updateLevelText(player, 1, 44, 42);
        updateLevelText(player, 2, 52, 50);
        updateLevelText(player, 3, 60, 58);
        updateLevelText(player, 4, 68, 66);
        updateLevelText(player, 5, 76, 74);
        updateLevelText(player, 6, 84, 82);
        updateLevelText(player, 7, 92, 90);
        updateLevelText(player, 8, 100, 98);
    }

    private void updateLevelText(Player player, int levelReq, int displayComp, int claimComp) {
        int level = player.getBattlePassLevel();
        boolean claimed = player.getBooleanAttribute("claimedLevel" + levelReq + "Reward");
        if (claimed) {
            player.getPacketDispatcher().sendComponentText(5106, displayComp, "<col=aaaaaa>Level " + levelReq + " (Claimed)</col>");
            player.getPacketDispatcher().sendComponentText(5106, claimComp, "Claimed");
        } else if (level >= levelReq) {
            player.getPacketDispatcher().sendComponentText(5106, displayComp, "<col=00ff00>Level " + levelReq + "</col>");
            player.getPacketDispatcher().sendComponentText(5106, claimComp, "Claim");
        } else {
            player.getPacketDispatcher().sendComponentText(5106, displayComp, "<col=ff0000>Level " + levelReq + "</col>");
            player.getPacketDispatcher().sendComponentText(5106, claimComp, "Locked");
        }
    }

    @Override
    public GameInterface getInterface() {
        return GameInterface.BATTLEPASS;
    }
}
