package com.near_reality.game.world.entity.player.bot

import com.zenyte.game.item.Item

/**
 * Configuration for special attack behavior.
 * 
 * @author Riskers
 */
data class SpecialAttackConfig(
    val weaponId: Int,
    val energyRequired: Int,
    val targetHealthThreshold: Int,
    val consecutiveSpecs: Boolean,
    val maxConsecutiveSpecs: Int = 2,
    val switchBackWeaponId: Int? = null,
    
    // Dynamic weapon switching based on conditions
    val conditionalWeaponSwitches: List<ConditionalWeaponSwitch> = emptyList()
)

data class ConditionalWeaponSwitch(
    val condition: WeaponSwitchCondition,
    val weaponId: Int,
    val switchBackWeaponId: Int? = null,
    val priority: Int = 0, // Higher priority = checked first
    val requiresTwoHanded: Boolean = false // Whether this weapon requires both weapon and shield slots
) 