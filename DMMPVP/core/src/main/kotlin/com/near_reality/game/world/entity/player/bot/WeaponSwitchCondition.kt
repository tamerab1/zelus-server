package com.near_reality.game.world.entity.player.bot

import com.near_reality.game.world.entity.player.bot.PKBotPlayer
import com.zenyte.game.world.entity.player.Player
import com.zenyte.game.world.entity.player.SkillConstants

/**
 * Defines conditions for dynamic weapon switching.
 * 
 * @author Riskers
 */
sealed class WeaponSwitchCondition {
    data class HealthBelow(val percentage: Int) : WeaponSwitchCondition()
    data class TargetHealthBelow(val percentage: Int) : WeaponSwitchCondition()
    data class PrayerPointsBelow(val percentage: Int) : WeaponSwitchCondition()
    data class SpecialEnergyAbove(val percentage: Int) : WeaponSwitchCondition()
    data class InWilderness(val value: Boolean = true) : WeaponSwitchCondition()
    data class TargetDistance(val maxDistance: Int) : WeaponSwitchCondition()
    data class Custom(val predicate: (PKBotPlayer, Player?) -> Boolean) : WeaponSwitchCondition()
} 