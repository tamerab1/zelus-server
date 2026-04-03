package com.near_reality.game.world.entity.player.bot.loadouts

import com.near_reality.game.world.entity.player.bot.*
import com.zenyte.game.item.Item
import com.zenyte.game.world.entity.player.container.impl.equipment.EquipmentSlot

/**
 * Loadout configuration for the DH Bot (Dharok's set with dynamic weapon switching).
 *
 * @author Riskers
 */
object DHLoadout {

    val loadout = run {
        val specialWeapons = listOf(
            Item(13652, 1),
            Item(11802, 1),
            Item(27690, 1)
        )
        val selectedSpecialWeapon = specialWeapons.random()

        BotLoadout(
            type = BotType.DH_BOT,
            name = "DH Bot",
            equipment = mapOf(
                EquipmentSlot.HELMET to Item(4716, 1),
                EquipmentSlot.CAPE to Item(6570, 1),
                EquipmentSlot.AMULET to Item(6585, 1),
                EquipmentSlot.WEAPON to Item(4587, 1),
                EquipmentSlot.PLATE to Item(4720, 1),
                EquipmentSlot.SHIELD to Item(8846, 1),
                EquipmentSlot.LEGS to Item(4722, 1),
                EquipmentSlot.HANDS to Item(7462, 1),
                EquipmentSlot.BOOTS to Item(4131, 1),
                EquipmentSlot.RING to Item(2550, 1),
                EquipmentSlot.AMMUNITION to Item(892, 100)
            ),
            inventory = listOf(
                Item(385, 20),
                Item(2434, 3),
                Item(2436, 2),
                Item(2440, 1),
                Item(4718, 1),
                selectedSpecialWeapon
            ),
            specialAttackConfig = SpecialAttackConfig(
                weaponId = selectedSpecialWeapon.id,
                energyRequired = 50,
                targetHealthThreshold = 60,
                consecutiveSpecs = false,
                maxConsecutiveSpecs = 1,
                switchBackWeaponId = 4587,
                conditionalWeaponSwitches = listOf(
                    ConditionalWeaponSwitch(
                        condition = WeaponSwitchCondition.Custom { bot, target ->
                            val healthPercentage = (bot.getHitpoints() * 100) / bot.getMaxHitpoints()
                            val shouldUseDHAxe = healthPercentage <= 30 && (1..4).random() <= 3 && bot.shouldContinueUsingDhAxe()
                            shouldUseDHAxe
                        },
                        weaponId = 4718,
                        switchBackWeaponId = 4587,
                        priority = 15,
                        requiresTwoHanded = true
                    ),
                    ConditionalWeaponSwitch(
                        condition = WeaponSwitchCondition.Custom { bot, target ->
                            val healthPercentage = (bot.getHitpoints() * 100) / bot.getMaxHitpoints()
                            val shouldUseDHAxe = healthPercentage <= 50 && (1..2).random() == 1 && bot.shouldContinueUsingDhAxe()
                            shouldUseDHAxe
                        },
                        weaponId = 4718,
                        switchBackWeaponId = 4587,
                        priority = 10,
                        requiresTwoHanded = true
                    )
                )
            ),
            combatConfig = CombatConfig(
                maxCombatDistance = 10,
                retreatHealthPercentage = 15,
                eatHealthPercentage = 35,
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
}