package com.zenyte.game.model.ui.testinterfaces;

import com.near_reality.api.service.user.UserPlayerHandler;
import com.near_reality.game.world.entity.player.PlayerAttributesKt;
import com.zenyte.game.GameInterface;
import com.zenyte.game.StarterLimiter;
import com.zenyte.game.item.Item;
import com.zenyte.game.model.HintArrow;
import com.zenyte.game.model.HintArrowPosition;
import com.zenyte.game.model.ui.Interface;
import com.zenyte.game.model.ui.InterfacePosition;
import com.zenyte.game.task.WorldTasksManager;
import com.zenyte.game.util.AccessMask;
import com.zenyte.game.world.World;
import com.zenyte.game.world.entity.masks.UpdateFlag;
import com.zenyte.game.world.entity.npc.NPC;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.game.world.entity.player.SkillConstants;
import com.zenyte.game.world.entity.player.Skills;
import com.zenyte.game.world.entity.player.dialogue.Dialogue;
import com.zenyte.game.world.entity.player.privilege.GameMode;
import com.zenyte.game.world.entity.player.var.VarCollection;
import com.zenyte.plugins.dialogue.PlainChat;
import com.zenyte.plugins.renewednpc.ZenyteGuide;
import kotlin.Unit;

//import java.util.Optional;
//
//import static com.zenyte.game.world.region.area.RegisterIslandArea.ZENYTE_GUIDE_LOCATION;
//
///**
// * @author Tommeh | 28-10-2018 | 20:42
// * @see <a href="https://www.rune-server.ee/members/tommeh/">Rune-Server profile</a>}
// */
//public class GameModeSetupInterface extends Interface {
//
//    @Override
//    protected void attach() {
//        put(9, "Rate 1");
//        put(10, "Rate 2");
//        put(11, "Rate 3");
//        put(13, "Rate 4");
//        put(14, "Mode");
//    }
//
//    @Override
//    public void open(Player player) {
//        final Object attr = player.getTemporaryAttributes().get("ironman_setup");
//
//        // Altijd een mode hebben (fallback)
//        if (PlayerAttributesKt.getSelectedGameMode(player) == null) {
//            PlayerAttributesKt.setSelectedGameMode(player, GameMode.REGULAR);
//        }
//
//        if (PlayerAttributesKt.getSelectedGameModeDifficulty(player) < 0 || PlayerAttributesKt.getSelectedGameModeDifficulty(player) > 3) {
//            PlayerAttributesKt.setSelectedGameModeDifficulty(player, 0);
//        }
//
//        // ❗Forceer UI-sync bij openen, ongeacht of het al gezet was
//        final GameMode currentMode = PlayerAttributesKt.getSelectedGameMode(player);
//        final int currentDifficulty = PlayerAttributesKt.getSelectedGameModeDifficulty(player);
//
//        // Selecteer juiste tab en rate visueel + intern
//        tab(player, getTabSlotForGameMode(currentMode));
//        selectExpMode(player, currentDifficulty);
//
//        // Originele logic
//        if (!(attr instanceof final String type))
//            return;
//
//        if (type.equals("register")) {
//            VarCollection.IRONMAN_MODE.send(player, 0);
//            VarCollection.UNKNOWN_IRONMAN.send(player, 0);
//            VarCollection.PIN_IRONMAN_MODE.send(player, 1);
//        } else if (type.equals("review")) {
//            VarCollection.PIN_IRONMAN_MODE.send(player, 0);
//            VarCollection.IRONMAN_MODE.updateSingle(player);
//            VarCollection.UNKNOWN_IRONMAN.updateSingle(player);
//        }
//
//        player.getPacketDispatcher().sendComponentSettings(getInterface(), 14, 0, 44, AccessMask.CLICK_OP1);
//        player.getInterfaceHandler().sendInterface(getInterface());
//        tab(player, 0);
//    }
//
//
//    @Override
//    public void close(final Player player, final Optional<GameInterface> replacement) {
//        player.lock();
//        selectExpMode(player, PlayerAttributesKt.getSelectedGameModeDifficulty(player));
//
//        WorldTasksManager.schedule(() -> {//have to delay it because closing will also close dialogue
//
//            final GameMode mode = PlayerAttributesKt.getSelectedGameMode(player);
//            if (mode == GameMode.GROUP_HARDCORE_IRON_MAN) {
//                   player.getDialogueManager().start(new Dialogue(player) {
//                    @Override
//                    public void buildDialogue() {
//                        npc(ZenyteGuide.NPC_ID, "<col=fe3200>Group Hardcore Iron man is disabled for now, please select another configuration.", 1)
//                                .executeAction(() -> {
//                                    player.unlock();
//                                    GameInterface.GAME_MODE_SETUP.open(player);
//                                });
//                    }
//                });
//                return;
//            }
//
//            final Object attr = player.getTemporaryAttributes().get("ironman_setup");
//            if (!(attr instanceof final String type))
//                return;
//
//            if (type.equals("register")) {
//                if (player.getVarManager().getBitValue(VarCollection.PIN_IRONMAN_MODE.getId()) == 1) {
//                    VarCollection.PIN_IRONMAN_MODE.send(player, 0);
//                }
//                int skilling = player.getSkillingXPRate();
//                int combat = player.getCombatXPRate();
//                NPC npc = World.findNPC(ZenyteGuide.NPC_ID, ZENYTE_GUIDE_LOCATION, 10).get();
//                player.getDialogueManager().start(new Dialogue(player, npc) {
//                    @Override
//                    public void buildDialogue() {
//                        npc(ZenyteGuide.NPC_ID, "Are you sure you would like to choose the<br><col=00080>" + mode + "</col> mode with,<br>" +
//                                "<col=00080>" + combat + "</col>x Combat exp, and <col=00080>" + skilling + "</col>x Skilling exp.", 1);
//                        options("Do you want <col=00080>" + mode + "</col> mode, <col=00080>" + combat + "</col>x Combat, <col=00080>" + skilling + "</col>x Skilling?", "Yes!", "No, not yet.").onOptionOne(() -> {
//                            player.getInterfaceHandler().closeInterface(InterfacePosition.CENTRAL);
//                            player.unlock();
//                            VarCollection.IRONMAN_MODE.send(player, mode.ordinal());
//                            PlayerAttributesKt.setSelectedGameMode(player, mode);
//                            if (mode == GameMode.REGULAR) {
//                                int[] combatSkills = {
//                                        SkillConstants.ATTACK,
//                                        SkillConstants.DEFENCE,
//                                        SkillConstants.STRENGTH,
//                                        SkillConstants.HITPOINTS,
//                                        SkillConstants.RANGED,
//                                        SkillConstants.PRAYER,
//                                        SkillConstants.MAGIC
//                                };
//
//                                for (int skill : combatSkills) {
//                                    double xp = Skills.getXPForLevel(99);
//                                    player.getSkills().forceSkill(skill, 99, xp);
//                                    player.getSkills().refresh(skill);
//                                }
//
//                                // Optioneel: update appearance en combat level
//                                player.getUpdateFlags().flag(UpdateFlag.APPEARANCE);
//                                player.getVarManager().sendBit(13027, player.getSkills().getCombatLevel());
//
//                                player.sendMessage("<col=FF0000>Your combat stats have been set to level 99.");
//                            }
//
//
//                            setKey(5);
//                        }).onOptionTwo(() -> {
//                            player.unlock();
//                            GameInterface.GAME_MODE_SETUP.open(player);
//                        });
//                        npc(5, "Now go to the portal, but be warned you cannot change your game mode once you travel there. Talk to me again if you want to change your game mode").setOnDisplay(() -> player.getPacketDispatcher().sendHintArrow(new HintArrow(3360, 7194, (byte) 50, HintArrowPosition.NORTH)));
//                    }
//
//                });
//            } else if (type.equals("review")) {
//                player.unlock();
//                final GameMode currentMode = player.getGameMode();
//                if (mode.equals(currentMode)) {
//                    return;
//                }
//                if (currentMode.equals(GameMode.REGULAR)) {
//                    player.sendMessage("You cannot become a " + mode + " as a regular player.");
//                    return;
//                }
//                if (currentMode.equals(GameMode.ULTIMATE_IRON_MAN) && mode.equals(GameMode.HARDCORE_IRON_MAN) || currentMode.equals(GameMode.HARDCORE_IRON_MAN) && mode.equals(GameMode.ULTIMATE_IRON_MAN)) {
//                    player.sendMessage("You cannot become a " + mode + " after leaving Tutorial Island.");
//                    return;
//                }
//                if (currentMode.equals(GameMode.STANDARD_IRON_MAN) && mode.equals(GameMode.HARDCORE_IRON_MAN) || mode.equals(GameMode.ULTIMATE_IRON_MAN)) {
//                    player.sendMessage("You cannot become a " + mode + " after leaving Tutorial Island.");
//                    return;
//                }
//                player.getInterfaceHandler().closeInterface(InterfacePosition.CENTRAL);
//                player.getDialogueManager().start(new Dialogue(player) {
//                    @Override
//                    public void buildDialogue() {
//                        plain("Are you sure you want to revoke your current <col=00080>" + currentMode + "</col> mode and switch to the <col=00080>" + mode + "</col> mode instead?");
//                        options(TITLE, "Yes, I'm sure.", "No.")
//                                .onOptionOne(() -> {
//                                    final Item[] oldArmour = ZenyteGuide.STARTER_ITEMS[currentMode.ordinal()];
//                                    final Item[] newArmour = ZenyteGuide.STARTER_ITEMS[mode.ordinal()];
//                                    UserPlayerHandler.INSTANCE.updateGameMode(player, mode, (success) -> {
//                                        if (success) {
//                                            player.getInventory().deleteItems(oldArmour).onFailure(item -> {
//                                                player.getEquipment().deleteItem(item).onFailure(i -> {
//                                                    player.getBank().remove(i);
//                                                });
//                                            });
//
//                                            if (player.isIronman()) {
//                                                String ip = player.getIP();
//                                                String mac = player.getMACAddress();
//
//                                                if (!StarterLimiter.canReceiveStarter(ip, mac)) {
//                                                    player.sendMessage("Je hebt het maximum aantal starter gear ontvangen op dit IP/MAC-adres.");
//                                                } else {
//                                                    StarterLimiter.registerStarter(ip, mac);
//                                                    player.getInventory().addItems(newArmour).onFailure(item -> player.getBank().add(item).onFailure(i -> World.spawnFloorItem(i, player)));
//                                                }
//                                            }
//
//                                            setKey(5);
//
//                                        } else
//                                            player.getDialogueManager().start(new PlainChat(player, "Failed to change your game-mode due to internal server error, please try again later."));
//                                        return Unit.INSTANCE;
//                                    });
//                                }).onOptionTwo(() -> {
//                                    player.getTemporaryAttributes().put("ironman_setup", "review");
//                                    open(player);
//                                });
//                        plain(5, "Congratulations, you have successfully changed your game mode to <col=00080>" + mode + "</col>.");
//                    }
//                });
//            }
//        }, 1);
//
//    }
//
//    @Override
//    protected void build() {
//        bind("Rate 1", player -> selectExpMode(player, 0));
//        bind("Rate 2", player -> selectExpMode(player, 1));
//        bind("Rate 3", player -> selectExpMode(player, 2));
//        bind("Rate 4", player -> selectExpMode(player, 3));
//        bind("Mode", (player, slotId, itemId, option) -> tab(player, slotId));
//    }
//
//    private void tab(Player player, int slotId) {
//        GameMode gameMode;
//
//        gameMode = switch (slotId) {
//            case 0 -> GameMode.REGULAR;                    // eerste tab: PVP
//            case 9 -> GameMode.STANDARD_IRON_MAN;
//            case 18 -> GameMode.HARDCORE_IRON_MAN;
//            case 27 -> GameMode.ULTIMATE_IRON_MAN;
//            //case 36 -> GameMode.REGULAR;              // laatste tab: REGULAR
//            default -> GameMode.REGULAR;                  // fallback
//        };
//
//        player.getVarManager().sendVar(263, gameMode != GameMode.REGULAR ? 0 : -1);
//        PlayerAttributesKt.setSelectedGameMode(player, gameMode);
//        selectExpMode(player, 0);
//    }
//    private int getTabSlotForGameMode(GameMode mode) {
//        return switch (mode) {
//            case REGULAR -> 0;
//            case STANDARD_IRON_MAN -> 9;
//            case HARDCORE_IRON_MAN -> 18;
//            case ULTIMATE_IRON_MAN -> 27;
//            //case REGULAR -> 36;
//            default -> 0;
//        };
//    }
//
//
//    private void selectExpMode(final Player player, int rate) {
//        PlayerAttributesKt.setSelectedGameModeDifficulty(player, rate);
//        final GameMode gameMode = PlayerAttributesKt.getSelectedGameMode(player);
//        if (gameMode.isGroupIronman()) {
//            PlayerAttributesKt.setSelectedGameMode(player, GameMode.GROUP_IRON_MAN);
//            player.setExperienceMultiplier(20, 20);
//            return;
//        }
//        switch (rate) {
//            case 0:
//                if (gameMode == GameMode.REGULAR)
//                    player.setExperienceMultiplier(150, 80);
//                else
//                    player.setExperienceMultiplier(80, 80);
//                break;
//            case 1:
//                if (gameMode == GameMode.REGULAR)
//                    player.setExperienceMultiplier(80, 50);
//                else
//                    player.setExperienceMultiplier(20, 20);
//                break;
//            case 2:
//                player.setExperienceMultiplier(10, 10);
//                break;
//            case 3:
//                player.setExperienceMultiplier(5, 5);
//                break;
//        }
//    }
//
//    @Override
//    public GameInterface getInterface() {
//        return GameInterface.GAME_MODE_SETUP;
//    }
//}
