package com.near_reality.game.world.entity.player.bot.loadouts

import com.near_reality.game.world.entity.player.bot.*
import com.zenyte.game.item.Item
import com.zenyte.game.world.entity.player.container.impl.equipment.EquipmentSlot

/**
 * Loadout configuration for the Second Melee Bot (Whip + AGS).
 * 
 * @author Riskers
 */
object MeleeWhipLoadout {
    val loadout = BotLoadout(
        type = BotType.MELEE_WHIP,
        name = "Melee Whip Bot",
        equipment = mapOf(
            EquipmentSlot.HELMET to Item(10828, 1),
            EquipmentSlot.CAPE to Item(6570, 1),
            EquipmentSlot.AMULET to Item(1725, 1),
            EquipmentSlot.WEAPON to Item(4151, 1),
            EquipmentSlot.PLATE to Item(10551, 1),
            EquipmentSlot.SHIELD to Item(8846, 1),
            EquipmentSlot.LEGS to Item(1079, 1),
            EquipmentSlot.HANDS to Item(7462, 1),
            EquipmentSlot.BOOTS to Item(4131, 1),
            EquipmentSlot.RING to Item(2550, 1),
            EquipmentSlot.AMMUNITION to Item(892, 100)
        ),
        inventory = listOf(
            Item(385, 20),
            Item(2434, 3),
            Item(2436, 2),
            Item(2440, 2),
            Item(11802, 1)
        ),
        specialAttackConfig = SpecialAttackConfig(
            weaponId = 11802,
            energyRequired = 50,
            targetHealthThreshold = 60,
            consecutiveSpecs = false,
            maxConsecutiveSpecs = 1,
            switchBackWeaponId = 4151
        ),
        combatConfig = CombatConfig(
            maxCombatDistance = 10,
            retreatHealthPercentage = 20,
            eatHealthPercentage = 50,
            eatUntilHealthPercentage = 70,
            usePrayers = true,
            aggressiveMode = true,
            autoRetreat = true,
            autoEat = true,
            autoPrayer = true,
            autoWeaponSwitch = true
        )
    )
} 