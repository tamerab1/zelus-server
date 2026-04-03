package com.zenyte.game.model.ui.testinterfaces;

import com.zenyte.game.GameInterface;
import com.zenyte.game.model.ui.Interface;
import com.zenyte.game.world.entity.masks.UpdateFlag;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.game.world.entity.player.SkillConstants;
import com.zenyte.game.world.entity.player.Skills;
import com.zenyte.game.world.entity.player.dialogue.Dialogue;
import com.zenyte.game.world.entity.player.privilege.GameMode;
import com.zenyte.game.world.region.area.wilderness.WildernessArea;

import java.util.Set;

/**
 * @author Kris | 16/04/2019 16:57
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
public class SkillsTabInterface extends Interface {

    private static final int[] SKILL_BUTTON_VARPS = new int[] { 0, 1, 2, 5, 3, 7, 4, 12, 22, 6, 8, 9, 10, 11, 19, 20, 23, 13, 14, 15, 16, 17, 18, 21 };
    private static final int[] COMPONENT_ID_TO_SKILL = new int[10]; // Index 0 t/m 9
    private static final Set<Integer> MANUALLY_SETTABLE_SKILLS = Set.of(
            SkillConstants.ATTACK,
            SkillConstants.STRENGTH,
            SkillConstants.DEFENCE,
            SkillConstants.RANGED,
            SkillConstants.PRAYER,
            SkillConstants.MAGIC,
            SkillConstants.HITPOINTS
    );

    static {
        COMPONENT_ID_TO_SKILL[1] = SkillConstants.ATTACK;
        COMPONENT_ID_TO_SKILL[2] = SkillConstants.STRENGTH;
        COMPONENT_ID_TO_SKILL[3] = SkillConstants.DEFENCE;
        COMPONENT_ID_TO_SKILL[4] = SkillConstants.RANGED;
        COMPONENT_ID_TO_SKILL[5] = SkillConstants.PRAYER;
        COMPONENT_ID_TO_SKILL[6] = SkillConstants.MAGIC;
        COMPONENT_ID_TO_SKILL[9] = SkillConstants.HITPOINTS;
    }





    @Override
    protected DefaultClickHandler getDefaultHandler() {

        return (player, componentId, slotId, itemId, optionId) -> {
            if (optionId == 1) { // Mobile XP popup
                return;
            }
            if (player.isLocked()) {
                return;
            }
            if (player.isUnderCombat()) {
                player.sendMessage("You can't do this while in combat.");
                return;
            }

            if (componentId < 0 || componentId >= COMPONENT_ID_TO_SKILL.length) {
                return;
            }
            int skill = COMPONENT_ID_TO_SKILL[componentId];
            System.out.println("Clicked componentId: " + componentId);


            if (MANUALLY_SETTABLE_SKILLS.contains(skill)) {
                if (!player.getGameMode().equals(GameMode.REGULAR)) {
                    player.sendMessage("You can only set your levels manually in PvP mode.");
                    return;
                }
                if (!player.getEquipment().getContainer().isEmpty()) {
                    player.sendMessage("Please unequip all gear before manually changing your combat stats.");
                    return;
                }
                if (WildernessArea.isWithinWilderness(player.getX(), player.getY())) {
                    player.sendMessage("You cannot change combat stats while in the Wilderness.");
                    return;
                }
                player.getDialogueManager().start(new Dialogue(player) {
                    @Override
                    public void buildDialogue() {

                        options("Select an option for " + Skills.getSkillName(skill),
                                new DialogueOption("View Skill Guide", () -> player.getSkills().sendSkillMenu(skill, 0)),
                                new DialogueOption("Set Level Manually", () -> {
                                    player.getDialogueManager().finish();
                                    player.sendInputInt("Enter new level for " + Skills.getSkillName(skill) + ":", level -> {
                                        if (level < 1 || level > 99) {
                                            player.sendMessage("Invalid level. Must be between 1 and 99.");
                                            return;
                                        }

                                        double xp = Skills.getXPForLevel(level);
                                        player.getSkills().forceSkill(skill, level, xp);
                                        player.getSkills().refresh(skill);
                                        player.getUpdateFlags().flag(UpdateFlag.APPEARANCE);
                                        player.getVarManager().sendBit(13027, player.getSkills().getCombatLevel());

                                        if (skill == SkillConstants.HITPOINTS) {
                                            player.heal(level);
                                        }
                                        if (skill == SkillConstants.PRAYER) {
                                            player.getPrayerManager().restorePrayerPoints(level);
                                        }

                                        player.sendMessage("Set " + Skills.getSkillName(skill) + " level to " + level + ".");
                                    });
                                })
                        );
                    }
                });
            }
 else {
                player.getSkills().sendSkillMenu(skill, 0);
            }
        };
    }



    @Override
    protected void attach() {

    }

    @Override
    public void open(final Player player) {
        player.getInterfaceHandler().sendInterface(this);
    }

    @Override
    protected void build() {

    }

    @Override
    public GameInterface getInterface() {
        return GameInterface.SKILLS_TAB;
    }
}
