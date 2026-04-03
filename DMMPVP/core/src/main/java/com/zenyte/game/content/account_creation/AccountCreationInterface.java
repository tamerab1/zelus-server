/*package com.zenyte.game.content.account_creation;

import com.near_reality.api.service.user.UserPlayerHandler;
import com.near_reality.game.world.entity.player.PlayerAttributesKt;
import com.zenyte.game.GameInterface;
import com.zenyte.game.item.Item;
import com.zenyte.game.model.HintArrow;
import com.zenyte.game.model.HintArrowPosition;
import com.zenyte.game.model.ui.Interface;
import com.zenyte.game.model.ui.InterfacePosition;
import com.zenyte.game.task.WorldTasksManager;
import com.zenyte.game.util.AccessMask;
import com.zenyte.game.world.World;
import com.zenyte.game.world.entity.npc.NPC;
import com.zenyte.game.world.entity.player.GameCommands;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.game.world.entity.player.SkillConstants;
import com.zenyte.game.world.entity.player.Skills;
import com.zenyte.game.world.entity.player.dialogue.Dialogue;
import com.zenyte.game.world.entity.player.privilege.GameMode;
import com.zenyte.game.world.entity.player.privilege.PlayerPrivilege;
import com.zenyte.game.world.entity.player.var.VarCollection;
import com.zenyte.plugins.dialogue.PlainChat;
import com.zenyte.plugins.renewednpc.ZenyteGuide;
import kotlin.Unit;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.zenyte.game.world.region.area.RegisterIslandArea.ZENYTE_GUIDE_LOCATION;

public class AccountCreationInterface extends Interface {


    static {
        new GameCommands.Command(PlayerPrivilege.PLAYER, "ac-c", "open", (p, args) -> {
            GameInterface.ACCOUNT_CREATION_INTERFACE.open(p);
        });
    }

    @Override
    protected void attach() {
        put(11, "Game mode container");
        put(17, "Xp mode container");
    }


    public static final Map<GameMode, Integer> MAPPED_VALUES = new HashMap<>();
    static {
        MAPPED_VALUES.put(GameMode.REGULAR, 0);
        MAPPED_VALUES.put(GameMode.STANDARD_IRON_MAN, 1);
        MAPPED_VALUES.put(GameMode.HARDCORE_IRON_MAN, 2);
        MAPPED_VALUES.put(GameMode.ULTIMATE_IRON_MAN, 3);
        MAPPED_VALUES.put(GameMode.GROUP_IRON_MAN, 4);
    }





    @Override
    protected void build() {
        bind("Game mode container", ((player, slotId, itemId, option) -> {
            if (slotId == 0) {
                player.getTemporaryAttributes().put("selected_game_mode", GameMode.REGULAR);
            }
            if (slotId == 1) {
                player.getTemporaryAttributes().put("selected_game_mode", GameMode.STANDARD_IRON_MAN);
            }
            if (slotId == 2) {
                player.getTemporaryAttributes().put("selected_game_mode", GameMode.HARDCORE_IRON_MAN);
            }
            if (slotId == 3) {
                player.getTemporaryAttributes().put("selected_game_mode", GameMode.ULTIMATE_IRON_MAN);
            }
            if (slotId == 4) {
                player.getTemporaryAttributes().put("selected_game_mode", GameMode.GROUP_IRON_MAN);
            }
        }));

        bind("Xp mode container", ((player, slotId, itemId, option) -> {
            if (slotId == 0) {
                player.getTemporaryAttributes().put("selected_xp_mode", XpModes.EASY);
                player.setExperienceMultiplier(XpModes.EASY.getCombatRate(), XpModes.EASY.getSkillingRate());
            }
            if (slotId == 1) {
                player.getTemporaryAttributes().put("selected_xp_mode", XpModes.MODERATE);
                player.setExperienceMultiplier(XpModes.MODERATE.getCombatRate(), XpModes.MODERATE.getSkillingRate());
            }
            if (slotId == 2) {
                player.getTemporaryAttributes().put("selected_xp_mode", XpModes.HARDCORE);
                player.setExperienceMultiplier(XpModes.HARDCORE.getCombatRate(), XpModes.HARDCORE.getSkillingRate());
            }
            if (slotId == 3) {
                player.getTemporaryAttributes().put("selected_xp_mode", XpModes.EXTREME);
                player.setExperienceMultiplier(XpModes.EXTREME.getCombatRate(), XpModes.EXTREME.getSkillingRate());
            }
        }));


    }

    @Override
    public void close(final Player player, final Optional<GameInterface> replacement) {

        player.lock();

        WorldTasksManager.schedule(() -> {

            player.getTemporaryAttributes().put("ironman_setup", "register");
            final GameMode mode = (GameMode) player.getTemporaryAttributes().getOrDefault("selected_game_mode", GameMode.REGULAR);
            if (mode == GameMode.GROUP_HARDCORE_IRON_MAN) {
                player.getDialogueManager().start(new Dialogue(player) {
                    @Override
                    public void buildDialogue() {
                        npc(ZenyteGuide.NPC_ID, "<col=fe3200>Group Hardcore Iron man is disabled for now, please select another configuration.", 1)
                                .executeAction(() -> {
                                    player.unlock();
                                    GameInterface.ACCOUNT_CREATION_INTERFACE.open(player);
                                });
                    }
                });
                return;
            }

            final Object attr = player.getTemporaryAttributes().get("ironman_setup");
            if (!(attr instanceof final String type))
                return;

            if (type.equals("register")) {
                if (player.getVarManager().getBitValue(VarCollection.PIN_IRONMAN_MODE.getId()) == 1) {
                    VarCollection.PIN_IRONMAN_MODE.send(player, 0);
                }

                int skilling = player.getSkillingXPRate();
                int combat = player.getCombatXPRate();

                NPC npc = World.findNPC(ZenyteGuide.NPC_ID, ZENYTE_GUIDE_LOCATION, 10).get();
                player.getDialogueManager().start(new Dialogue(player, npc) {
                    @Override
                    public void buildDialogue() {
                        npc(ZenyteGuide.NPC_ID, "Are you sure you would like to choose the<br><col=00080>" + mode + "</col> mode with,<br>" +
                                "<col=00080>" + combat + "</col>x Combat exp, and <col=00080>" + skilling + "</col>x Skilling exp.", 1);
                        options("Do you want <col=00080>" + mode + "</col> mode, <col=00080>" + combat + "</col>x Combat, <col=00080>" + skilling + "</col>x Skilling?", "Yes!", "No, not yet.").onOptionOne(() -> {
                            player.getInterfaceHandler().closeInterface(InterfacePosition.CENTRAL);
                            player.unlock();
                            VarCollection.IRONMAN_MODE.send(player, mode.ordinal());
                            PlayerAttributesKt.setSelectedGameMode(player, mode);
                            setKey(5);
                        }).onOptionTwo(() -> {
                            player.unlock();
                            GameInterface.ACCOUNT_CREATION_INTERFACE.open(player);
                        });
                        npc(5, "Welcome...").setOnDisplay(() -> {
                            player.getTemporaryAttributes().remove("starting_setup");
                            player.putBooleanAttribute("registered", true);

                            // Als de gamemode REGULAR is, geef direct 99 combat stats
                            if (mode == GameMode.REGULAR) {
                                setMaxCombatSkills(player);
                            }
                            player.getPacketDispatcher().sendHintArrow(
                                    new HintArrow(3093, 3110, (byte) 0, HintArrowPosition.CENTER)
                            );
                        });

                    }

                });
            } else if (type.equals("review")) {
                player.unlock();
                final GameMode currentMode = player.getGameMode();
                if (mode.equals(currentMode)) {
                    return;
                }
                if (currentMode.equals(GameMode.REGULAR)) {
                    player.sendMessage("You cannot become a " + mode + " as a regular player.");
                    return;
                }
                if (currentMode.equals(GameMode.ULTIMATE_IRON_MAN) && mode.equals(GameMode.HARDCORE_IRON_MAN) || currentMode.equals(GameMode.HARDCORE_IRON_MAN) && mode.equals(GameMode.ULTIMATE_IRON_MAN)) {
                    player.sendMessage("You cannot become a " + mode + " after leaving Tutorial Island.");
                    return;
                }
                if (currentMode.equals(GameMode.STANDARD_IRON_MAN) && mode.equals(GameMode.HARDCORE_IRON_MAN) || mode.equals(GameMode.ULTIMATE_IRON_MAN)) {
                    player.sendMessage("You cannot become a " + mode + " after leaving Tutorial Island.");
                    return;
                }
                player.getInterfaceHandler().closeInterface(InterfacePosition.CENTRAL);
                player.getDialogueManager().start(new Dialogue(player) {
                    @Override
                    public void buildDialogue() {
                        plain("Are you sure you want to revoke your current <col=00080>" + currentMode + "</col> mode and switch to the <col=00080>" + mode + "</col> mode instead?");
                        options(TITLE, "Yes, I'm sure.", "No.")
                                .onOptionOne(() -> {
                                    final Item[] oldArmour = ZenyteGuide.STARTER_ITEMS[currentMode.ordinal()];
                                    final Item[] newArmour = ZenyteGuide.STARTER_ITEMS[mode.ordinal()];
                                    UserPlayerHandler.INSTANCE.updateGameMode(player, mode, (success) -> {
                                        if (success) {
                                            player.getInventory().deleteItems(oldArmour).onFailure(item -> {
                                                player.getEquipment().deleteItem(item).onFailure(i -> {
                                                    player.getBank().remove(i);
                                                });
                                            });
                                            if (player.isIronman()) {
                                                player.getInventory().addItems(newArmour).onFailure(item -> player.getBank().add(item).onFailure(i -> World.spawnFloorItem(i, player)));
                                            }
                                            setKey(5);
                                        } else
                                            player.getDialogueManager().start(new PlainChat(player, "Failed to change your game-mode due to internal server error, please try again later."));
                                        return Unit.INSTANCE;
                                    });
                                }).onOptionTwo(() -> {
                                    player.getTemporaryAttributes().put("ironman_setup", "review");
                                    open(player);
                                });
                        plain(5, "Congratulations, you have successfully changed your game mode to <col=00080>" + mode + "</col>.");
                    }
                });
            }
        }, 1);
    }


    @Override
    public void open(Player player) {

        player.getPacketDispatcher().sendClientScript(18900,
                5,
                4,
                getComponent("Game mode container") | (getId() << 16),
                getComponent("Xp mode container") | (getId() << 16));

        player.getPacketDispatcher().sendClientScript(18901, getComponent("Game mode container") | (getId() << 16),
                0, "Regular Game Mode", "<img=68> Regular PVP");

        player.getPacketDispatcher().sendClientScript(18901, getComponent("Game mode container") | (getId() << 16),
                1, "Ironman Mode", "<img=2> Ironman");

        player.getPacketDispatcher().sendClientScript(18901, getComponent("Game mode container") | (getId() << 16),
                2, "Hardcore Ironman Mode", "<img=10> Hardcore Ironman");

        player.getPacketDispatcher().sendClientScript(18901, getComponent("Game mode container") | (getId() << 16),
                3, "Ultimate Ironman Mode", "<img=3> Ultimate Ironman");

        player.getPacketDispatcher().sendClientScript(18901, getComponent("Game mode container") | (getId() << 16),
                4, "Group Ironman Mode", "<img=41> Group Ironman");

        player.getPacketDispatcher().sendClientScript(18904,
                getComponent("Xp mode container") | (getId() << 16),
                0, "Easy Difficulty", "180x", "70x", "0%");

        player.getPacketDispatcher().sendClientScript(18904,
                getComponent("Xp mode container") | (getId() << 16),
                1, "Moderate Difficulty", "50x", "20x", "2%");

        player.getPacketDispatcher().sendClientScript(18904,
                getComponent("Xp mode container") | (getId() << 16),
                2, "Hardcore Difficulty", "20x", "10x", "5%");

        player.getPacketDispatcher().sendClientScript(18904,
                getComponent("Xp mode container") | (getId() << 16),
                3, "Extreme Difficulty", "5x", "3x", "8%");

        player.getPacketDispatcher().sendComponentSettings(getInterface(),
                getComponent("Game mode container"), 0, 4, AccessMask.CLICK_OP1);

        player.getPacketDispatcher().sendComponentSettings(getInterface(),
                getComponent("Xp mode container"), 0, 3, AccessMask.CLICK_OP1);


        player.getInterfaceHandler().sendInterface(getInterface());



        var gameMode = (GameMode) player.getTemporaryAttributes().getOrDefault("selected_game_mode", GameMode.REGULAR);
        var xpMode = (XpModes) player.getTemporaryAttributes().getOrDefault("selected_xp_mode", XpModes.EASY);


        player.getPacketDispatcher().sendClientScript(18902,
                getComponent("Game mode container") | (getId() << 16), MAPPED_VALUES.get(gameMode));

        player.getPacketDispatcher().sendClientScript(18903,
                getComponent("Xp mode container") | (getId() << 16), xpMode.getIndex());

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
    }



    @Override
    public GameInterface getInterface() {
        return GameInterface.ACCOUNT_CREATION_INTERFACE;
    }
}*/