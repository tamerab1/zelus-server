package com.near_reality.game.world.entity.player.bot.loadouts

import com.near_reality.game.world.entity.player.bot.*
import com.zenyte.game.item.Item
import com.zenyte.game.world.entity.player.container.impl.equipment.EquipmentSlot

object PureLoadout {
    val loadout = BotLoadout(
        type = BotType.PURE_BOT,
        name = "Pure Bot",
        equipment = mapOf(
            EquipmentSlot.HELMET to Item(4502, 1),
            EquipmentSlot.CAPE to Item(10499, 1),
            EquipmentSlot.AMULET to Item(6585, 1),
            EquipmentSlot.WEAPON to Item(4587, 1),
            EquipmentSlot.PLATE to Item(11335, 1),
            EquipmentSlot.SHIELD to Item(3844, 1),
            EquipmentSlot.LEGS to Item(2497, 1),
            EquipmentSlot.HANDS to Item(7462, 1),
            EquipmentSlot.BOOTS to Item(3105, 1),
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