package com.near_reality.game.world.entity.player.bot

import com.zenyte.game.item.Item

/**
 * Configuration settings for PK bots.
 * 
 * @author Riskers
 */
data class BotConfiguration(
    val botType: BotType = BotType.MELEE_DDS,
    val loadout: BotLoadout? = null,
    
    val specialAttackConfig: SpecialAttackConfig? = null,
    
    val combatConfig: CombatConfig? = null,
    
    val maxCombatDistance: Int = 10,
    val minCombatDistance: Int = 1,
    val retreatHealthPercentage: Int = 20,
    val eatHealthPercentage: Int = 50,
    val eatUntilHealthPercentage: Int = 70,
    
    val usePrayers: Boolean = true,
    val usePiety: Boolean = true,
    val useOverheadPrayers: Boolean = true,
    val prayerSwitchDelay: Int = 3,
    
    val useSpecialAttacks: Boolean = true,
    val specialAttackChance: Int = 10,
    val specialAttackTargetHealth: Int = 50,
    val specialAttackWeapons: List<Int> = listOf(5698, 5699),
    val specialAttackEnergy: Int = 25,
    
    val foodItem: Int = 385,
    val foodAmount: Int = 28,
    val eatDelay: Int = 3,
    val foodCooldown: Int = 3,
    
    val weaponId: Int = 5698,
    val shieldId: Int = 1171,
    val helmetId: Int = 1165,
    val chestplateId: Int = 1127,
    val legsId: Int = 1079,
    val glovesId: Int = 7458,
    val bootsId: Int = 4131,
    val capeId: Int = 9768,
    val amuletId: Int = 6585,
    
    val weaponSets: Map<String, List<Item>> = mapOf(
        "melee" to listOf(
            Item(4587),
            Item(1171),
            Item(1165),
            Item(1127),
            Item(1079),
            Item(7458),
            Item(4131),
            Item(9768),
            Item(6585)
        ),
        "special" to listOf(
            Item(5698),
            Item(1171),
            Item(1165),
            Item(1127),
            Item(1079),
            Item(7458),
            Item(4131),
            Item(9768),
            Item(6585)
        )
    ),
    
    val prayerPotionId: Int = 2434,
    val prayerPotionAmount: Int = 3,
    val superCombatPotionId: Int = 12695,
    val superCombatPotionAmount: Int = 1,
    
    val aiTickDelay: Int = 1,
    val maxTargetDistance: Int = 15,
    val idleWanderRadius: Int = 5,
    
    val aggressiveMode: Boolean = true,
    val autoRetreat: Boolean = true,
    val autoEat: Boolean = true,
    val autoPrayer: Boolean = true,
    val autoWeaponSwitch: Boolean = true,
    val allowBotTargeting: Boolean = false,
    
    val eatCooldown: Int = 3,
    val weaponSwitchCooldown: Int = 5,
    val specialAttackCooldown: Int = 10,
    
    val dropBloodMoney: Boolean = true,
    val bloodMoneyAmount: Int = 25,
    val dropEquipment: Boolean = false
) {
    companion object {
        /**
         * Create configuration from a loadout
         */
        fun fromLoadout(loadout: BotLoadout): BotConfiguration {
            return BotConfiguration(
                botType = loadout.type,
                loadout = loadout,
                specialAttackConfig = loadout.specialAttackConfig,
                combatConfig = loadout.combatConfig,
                
                // Map loadout config to legacy properties for backward compatibility
                maxCombatDistance = loadout.combatConfig.maxCombatDistance,
                retreatHealthPercentage = loadout.combatConfig.retreatHealthPercentage,
                eatHealthPercentage = loadout.combatConfig.eatHealthPercentage,
                eatUntilHealthPercentage = loadout.combatConfig.eatUntilHealthPercentage,
                usePrayers = loadout.combatConfig.usePrayers,
                aggressiveMode = loadout.combatConfig.aggressiveMode,
                autoRetreat = loadout.combatConfig.autoRetreat,
                autoEat = loadout.combatConfig.autoEat,
                autoPrayer = loadout.combatConfig.autoPrayer,
                autoWeaponSwitch = loadout.combatConfig.autoWeaponSwitch,
                allowBotTargeting = loadout.combatConfig.allowBotTargeting,
                
                // Special attack properties
                useSpecialAttacks = true,
                specialAttackWeapons = listOf(loadout.specialAttackConfig.weaponId),
                specialAttackEnergy = loadout.specialAttackConfig.energyRequired,
                specialAttackTargetHealth = loadout.specialAttackConfig.targetHealthThreshold
            )
        }
        
        /**
         * Default configuration for melee PK bots (legacy)
         */
        val DEFAULT_MELEE_CONFIG = BotConfiguration()
        
        /**
         * Configuration for aggressive PK bots (legacy)
         */
        val AGGRESSIVE_CONFIG = BotConfiguration(
            aggressiveMode = true,
            maxCombatDistance = 15,
            retreatHealthPercentage = 15,
            specialAttackChance = 5
        )
        
        /**
         * Configuration for defensive PK bots (legacy)
         */
        val DEFENSIVE_CONFIG = BotConfiguration(
            aggressiveMode = false,
            retreatHealthPercentage = 40,
            eatHealthPercentage = 50,
            autoRetreat = true
        )
        
        /**
         * Configuration for special attack focused bots (legacy)
         */
        val SPECIAL_ATTACK_CONFIG = BotConfiguration(
            useSpecialAttacks = true,
            specialAttackChance = 3, // Higher chance
            specialAttackTargetHealth = 70, // Use spec earlier
            autoWeaponSwitch = true
        )
        
        /**
         * Configuration for bot vs bot scenarios
         */
        val BOT_VS_BOT_CONFIG = BotConfiguration(
            aggressiveMode = true,
            allowBotTargeting = true, // Allow bots to target other bots
            maxCombatDistance = 15,
            retreatHealthPercentage = 15,
            specialAttackChance = 5
        )
    }
} 