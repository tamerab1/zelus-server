package com.near_reality.game.world.entity.player.bot.loadouts

import com.near_reality.game.world.entity.player.bot.*
import com.zenyte.game.item.Item
import com.zenyte.game.world.entity.player.container.impl.equipment.EquipmentSlot

/**
 * Loadout configuration for the Melee DDS bot (current bot).
 * 
 * @author Riskers
 */
object MeleeDDSLoadout {
    val loadout = BotLoadout(
        type = BotType.MELEE_DDS,
        name = "Melee DDS Bot",
        equipment = mapOf(
            EquipmentSlot.HELMET to Item(10828, 1),
            EquipmentSlot.CAPE to Item(6570, 1),
            EquipmentSlot.AMULET to Item(6585, 1),
            EquipmentSlot.WEAPON to Item(4587, 1),
            EquipmentSlot.PLATE to Item(1127, 1),
            EquipmentSlot.SHIELD to Item(8850, 1),
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
            Item(5698, 1)
        ),
        specialAttackConfig = SpecialAttackConfig(
            weaponId = 5698,
            energyRequired = 25,
            targetHealthThreshold = 50,
            consecutiveSpecs = true,
            maxConsecutiveSpecs = 2,
            switchBackWeaponId = 4587
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