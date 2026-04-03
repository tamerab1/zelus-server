package com.zenyte.game.model.ui.testinterfaces;

import com.zenyte.game.GameInterface;
import com.zenyte.game.content.account_creation.XpModes;
import com.zenyte.game.model.HintArrow;
import com.zenyte.game.model.HintArrowPosition;
import com.zenyte.game.model.ui.Interface;
import com.zenyte.game.util.AccessMask;
import com.zenyte.game.world.entity.masks.UpdateFlag;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.game.world.entity.player.SkillConstants;
import com.zenyte.game.world.entity.player.Skills;
import com.zenyte.plugins.dialogue.PlainChat;
import com.zenyte.game.world.entity.player.dialogue.Dialogue;
import com.zenyte.game.world.entity.player.privilege.GameMode;
import com.near_reality.game.world.entity.player.PlayerAttributesKt;

public class GameModeInterfaceTest extends Interface {

    @Override
    protected void attach() {
        // Gamemodes
        put(32, "Regular Mode");
        put(38, "Ironman Mode");
        put(44, "Hardcore Ironman Mode");
        put(50, "Ultimate Ironman Mode");
        put(28, "Group Ironman Select");

        // Xp modes
        put(64, "Easy Mode");
        put(68, "Normal Mode");
        put(72, "Hard Mode");
        put(76, "Realism Mode");

        // Buttons
        put(89, "Create GameMode");
    }

    @Override
    protected void build() {
        // ✅ Gamemode selects
        bind("Regular Mode", (player, slotId, itemId, option) -> {
            player.getTemporaryAttributes().put("selected_game_mode", GameMode.REGULAR);
            player.getPacketDispatcher().sendClientScript(7054, 1);
            player.sendMessage("You selected <col=FF0000>Regular PVP Mode</col>.");
        });
        bind("Ironman Mode", (player, slotId, itemId, option) -> {
            player.getTemporaryAttributes().put("selected_game_mode", GameMode.STANDARD_IRON_MAN);
            player.getPacketDispatcher().sendClientScript(7054, 2);
            player.sendMessage("You selected <col=FF0000>Ironman Mode</col>.");
        });
        bind("Hardcore Ironman Mode", (player, slotId, itemId, option) -> {
            player.getTemporaryAttributes().put("selected_game_mode", GameMode.HARDCORE_IRON_MAN);
            player.getPacketDispatcher().sendClientScript(7054, 3);
            player.sendMessage("You selected <col=FF0000>Hardcore Ironman Mode</col>.");
        });
        bind("Ultimate Ironman Mode", (player, slotId, itemId, option) -> {
            player.getTemporaryAttributes().put("selected_game_mode", GameMode.ULTIMATE_IRON_MAN);
            player.getPacketDispatcher().sendClientScript(7054, 4);
            player.sendMessage("You selected <col=FF0000>Ultimate Ironman Mode</col>.");
        });

        // ✅ Xp mode selects
        bind("Easy Mode", (player, slotId, itemId, option) -> {
            player.getTemporaryAttributes().put("selected_xp_mode", XpModes.EASY);
            player.getPacketDispatcher().sendClientScript(7054, 5);
            player.sendMessage("You selected <col=FF0000>Easy Mode</col> (<col=FF0000>" +
                    XpModes.EASY.getCombatRate() + "x</col> combat / <col=FF0000>" +
                    XpModes.EASY.getSkillingRate() + "x</col> skilling).");
        });
        bind("Normal Mode", (player, slotId, itemId, option) -> {
            player.getTemporaryAttributes().put("selected_xp_mode", XpModes.MODERATE);
            player.getPacketDispatcher().sendClientScript(7054, 6);
            player.sendMessage("You selected <col=FF0000>Normal Mode</col> (<col=FF0000>" +
                    XpModes.MODERATE.getCombatRate() + "x</col> combat / <col=FF0000>" +
                    XpModes.MODERATE.getSkillingRate() + "x</col> skilling).");
        });
        bind("Hard Mode", (player, slotId, itemId, option) -> {
            player.getTemporaryAttributes().put("selected_xp_mode", XpModes.HARDCORE);
            player.getPacketDispatcher().sendClientScript(7054, 7);
            player.sendMessage("You selected <col=FF0000>Hard Mode</col> (<col=FF0000>" +
                    XpModes.HARDCORE.getCombatRate() + "x</col> combat / <col=FF0000>" +
                    XpModes.HARDCORE.getSkillingRate() + "x</col> skilling).");
        });
        bind("Realism Mode", (player, slotId, itemId, option) -> {
            player.getTemporaryAttributes().put("selected_xp_mode", XpModes.EXTREME);
            player.getPacketDispatcher().sendClientScript(7054, 8);
            player.sendMessage("You selected <col=FF0000>Realism Mode</col> (<col=FF0000>" +
                    XpModes.EXTREME.getCombatRate() + "x</col> combat / <col=FF0000>" +
                    XpModes.EXTREME.getSkillingRate() + "x</col> skilling).");
        });
        bind("Group Ironman Select", (player, slotId, itemId, option) -> {
            player.getDialogueManager().start(new Dialogue(player) {
                @Override
                public void buildDialogue() {
                    plain("What ironman mode do you want to select?");
                    options("Select Ironman Mode:",
                            "Group Ironman",
                            "Hardcore Group Ironman",
                            "Cancel")
                            .onOptionOne(() -> {
                                player.getTemporaryAttributes().put("selected_game_mode", GameMode.GROUP_IRON_MAN);
                                player.getPacketDispatcher().sendClientScript(7054, 9); // 👈 kies een vrij ID
                                player.sendMessage("You selected <col=FF0000>Group Ironman</col>.");
                                player.getPacketDispatcher().sendHintArrow(
                                        new HintArrow(3094, 3110, (byte) 50, HintArrowPosition.NORTH)
                                );
                            })
                            .onOptionTwo(() -> {
                                player.getTemporaryAttributes().put("selected_game_mode", GameMode.GROUP_HARDCORE_IRON_MAN);
                                player.getPacketDispatcher().sendClientScript(7054, 10); // 👈 kies een vrij ID
                                player.sendMessage("You selected <col=FF0000>Hardcore Group Ironman</col>.");
                                player.getPacketDispatcher().sendHintArrow(
                                        new HintArrow(3094, 3110, (byte) 50, HintArrowPosition.NORTH)
                                );
                            })
                            .onOptionThree(() -> {
                                player.sendMessage("You decide not to select a group ironman mode.");
                            });
                }
            });
        });

        // ✅ Create knop met bevestiging
        bind("Create GameMode", (player, slotId, itemId, option) -> {

            GameMode mode = (GameMode) player.getTemporaryAttributes()
                    .getOrDefault("selected_game_mode", GameMode.REGULAR);
            XpModes xpMode = (XpModes) player.getTemporaryAttributes()
                    .getOrDefault("selected_xp_mode", XpModes.EASY);

            player.getDialogueManager().start(new Dialogue(player) {
                @Override
                public void buildDialogue() {
                    plain("Are you sure you want to create your account as " +
                            "<col=FF0000>" + mode + "</col> with " +
                            "<col=FF0000>" + xpMode.getCombatRate() + "x</col> combat XP and " +
                            "<col=FF0000>" + xpMode.getSkillingRate() + "x</col> skilling XP?");
                    options("Confirm account creation?",
                            "Yes, create my account.",
                            "No, go back.")
                            .onOptionOne(() -> {
                                System.out.println("DEBUG: Player " + player.getUsername() + " creating account with mode=" + mode + ", xpMode=" + xpMode);

                                // Definitief aanmaken
                                PlayerAttributesKt.setSelectedGameMode(player, mode);
                                System.out.println("DEBUG: Player " + player.getUsername() + " saved gamemode as " + mode);

                                player.setExperienceMultiplier(xpMode.getCombatRate(), xpMode.getSkillingRate());

                                if (mode == GameMode.REGULAR) {
                                    setMaxCombatSkills(player);
                                }

                                player.getInterfaceHandler().closeInterface(getInterface().getPosition());
                                player.getDialogueManager().start(new PlainChat(player,
                                        "Your account has been created in <col=FF0000>" + mode + "</col> " +
                                                "with <col=FF0000>" + xpMode.getCombatRate() + "x</col> combat and " +
                                                "<col=FF0000>" + xpMode.getSkillingRate() + "x</col> skilling XP."));


                                player.getPacketDispatcher().sendHintArrow(
                                        new HintArrow(3094, 3110, (byte) 50, HintArrowPosition.NORTH)
                                );
                            })
                            .onOptionTwo(() -> {

                                GameInterface.CREATEACCOUNT.open(player);
                            });
                }
            });
        });
    }

    private void setMaxCombatSkills(Player player) {
        int[] combatSkills = {
                SkillConstants.ATTACK,
                SkillConstants.DEFENCE,
                SkillConstants.STRENGTH,
                SkillConstants.HITPOINTS,
                SkillConstants.RANGED,
                SkillConstants.PRAYER,
                SkillConstants.MAGIC
        };
        for (int skill : combatSkills) {
            double xp = Skills.getXPForLevel(99);
            player.getSkills().forceSkill(skill, 99, xp);
            player.getSkills().refresh(skill);
        }
        player.getUpdateFlags().flag(UpdateFlag.APPEARANCE);
        player.getVarManager().sendBit(13027, player.getSkills().getCombatLevel());
    }

    @Override
    public void open(Player player) {
        super.open(player);
        // Zet username in comp 88
        player.getPacketDispatcher().sendComponentText(
                getInterface(), 88, player.getUsername()
        );

        // Maak components klikbaar
        player.getPacketDispatcher().sendComponentSettings(getInterface(), 32, 0, 0, AccessMask.CLICK_OP1);
        player.getPacketDispatcher().sendComponentSettings(getInterface(), 38, 0, 0, AccessMask.CLICK_OP1);
        player.getPacketDispatcher().sendComponentSettings(getInterface(), 44, 0, 0, AccessMask.CLICK_OP1);
        player.getPacketDispatcher().sendComponentSettings(getInterface(), 50, 0, 0, AccessMask.CLICK_OP1);
        player.getPacketDispatcher().sendComponentSettings(getInterface(), 28, 0, 0, AccessMask.CLICK_OP1);

        player.getPacketDispatcher().sendComponentSettings(getInterface(), 64, 0, 0, AccessMask.CLICK_OP1);
        player.getPacketDispatcher().sendComponentSettings(getInterface(), 68, 0, 0, AccessMask.CLICK_OP1);
        player.getPacketDispatcher().sendComponentSettings(getInterface(), 72, 0, 0, AccessMask.CLICK_OP1);
        player.getPacketDispatcher().sendComponentSettings(getInterface(), 76, 0, 0, AccessMask.CLICK_OP1);

        player.getPacketDispatcher().sendComponentSettings(getInterface(), 89, 0, 0, AccessMask.CLICK_OP1);
    }

    @Override
    public GameInterface getInterface() {
        return GameInterface.CREATEACCOUNT;
    }
}
