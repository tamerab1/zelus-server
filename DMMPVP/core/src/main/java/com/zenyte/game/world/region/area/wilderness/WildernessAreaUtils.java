package com.zenyte.game.world.region.area.wilderness;

import com.near_reality.game.world.entity.player.PlayerAttributesKt;
import com.zenyte.game.GameInterface;
import com.zenyte.game.content.consumables.ConsumableEffects;
import com.zenyte.game.model.ui.testinterfaces.GameNoticeboardInterface;
import com.zenyte.game.model.ui.testinterfaces.advancedsettings.SettingVariables;
import com.zenyte.game.world.WorldThread;
import com.zenyte.game.world.entity.Entity;
import com.zenyte.game.world.entity.npc.NPC;
import com.zenyte.game.world.entity.npc.impl.wilderness.ChaosFanatic;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.game.world.entity.player.action.combat.PlayerCombat;
import com.zenyte.game.world.entity.player.action.combat.magic.CombatSpell;
import com.zenyte.game.world.entity.player.calog.CAType;
import com.zenyte.game.world.entity.player.dailychallenge.ChallengeCategory;
import com.zenyte.game.world.entity.player.dailychallenge.challenge.CombatChallenge;
import com.zenyte.game.world.entity.player.variables.TickVariable;
import com.zenyte.utils.TimeUnit;

import static com.zenyte.game.world.region.area.wilderness.WildernessArea.*;

public class WildernessAreaUtils {

    public static void enter(final Player player) {
        final boolean canPvp = player.isCanPvp();
        if (!canPvp) {
            setAttackable(player);
        }
        if (player.getVariables().getTime(TickVariable.OVERLOAD) > 0) {
            player.getVariables().cancel(TickVariable.OVERLOAD);
            ConsumableEffects.resetOverload(player);
        }
        if(player.getAttributes().containsKey("DIVINE_POTION")) {
            player.getAttributes().remove("DIVINE_POTION");
            player.getVariables().cancel(TickVariable.DIVINE_SUPER_COMBAT_POTION);
            for (int i = 0; i < 23; i++) {
                player.getSkills().setLevel(i, player.getSkills().getLevelForXp(i));
            }
        }
        // enable blighted stacks
        player.getVarManager().sendBit(5963, 1);
        player.getVariables().cancel(TickVariable.BOUNTY_HUNTER_TARGET_LOSS);
        if (player.isXPDropsMultiplied() && player.isXPDropsWildyOnly()) {
            player.getVarManager().sendVar(3504, player.getCombatXPRate());
        }
    }

    public static boolean sendDeath(Player player, Entity source) {
        if (source instanceof Player) {
            final Player killer = (Player) source;

            // Stats bijwerken
            killer.getAttributes().put("WildernessKills",
                    killer.getNumericAttribute("WildernessKills").intValue() + 1);
            player.getAttributes().put("WildernessDeaths",
                    player.getNumericAttribute("WildernessDeaths").intValue() + 1);

            refreshKDR(player);
            refreshKDR(killer);

            // ✅ Alle combat challenges met target "player" updaten
            killer.getDailyChallengeManager().getChallengeProgression().forEach((challenge, progress) -> {
                if (challenge.getCategory().equals(ChallengeCategory.COMBAT)) {
                    // Zorg dat we enkel PvP challenges meepakken
                    if (challenge instanceof CombatChallenge combat && "player".equalsIgnoreCase(combat.getNpc())) {
                        killer.getDailyChallengeManager().update(challenge, 1);
                    }
                }
            });
        }
        return false;
    }


    public static boolean attack(Player player, Entity entity, PlayerCombat combat) {
        if (entity instanceof Player) {
            final Player target = (Player) entity;
            if (target.inArea(FeroxEnclaveDangerArea.class) && target.getVariables().getTime(TickVariable.TELEBLOCK) <= 0 && player.getVariables().getTime(TickVariable.TELEBLOCK) <= 0) {
                player.sendMessage("You cannot fight another player whilst next to the Enclave, please move further out.");
                return false;
            }
            if (!player.getVariables().isSkulled() && player.getVarManager().getBitValue(SettingVariables.PK_SKULL_PREVENTION_VARBIT_ID) == 1 && canSkull(player, target)) {
                player.sendMessage("You cannot attack this target as it would result in you getting skulled.");
                return false;
            }
            if (player.isCanPvp() && !target.isCanPvp()) {
                player.sendMessage("That player is not in the wilderness.");
                return false;
            }
            final int level = getWildernessLevel(player.getLocation()).orElse(-1);
            final int otherLevel = getWildernessLevel(entity.getLocation()).orElse(-1);
            final int minimumLevel = Math.min(level, otherLevel);
            if (minimumLevel >= 1) {
                if (PlayerAttributesKt.getBlackSkulled(target))
                    return true;
                if (PlayerAttributesKt.getBlackSkulled(player) && player.getAttackedBy() == target)
                    return true;
                if (Math.abs(player.getSkills().getCombatLevel() - target.getSkills().getCombatLevel()) > minimumLevel) {
                    player.sendMessage("The difference between your Combat level and the Combat level of "
                            + target.getPlayerInformation().getDisplayname() + " is too great.");
                    player.sendMessage((target.getAppearance().isMale() ? "He" : "She") + " needs to move deeper into" +
                            " the Wilderness before you can attack them.");
                    return false;
                }
            }
        }
        return true;
    }


    private static boolean canSkull(Player player, Player target) {
        final long now = WorldThread.getCurrentCycle();
        return player.getAttackedByPlayers().getLong(target.getUsername()) < now
                && target.getAttackedByPlayers().getLong(player.getUsername()) < now;
    }

    public static void onAttack(final Player player, final Entity entity, final String style,
                         final CombatSpell spell, final boolean splash) {
        if (entity instanceof NPC) {
            return;
        }
        final Player target = (Player) entity;
        if (target != null && canSkull(player, target)) {
            player.getVariables().setSkull(true);
            target.getAttackedByPlayers().put(player.getUsername(), TimeUnit.MINUTES.toTicks(20) + WorldThread.getCurrentCycle());
        }
    }

    public static void leave(final Player player, boolean logout) {
        setUnattackable(player);
        // disable blighted stacks
        player.getVarManager().sendBit(5963, 0);
        if (player.isXPDropsMultiplied() && player.isXPDropsWildyOnly()) {
            player.getVarManager().sendVar(3504, 1);
        }
        player.getCombatAchievements().removeCurrentTaskFlag(CAType.PRAYING_TO_THE_GODS, ChaosFanatic.CA_TASK_PRAYING_TO_THE_GODS_STARTED);
    }

    public static void setAttackable(final Player player) {
        refreshKDR(player);
        player.setCanPvp(true);
        //Sets the special attack orb unclickable.
        player.getVarManager().sendBit(IN_WILDERNESS_VARBIT_SPECIAL_UNCLICKABLE, 0);
        GameNoticeboardInterface.refreshWildernessCounters(GameNoticeboardInterface.wildernessCount.incrementAndGet());
        //Supposed to clear received damage when re-entering wilderness.
        player.getReceivedDamage().clear();
        GameInterface.WILDERNESS_OVERLAY.open(player);
    }


    private static void refreshKDR(final Player player) {
        player.getVarManager().sendVar(1102, player.getNumericAttribute("WildernessDeaths").intValue());
        player.getVarManager().sendVar(1103, player.getNumericAttribute("WildernessKills").intValue());
    }

    public static void setUnattackable(final Player player) {
        player.getInterfaceHandler().closeInterface(GameInterface.WILDERNESS_OVERLAY);
        player.setCanPvp(false);
        //Check if next player location is not wilderness, then can reset tb. else don't reset will break ferox mechanics
        if (!isWithinWilderness(player.getLocation())) {
            player.getVariables().resetTeleblock();
            player.getReceivedDamage().clear();
        }
        player.getVarManager().sendBit(IN_WILDERNESS_VARBIT_SPECIAL_UNCLICKABLE, 0);
        GameNoticeboardInterface.refreshWildernessCounters(GameNoticeboardInterface.wildernessCount.decrementAndGet());
    }


}
