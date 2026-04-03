package com.near_reality.game.world.entity.player.bot

/**
 * Configuration for combat behavior.
 * 
 * @author Riskers
 */
data class CombatConfig(
    val maxCombatDistance: Int = 10,
    val minCombatDistance: Int = 1,
    val retreatHealthPercentage: Int = 20,
    val eatHealthPercentage: Int = 50,
    val eatUntilHealthPercentage: Int = 70,
    val usePrayers: Boolean = true,
    val aggressiveMode: Boolean = true,
    val autoRetreat: Boolean = true,
    val autoEat: Boolean = true,
    val autoPrayer: Boolean = true,
    val autoWeaponSwitch: Boolean = true,
    val allowBotTargeting: Boolean = false // Allow bots to target other bots
) 