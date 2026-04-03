package com.near_reality.game.world.entity.player.bot

import com.zenyte.game.world.entity.player.Player
import com.zenyte.game.world.WorldThread
import com.zenyte.game.world.entity.Location
import com.near_reality.game.world.entity.player.bot.BotState
import com.zenyte.game.item.Item
import com.zenyte.game.content.consumables.Consumable
import com.zenyte.game.world.entity.player.container.impl.equipment.EquipmentSlot
import com.zenyte.game.world.entity.player.action.combat.PlayerCombat
import com.zenyte.game.world.entity.player.action.combat.SpecialType
import com.zenyte.plugins.`object`.WildernessDitchObject
import com.zenyte.game.world.`object`.WorldObject
import com.zenyte.game.world.entity.player.SkillConstants

/**
 * Handles the artificial intelligence for PK bots.
 * 
 * @author Riskers
 */
class PKBotAI(private val bot: PKBotPlayer) {
    
    private val combat = PKBotCombat(bot)
    private var lastEatTick = 0
    private var lastWeaponSwitchTick = 0
    private var lastSpecialAttackTick = 0
    private var lastWeaponSwitchBackTick = 0
    
    fun process() {
        if (bot.isDead() || bot.isDying()) {
            if (bot.getCurrentState() != BotState.RESPAWNING) {
                val killer = findKiller()
                bot.triggerDeath(killer)
                bot.setState(BotState.RESPAWNING)
            }
            return
        }
        
        if (shouldReturnToFightingLocation()) {
            handleReturnToFightingLocation()
            return
        }
        
        if (shouldDrinkPrayerPotion()) {
            handlePrayerPotion()
            return
        }
        
        if (shouldEat()) {
            handleEating()
            return
        }
        
        when (bot.getCurrentState()) {
            BotState.IDLE -> handleIdle()
            BotState.COMBAT -> handleCombat()
            BotState.EATING -> handleEating()
            BotState.PRAYER_SWITCHING -> handlePrayerSwitching()
            BotState.MOVING -> handleMoving()
            BotState.RETREATING -> handleRetreating()
            BotState.WEAPON_SWITCHING -> handleWeaponSwitching()
            BotState.RESPAWNING -> handleRespawn()
            BotState.RESTOCKING -> handleRestock()
            BotState.RETURNING -> handleReturnToFightingLocation()
            BotState.CROSSING_DITCH -> handleCrossingDitch()
        }
    }
    
    private fun shouldEat(): Boolean {
        val currentTick = WorldThread.getCurrentCycle().toInt()
        if (currentTick - lastEatTick < bot.getConfiguration().eatCooldown) {
            return false
        }
        
        val currentHealth = bot.getSkills().getLevel(com.zenyte.game.world.entity.player.Skills.HITPOINTS)
        val maxHealth = bot.getSkills().getLevelForXp(com.zenyte.game.world.entity.player.Skills.HITPOINTS)
        val healthPercentage = (currentHealth * 100) / maxHealth
        
        val loadout = bot.getLoadout()
        val eatHealthPercentage = loadout?.combatConfig?.eatHealthPercentage ?: bot.getConfiguration().eatHealthPercentage
        
        return healthPercentage <= eatHealthPercentage
    }
    
    private fun shouldContinueEating(): Boolean {
        val currentHealth = bot.getSkills().getLevel(com.zenyte.game.world.entity.player.Skills.HITPOINTS)
        val maxHealth = bot.getSkills().getLevelForXp(com.zenyte.game.world.entity.player.Skills.HITPOINTS)
        val healthPercentage = (currentHealth * 100) / maxHealth
        
        val loadout = bot.getLoadout()
        val eatUntilHealthPercentage = loadout?.combatConfig?.eatUntilHealthPercentage ?: bot.getConfiguration().eatUntilHealthPercentage
        
        return healthPercentage < eatUntilHealthPercentage
    }
    
    private fun handleIdle() {
        deactivateCombatPrayers()
        
        val attacker = findAttacker()
        if (attacker != null) {
            sendRandomPkMessage()
            
            lastTargetTime = 0L
            bot.setTarget(attacker)
            bot.setState(BotState.COMBAT)
            
            activateCombatPrayers(attacker)
        } else {
            val nearbyTarget = findNearbyTarget()
            if (nearbyTarget != null) {
                sendRandomPkMessage()
                
                lastTargetTime = 0L
                bot.setTarget(nearbyTarget)
                bot.setState(BotState.COMBAT)
                
                activateCombatPrayers(nearbyTarget)
            } else {
                if (shouldRestockAfterTimeout()) {
                    teleportToRestock()
                    return
                }

                // ▓▓▓ RANDOM MOVEMENT (NEW)
                if ((0..20).random() == 0) {
                    // 1x per 20 ticks, anders te veel beweging
                    wanderRandomly()
                }



            if (lastTargetTime != 0L) {
                    val currentTime = WorldThread.getCurrentCycle()
                    val timeSinceLastTarget = currentTime - lastTargetTime
                    val secondsElapsed = timeSinceLastTarget / 600
                    if (secondsElapsed.toInt() % 10 == 0 && secondsElapsed > 0) {
                    }
                }
            }
        }
    }
    
    private fun handleCombat() {
        if (bot.isDead() || bot.isDying()) {
            val lastTarget = bot.getTarget()
            if (lastTarget != null) {
                if (lastTarget.getAttacking() == bot.getFakePlayer()) {
                    lastTarget.setAttacking(null)
                }
                lastTarget.lastTarget = (null)
                bot.setTarget(null)
            }
            
            val fakePlayer = bot.getFakePlayer()
            if (fakePlayer.getAttacking() != null) {
                fakePlayer.setAttacking(null)
            }
            
            bot.setState(BotState.RESPAWNING)
            return
        }
        
        val attacker = findAttacker()
        if (attacker != null) {
            val currentTarget = bot.getTarget()
            if (currentTarget != attacker) {
                
                val fakePlayer = bot.getFakePlayer()
                if (fakePlayer.getAttacking() != null) {
                    fakePlayer.setAttacking(null)
                }
                
                bot.setTarget(attacker)
                activateCombatPrayers(attacker)
            }
        }
        
        val target = bot.getTarget()
        if (target == null) {
            if (lastTargetTime == 0L) {
                lastTargetTime = WorldThread.getCurrentCycle()
            }
            bot.setState(BotState.IDLE)
            return
        }
        
        if (!isTargetStillValid(target)) {
            val distance = bot.getLocation().getDistance(target.location)
            if (distance > 15) {
                bot.setTarget(null)
                teleportToRestock()
                return
            }
            
            if (target.attacking != null && target.attacking != bot.getFakePlayer()) {
                val attackingPlayer = target.attacking
                if (attackingPlayer is Player) {
                } else {
                }
            }
            
            bot.setTarget(null)
            
            val fakePlayer = bot.getFakePlayer()
            if (fakePlayer.getAttacking() != null) {
                fakePlayer.setAttacking(null)
            }
            
            val newTarget = findAttacker() ?: findNearbyTarget()
            if (newTarget != null) {
                bot.setTarget(newTarget)
                bot.setState(BotState.COMBAT)
                activateCombatPrayers(newTarget)
                return
            } else {
                if (lastTargetTime == 0L) {
                    lastTargetTime = WorldThread.getCurrentCycle()
                } else {
                }
                bot.setState(BotState.IDLE)
                return
            }
        }
        
        if (lastTargetTime != 0L) {
            lastTargetTime = 0L
        }
        
        activateCombatPrayers(target)
        
        if (shouldSwitchWeapon(target)) {
            handleWeaponSwitching()
        }
        
        combat.processCombat()
    }

    private fun wanderRandomly() {
        val radius = bot.getConfiguration().idleWanderRadius

        // Random stap -2..2 in beide richtingen
        val dx = (-2..2).random()
        val dy = (-2..2).random()

        if (dx == 0 && dy == 0) return

        val current = bot.getLocation()
        val nextX = current.x + dx
        val nextY = current.y + dy

        val fake = bot.getFakePlayer()

        // Zorg dat het pad geldig is
        if (!fake.addWalkSteps(nextX, nextY, 1, true)) {
            return
        }

        bot.setState(BotState.MOVING)
    }

    private fun isTargetStillValid(target: Player): Boolean {
        if (failedTargets.contains(target.username)) {
            return false
        }
        
        if (com.zenyte.game.world.World.getPlayerByUsername(target.username) == null) {
            return false
        }
        
        if (target.isDead || target.isDying) {
            return false
        }
        
        if (isPlayerBot(target)) {
            val targetBot = com.near_reality.game.world.entity.player.bot.PKBotPlayer.getBotByFakePlayer(target as com.near_reality.game.world.entity.player.FakePlayer)
            if (targetBot != null && !shouldTargetBot(targetBot)) {
                return false
            }
        }
        
        if (target.attacking != null && target.attacking != bot.getFakePlayer()) {
            val attackingPlayer = target.attacking
            if (attackingPlayer is Player) {
            } else {
            }
            return false
        }
        
        val distance = bot.getLocation().getDistance(target.location)
        if (distance > 15) {
            return false
        }
        
        if (bot.getLocation().plane != target.location.plane) {
            return false
        }
        
        val botRegion = bot.getLocation().regionId
        val targetRegion = target.location.regionId
        if (botRegion != targetRegion && distance > 10) {
            return false
        }
        
        if (target.location.x > 10000 || target.location.y > 10000) {
            addToFailedTargets(target.username)
            return false
        }
        
        if (target.location.x < 0 || target.location.y < 0 || target.location.x > 10000 || target.location.y > 10000) {
            addToFailedTargets(target.username)
            return false
        }
        
        return true
    }
    
    private fun areValidCoordinates(x: Int, y: Int): Boolean {
        return x >= 0 && y >= 0 && x <= 10000 && y <= 10000
    }
    
    private fun addToFailedTargets(username: String) {
        failedTargets.add(username)
        
        com.zenyte.game.task.WorldTasksManager.schedule((FAILED_TARGET_TIMEOUT / 600).toInt()) {
            failedTargets.remove(username)
        }
    }
    
    private fun isUsingSmitePrayer(): Boolean {
        try {
            val prayerManager = bot.getFakePlayer().getPrayerManager()
            return prayerManager.isActive(com.zenyte.game.content.skills.prayer.Prayer.SMITE)
        } catch (e: Exception) {
            return false
        }
    }
    
    private fun isUsingProtectItemPrayer(): Boolean {
        try {
            val prayerManager = bot.getFakePlayer().getPrayerManager()
            return prayerManager.isActive(com.zenyte.game.content.skills.prayer.Prayer.PROTECT_ITEM)
        } catch (e: Exception) {
            return false
        }
    }
    
    fun activateCombatPrayers(target: Player) {
        val loadout = bot.getLoadout()
        val usePrayers = loadout?.combatConfig?.usePrayers ?: bot.getConfiguration().usePrayers
        
        if (!usePrayers) {
            return
        }
        
        val currentPrayerPoints = bot.getPrayerPoints()
        val maxPrayerPoints = bot.getSkills().getLevelForXp(com.zenyte.game.world.entity.player.SkillConstants.PRAYER)
        val prayerPercentage = (currentPrayerPoints * 100) / maxPrayerPoints
        
        activateSmitePrayer()
        activateProtectItemPrayer()
    }
    
    private fun handleEating() {
        val config = bot.getConfiguration()
        val currentTick = WorldThread.getCurrentCycle().toInt()
        
        if (!shouldContinueEating()) {
            bot.setState(BotState.COMBAT)
            return
        }
        
        if (currentTick - lastEatTick < config.eatCooldown) {
            bot.setState(BotState.COMBAT)
            return
        }
        
        val inventory = bot.getInventory()
        var foodFound = false
        
        val loadout = bot.getLoadout()
        val foodItem = loadout?.inventory?.find { it.id == 385 }?.id ?: config.foodItem
        
        for (i in 0 until inventory.getContainer().getSize()) {
            val item = inventory.getContainer().get(i)
            if (item != null && item.id == foodItem) {
                val consumable = Consumable.consumables.get(item.id)
                if (consumable != null) {
                    try {
                        consumable.consume(bot.getFakePlayer(), item, i)
                        val currentHealth = bot.getHitpoints()
                        val maxHealth = bot.getMaxHitpoints()
                        val healthPercentage = (currentHealth * 100) / maxHealth
                        lastEatTick = currentTick
                        foodFound = true
                        break
                    } catch (e: Exception) {
                    }
                }
            }
        }
        
        if (!foodFound) {
            bot.setState(BotState.COMBAT)
            return
        }
        
        bot.setState(BotState.EATING)
    }
    
    private fun shouldDrinkPrayerPotion(): Boolean {
        val config = bot.getConfiguration()
        if (!config.usePrayers) return false
        
        val currentPrayerPoints = bot.getPrayerPoints()
        val maxPrayerPoints = bot.getSkills().getLevelForXp(com.zenyte.game.world.entity.player.SkillConstants.PRAYER)
        val prayerPercentage = (currentPrayerPoints * 100) / maxPrayerPoints
        
        return prayerPercentage <= 30
    }
    
    private fun handlePrayerPotion() {
        val config = bot.getConfiguration()
        val inventory = bot.getInventory()
        
        val loadout = bot.getLoadout()
        val prayerPotionId = loadout?.inventory?.find { it.id == 2434 }?.id ?: config.prayerPotionId
        
        for (i in 0 until inventory.getContainer().getSize()) {
            val item = inventory.getContainer().get(i)
            if (item != null && item.id == prayerPotionId) {
                val consumable = Consumable.consumables.get(item.id)
                if (consumable != null) {
                    try {
                        consumable.consume(bot.getFakePlayer(), item, i)
                        val currentPrayerPoints = bot.getPrayerPoints()
                        val maxPrayerPoints = bot.getSkills().getLevelForXp(com.zenyte.game.world.entity.player.SkillConstants.PRAYER)
                        val prayerPercentage = (currentPrayerPoints * 100) / maxPrayerPoints
                        break
                    } catch (e: Exception) {
                    }
                }
            }
        }
    }
    
    fun shouldUseSpecialAttack(target: Player): Boolean {
        val config = bot.getConfiguration()
        val specialConfig = config.specialAttackConfig ?: return false
        
        if (!config.useSpecialAttacks) return false
        
        val hasSpecialWeapon = bot.getInventory().containsItem(specialConfig.weaponId)
        if (!hasSpecialWeapon) {
            return false
        }
        
        val specialEnergy = bot.getCombatDefinitions().getSpecialEnergy()
        if (specialEnergy < specialConfig.energyRequired) {
            return false
        }
        
        val targetHealth = target.getHitpoints()
        val targetMaxHealth = target.getMaxHitpoints()
        val targetHealthPercentage = (targetHealth * 100) / targetMaxHealth
        
        if (targetHealthPercentage > 60) {
            bot.resetSpecialAttackSession()
        }
        
        val maxPossibleSpecials = specialEnergy / specialConfig.energyRequired
        val maxAllowedSpecials = if (specialConfig.consecutiveSpecs) 2 else 1
        val plannedSpecials = minOf(maxPossibleSpecials, maxAllowedSpecials)
        
        val currentSpecialsUsed = bot.getConsecutiveSpecialAttacks()
        
        if (currentSpecialsUsed >= plannedSpecials) {
            return false
        }
        
        val hpThreshold = if (plannedSpecials == 1) {
            40
        } else {
            50
        }
        
        if (currentSpecialsUsed == 0 && targetHealthPercentage > hpThreshold) {
            return false
        }
        
        if (currentSpecialsUsed > 0 && targetHealthPercentage > 20) {
            return false
        }
        
        val shouldSpec = (1..5).random() == 1
        if (shouldSpec) {
        }
        return shouldSpec
    }
    
    private fun shouldSwitchWeapon(target: Player): Boolean {
        val config = bot.getConfiguration()
        val specialConfig = config.specialAttackConfig ?: return false
        
        val conditionalSwitches = specialConfig.conditionalWeaponSwitches
            .sortedByDescending { it.priority }
        
        for (switch in conditionalSwitches) {
            if (shouldApplyWeaponSwitch(switch, target)) {
                return true
            }
        }
        
        return shouldSwitchForSpecialAttack(target)
    }
    
    private fun shouldApplyWeaponSwitch(switch: ConditionalWeaponSwitch, target: Player?): Boolean {
        val currentWeapon = bot.getEquipment().getItem(EquipmentSlot.WEAPON)
        
        if (currentWeapon?.id == switch.weaponId) {
            return false
        }
        
        return when (val condition = switch.condition) {
            is WeaponSwitchCondition.HealthBelow -> {
                val healthPercentage = (bot.getHitpoints() * 100) / bot.getMaxHitpoints()
                healthPercentage <= condition.percentage
            }
            is WeaponSwitchCondition.TargetHealthBelow -> {
                target?.let { targetPlayer ->
                    val targetHealthPercentage = (targetPlayer.getHitpoints() * 100) / targetPlayer.getMaxHitpoints()
                    targetHealthPercentage <= condition.percentage
                } ?: false
            }
            is WeaponSwitchCondition.PrayerPointsBelow -> {
                val prayerPercentage = (bot.getPrayerPoints() * 100) / bot.getSkills().getLevelForXp(SkillConstants.PRAYER)
                prayerPercentage <= condition.percentage
            }
            is WeaponSwitchCondition.SpecialEnergyAbove -> {
                val specialEnergy = bot.getCombatDefinitions().getSpecialEnergy()
                specialEnergy >= condition.percentage
            }
            is WeaponSwitchCondition.InWilderness -> {
                val location = bot.getLocation()
                val isInWilderness = location.y >= 3520 && location.y <= 3967 && 
                                   location.x >= 2944 && location.x <= 3391
                isInWilderness == condition.value
            }
            is WeaponSwitchCondition.TargetDistance -> {
                target?.let { targetPlayer ->
                    val distance = bot.getLocation().getDistance(targetPlayer.location)
                    distance <= condition.maxDistance
                } ?: false
            }
            is WeaponSwitchCondition.Custom -> {
                condition.predicate(bot, target)
            }
        }
    }
    
    private fun shouldSwitchForSpecialAttack(target: Player): Boolean {
        val config = bot.getConfiguration()
        val specialConfig = config.specialAttackConfig ?: return false
        
        val currentTick = WorldThread.getCurrentCycle().toInt()
        
        if (currentTick - lastWeaponSwitchBackTick < 4) {
            return false
        }
        
        val currentWeapon = bot.getEquipment().getItem(EquipmentSlot.WEAPON)
        
        if (currentWeapon?.id == specialConfig.weaponId) {
            val targetHealth = target.getHitpoints()
            val targetMaxHealth = target.getMaxHitpoints()
            val targetHealthPercentage = (targetHealth * 100) / targetMaxHealth
            val specialEnergy = bot.getCombatDefinitions().getSpecialEnergy()
            val hasSpecialWeapon = bot.getInventory().containsItem(specialConfig.weaponId)
            val currentSpecialsUsed = bot.getConsecutiveSpecialAttacks()
            
            val maxPossibleSpecials = specialEnergy / specialConfig.energyRequired
            val maxAllowedSpecials = if (specialConfig.consecutiveSpecs) 2 else 1
            val plannedSpecials = minOf(maxPossibleSpecials, maxAllowedSpecials)
            
            if (currentSpecialsUsed < plannedSpecials) {
                return false
            }
            
            val hpThreshold = if (plannedSpecials == 1) 40 else 50
            val shouldSwitchBack = (currentSpecialsUsed == 0 && targetHealthPercentage > hpThreshold) ||
                                 (currentSpecialsUsed > 0 && targetHealthPercentage > 20) ||
                                 specialEnergy < specialConfig.energyRequired ||
                                 !hasSpecialWeapon
            
            if (shouldSwitchBack) {
                lastWeaponSwitchBackTick = currentTick
                return true
            }
        }
        
        return false
    }
    
    private fun shouldSwitchBackToPrimaryWeapon(target: Player): Boolean {
        val config = bot.getConfiguration()
        val specialConfig = config.specialAttackConfig ?: return false
        
        val currentWeapon = bot.getEquipment().getItem(EquipmentSlot.WEAPON)
        val primaryWeaponId = specialConfig.switchBackWeaponId ?: 4587
        
        if (currentWeapon?.id != primaryWeaponId) {
            val conditionalSwitches = specialConfig.conditionalWeaponSwitches
            for (switch in conditionalSwitches) {
                if (currentWeapon?.id == switch.weaponId) {
                    if (!shouldApplyWeaponSwitch(switch, target)) {
                        return true
                    }
                }
            }
        }
        
        return false
    }
    
    fun handleSpecialAttack(target: Player) {
        val config = bot.getConfiguration()
        val specialConfig = config.specialAttackConfig ?: return
        
        try {
            val currentTick = WorldThread.getCurrentCycle().toInt()
            
            if (!bot.shouldDoConsecutiveSpecialAttack()) {
                bot.resetSpecialAttackTracking()
            }
            
            val equipment = bot.getEquipment()
            equipment.set(EquipmentSlot.WEAPON, Item(specialConfig.weaponId, 1))
            equipment.refresh()
            
            bot.getCombatDefinitions().setSpecial(true, false)
            
            lastWeaponSwitchBackTick = currentTick
            
        } catch (e: Exception) {
        }
        
        bot.setState(BotState.COMBAT)
    }
    
    private fun handleSwitchBackToScimitar() {
        try {
            val loadout = bot.getLoadout()
            val switchBackWeaponId = loadout?.specialAttackConfig?.switchBackWeaponId ?: 4587
            
            val equipment = bot.getEquipment()
            equipment.set(EquipmentSlot.WEAPON, Item(switchBackWeaponId, 1))
            equipment.refresh()
            
            bot.getCombatDefinitions().setSpecial(false, false)
            
        } catch (e: Exception) {
        }
    }
    
    fun extendWeaponSwitchCooldown() {
        val currentTick = WorldThread.getCurrentCycle().toInt()
        lastWeaponSwitchBackTick = currentTick + 4
    }
    

    
    private fun handleWeaponSwitching() {
        val target = bot.getTarget()
        if (target == null) return
        
        if (shouldSwitchWeapon(target)) {
            handleSwitchToConditionalWeapon(target)
        } else if (shouldSwitchBackToPrimaryWeapon(target)) {
            handleSwitchBackToPrimaryWeapon()
        }
    }
    
    private fun handleSwitchToConditionalWeapon(target: Player) {
        val config = bot.getConfiguration()
        val specialConfig = config.specialAttackConfig ?: return
        
        val switch = specialConfig.conditionalWeaponSwitches
            .sortedByDescending { it.priority }
            .find { shouldApplyWeaponSwitch(it, target) }
        
        if (switch != null) {
            try {
                val equipment = bot.getEquipment()
                
                if (switch.requiresTwoHanded) {
                    equipment.set(EquipmentSlot.WEAPON, Item(switch.weaponId, 1))
                    equipment.set(EquipmentSlot.SHIELD, null)
                } else {
                    equipment.set(EquipmentSlot.WEAPON, Item(switch.weaponId, 1))
                }
                
                equipment.refresh()
                
            } catch (e: Exception) {
            }
        }
    }
    
    private fun handleSwitchBackToPrimaryWeapon() {
        val config = bot.getConfiguration()
        val specialConfig = config.specialAttackConfig ?: return
        
        try {
            val equipment = bot.getEquipment()
            val primaryWeaponId = specialConfig.switchBackWeaponId ?: 4587
            
            val currentWeapon = equipment.getItem(EquipmentSlot.WEAPON)
            val isTwoHandedWeapon = currentWeapon?.id == 4718
            
            if (isTwoHandedWeapon) {
                equipment.set(EquipmentSlot.WEAPON, Item(primaryWeaponId, 1))
                equipment.set(EquipmentSlot.SHIELD, Item(8846, 1))
            } else {
                equipment.set(EquipmentSlot.WEAPON, Item(primaryWeaponId, 1))
            }
            
            equipment.refresh()
            
        } catch (e: Exception) {
        }
    }
    
    private fun equipWeaponSet(setName: String) {
        val config = bot.getConfiguration()
        val weaponSet = config.weaponSets[setName]
        
        if (weaponSet != null) {
            try {
                val equipment = bot.getEquipment()
                
                weaponSet.forEachIndexed { index, item ->
                    when (index) {
                        0 -> equipment.set(EquipmentSlot.WEAPON, item)
                        1 -> equipment.set(EquipmentSlot.SHIELD, item)
                        2 -> equipment.set(EquipmentSlot.HELMET, item)
                        3 -> equipment.set(EquipmentSlot.PLATE, item)
                        4 -> equipment.set(EquipmentSlot.LEGS, item)
                        5 -> equipment.set(EquipmentSlot.HANDS, item)
                        6 -> equipment.set(EquipmentSlot.BOOTS, item)
                        7 -> equipment.set(EquipmentSlot.CAPE, item)
                        8 -> equipment.set(EquipmentSlot.AMULET, item)
                    }
                }
                
                equipment.refresh()
            } catch (e: Exception) {
            }
        }
    }
    
    private fun handlePrayerSwitching() {
        val target = bot.getTarget()
        if (target != null) {
            activateSmitePrayer()
            activateProtectItemPrayer()
        }
        
        bot.setState(BotState.COMBAT)
    }
    
    private fun hasEnoughPrayerPoints(): Boolean {
        val currentPrayerPoints = bot.getPrayerPoints()
        return currentPrayerPoints > 0
    }
    
    private fun activateSmitePrayer() {
        if (!hasEnoughPrayerPoints()) {
            return
        }
        
        try {
            val prayerManager = bot.getFakePlayer().getPrayerManager()
            if (!prayerManager.isActive(com.zenyte.game.content.skills.prayer.Prayer.SMITE)) {
                prayerManager.activatePrayer(com.zenyte.game.content.skills.prayer.Prayer.SMITE)
            }
        } catch (e: Exception) {
        }
    }
    
    private fun activateProtectItemPrayer() {
        if (!hasEnoughPrayerPoints()) {
            return
        }
        
        try {
            val prayerManager = bot.getFakePlayer().getPrayerManager()
            if (!prayerManager.isActive(com.zenyte.game.content.skills.prayer.Prayer.PROTECT_ITEM)) {
                prayerManager.activatePrayer(com.zenyte.game.content.skills.prayer.Prayer.PROTECT_ITEM)
            }
        } catch (e: Exception) {
        }
    }
    
    private fun deactivateCombatPrayers() {
        try {
            val prayerManager = bot.getFakePlayer().getPrayerManager()
            
            if (prayerManager.isActive(com.zenyte.game.content.skills.prayer.Prayer.SMITE)) {
                prayerManager.deactivatePrayer(com.zenyte.game.content.skills.prayer.Prayer.SMITE)
            }
            
            if (prayerManager.isActive(com.zenyte.game.content.skills.prayer.Prayer.PROTECT_ITEM)) {
                prayerManager.deactivatePrayer(com.zenyte.game.content.skills.prayer.Prayer.PROTECT_ITEM)
            }
        } catch (e: Exception) {
        }
    }
    
    private fun handleMoving() {
        val target = bot.getTarget()
        if (target != null) {
            val distance = bot.getLocation().getDistance(target.location)
            val config = bot.getConfiguration()
            
            if (distance > config.maxCombatDistance) {
                moveTowardsTarget(target)
            } else if (distance < config.minCombatDistance) {
                moveAwayFromTarget(target)
            }
        }
        
        bot.setState(BotState.COMBAT)
    }
    
    private fun handleRetreating() {
        val currentHealth = bot.getSkills().getLevel(com.zenyte.game.world.entity.player.Skills.HITPOINTS)
        val maxHealth = bot.getSkills().getLevelForXp(com.zenyte.game.world.entity.player.Skills.HITPOINTS)
        val healthPercentage = (currentHealth * 100) / maxHealth
        
        val config = bot.getConfiguration()
        if (healthPercentage > config.retreatHealthPercentage) {
            bot.setState(BotState.COMBAT)
        } else {
            retreatFromCombat()
        }
    }
    
    private fun moveTowardsTarget(target: Player) {
        val dx = target.location.x - bot.getLocation().x
        val dy = target.location.y - bot.getLocation().y
        
        val nextX = bot.getLocation().x + if (dx > 0) 1 else if (dx < 0) -1 else 0
        val nextY = bot.getLocation().y + if (dy > 0) 1 else if (dy < 0) -1 else 0
        
        val fakePlayer = bot.getFakePlayer()
        if (!fakePlayer.addWalkSteps(nextX, nextY, 1, true)) {
            return
        }
    }
    
    private fun moveAwayFromTarget(target: Player) {
        val dx = bot.getLocation().x - target.location.x
        val dy = bot.getLocation().y - target.location.y
        
        val nextX = bot.getLocation().x + if (dx > 0) 1 else if (dx < 0) -1 else 0
        val nextY = bot.getLocation().y + if (dy > 0) 1 else if (dy < 0) -1 else 0
        
        val fakePlayer = bot.getFakePlayer()
        if (!fakePlayer.addWalkSteps(nextX, nextY, 1, true)) {
            return
        }
    }
    
    private fun retreatFromCombat() {
        val target = bot.getTarget()
        if (target != null) {
            moveAwayFromTarget(target)
        }
    }

    private fun findAttacker(): Player? {
        val allPlayers = com.zenyte.game.world.World.getPlayers()
        
        for (player in allPlayers) {
            if (player.username == bot.getUsername()) {
                continue
            }
            
            if (player.isDead || player.isFinished) {
                continue
            }
            
            if (failedTargets.contains(player.username)) {
                continue
            }
            
            if (!areValidCoordinates(player.location.x, player.location.y)) {
                addToFailedTargets(player.username)
                continue
            }
            
            if (player.attacking != null && player.attacking != bot.getFakePlayer()) {
                continue
            }
            
            if (isPlayerBot(player)) {
                val targetBot = com.near_reality.game.world.entity.player.bot.PKBotPlayer.getBotByFakePlayer(player as com.near_reality.game.world.entity.player.FakePlayer)
                if (targetBot != null && !shouldTargetBot(targetBot)) {
                    if ((System.currentTimeMillis() / 1000) % 10 == 0L) {
                    }
                    continue
                }
            }
            
            if (player.attacking == bot.getFakePlayer()) {
                if (isTargetStillValid(player)) {
                    return player
                } else {
                    player.attacking = null
                    if (!areValidCoordinates(player.location.x, player.location.y)) {
                        addToFailedTargets(player.username)
                    }
                }
            }
        }
        
        return null
    }
    
    private fun findNearbyTarget(): Player? {
        val loadout = bot.getLoadout()
        val aggressiveMode = loadout?.combatConfig?.aggressiveMode ?: bot.getConfiguration().aggressiveMode
        
        if (!aggressiveMode) {
            return null
        }
        
        val allPlayers = com.zenyte.game.world.World.getPlayers()
        val botLocation = bot.getLocation()
        
        for (player in allPlayers) {
            if (player.username == bot.getUsername()) {
                continue
            }
            
            if (player.isDead || player.isFinished) {
                continue
            }
            
            if (failedTargets.contains(player.username)) {
                continue
            }
            
            if (!areValidCoordinates(player.location.x, player.location.y)) {
                addToFailedTargets(player.username)
                continue
            }
            
            if (player.attacking != null && player.attacking != bot.getFakePlayer()) {
                continue
            }
            
            if (isPlayerBot(player)) {
                val targetBot = com.near_reality.game.world.entity.player.bot.PKBotPlayer.getBotByFakePlayer(player as com.near_reality.game.world.entity.player.FakePlayer)
                if (targetBot != null && !shouldTargetBot(targetBot)) {
                    if ((System.currentTimeMillis() / 1000) % 10 == 0L) {
                    }
                    continue
                }
            }
            
            val distance = botLocation.getDistance(player.location)
            if (distance <= 1) {
                if (isTargetStillValid(player)) {
                    return player
                } else {
                    if (!areValidCoordinates(player.location.x, player.location.y)) {
                        addToFailedTargets(player.username)
                    }
                }
            }
        }
        
        return null
    }
    
    private fun findKiller(): com.zenyte.game.world.entity.Entity? {
        val fakePlayer = bot.getFakePlayer()
        
        return try {
            fakePlayer.getMostDamagePlayer()
        } catch (e: Exception) {
            findAttacker()
        }
    }
    
    private fun getWaypoints(): List<Location> {
        val botType = bot.getBotType()
        val targetLocation = when (botType) {
            BotType.MELEE_DDS -> Location(3093, 3536, 0)
            BotType.MELEE_WHIP -> Location(3094, 3536, 0)
            BotType.DH_BOT -> Location(3095, 3536, 0)
            else -> Location(3093, 3536, 0)
        }
        
        return listOf(
            Location(3095, 3499, 0),
            Location(3096, 3512, 0),
            Location(3100, 3520, 0),
            targetLocation
        )
    }
    
    private var currentWaypointIndex = 0
    private var hasCrossedDitch = false
    
    private var lastTargetTime = 0L
    private val RESTOCK_TIMER = 60 * 600L
    
    private val failedTargets = mutableSetOf<String>()
    private val FAILED_TARGET_TIMEOUT = 30 * 600L
    
    private fun isPlayerBot(player: Player): Boolean {
        if (player is com.near_reality.game.world.entity.player.FakePlayer) {
            val bot = com.near_reality.game.world.entity.player.bot.PKBotPlayer.getBotByFakePlayer(player)
            return bot != null
        }
        return false
    }
    
    private fun shouldTargetBot(targetBot: com.near_reality.game.world.entity.player.bot.PKBotPlayer): Boolean {
        val loadout = bot.getLoadout()
        val allowBotTargeting = loadout?.combatConfig?.allowBotTargeting ?: bot.getConfiguration().allowBotTargeting
        return allowBotTargeting
    }
    
    private fun shouldReturnToFightingLocation(): Boolean {
        val currentLocation = bot.getLocation()
        val respawnLocation = Location(3105, 3494, 0)
        val waypoints = getWaypoints()
        val fightingLocation = waypoints.last()
        
        val atRespawnLocation = currentLocation.getDistance(respawnLocation) <= 2
        val atFightingLocation = currentLocation.getDistance(fightingLocation) <= 2
        
        return atRespawnLocation && !atFightingLocation
    }
    
    private fun shouldCrossWildernessDitch(): Boolean {
        val currentLocation = bot.getLocation()
        
        val ditchLocation = Location(3100, 3520, 0)
        val distanceToDitch = currentLocation.getDistance(ditchLocation)
        
        return distanceToDitch <= 3 && currentWaypointIndex >= 2 && !hasCrossedDitch
    }
    
    private fun crossWildernessDitch() {
        try {
            val fakePlayer = bot.getFakePlayer()
            val currentLocation = bot.getLocation()
            
            bot.setState(BotState.CROSSING_DITCH)
            
            val possibleDitchLocations = listOf(
                Location(3100, 3520, 0),
                Location(3100, 3521, 0),
                Location(3100, 3519, 0),
                Location(3099, 3520, 0),
                Location(3101, 3520, 0),
                Location(3100, 3522, 0),
                Location(3100, 3518, 0)
            )
            
            var ditchObject: WorldObject? = null
            var foundLocation: Location? = null
            
            for (location in possibleDitchLocations) {
                val obj = com.zenyte.game.world.World.getObjectWithType(location, 10)
                if (obj != null) {
                    ditchObject = obj
                    foundLocation = location
                    break
                }
            }
            
            if (ditchObject != null) {
                if (fakePlayer.getY() > 3521) {
                    fakePlayer.getTemporaryAttributes().put("EnteredWildernessDitch", true)
                    
                    WildernessDitchObject.jump(fakePlayer, ditchObject)
                } else {
                    WildernessDitchObject.jump(fakePlayer, ditchObject)
                }
                
                com.zenyte.game.task.WorldTasksManager.schedule(3) {
                    hasCrossedDitch = true
                    
                    currentWaypointIndex++
                    
                    bot.setState(BotState.RETURNING)
                }
            } else {
                bot.setState(BotState.RETURNING)
            }
        } catch (e: Exception) {
            bot.setState(BotState.RETURNING)
        }
    }
    
    private fun handleCrossingDitch() {
    }
    
    private fun handleReturnToFightingLocation() {
        if (isBotMoving()) {
            return
        }
        
        val currentLocation = bot.getLocation()
        val waypoints = getWaypoints()
        
        if (shouldCrossWildernessDitch()) {
            crossWildernessDitch()
            return
        }
        
        if (currentWaypointIndex >= waypoints.size) {
            bot.setState(BotState.IDLE)
            currentWaypointIndex = 0
            return
        }
        
        val currentWaypoint = waypoints[currentWaypointIndex]
        
        if (currentLocation.getDistance(currentWaypoint) <= 2) {
            currentWaypointIndex++
            
            if (currentWaypointIndex < waypoints.size) {
                val nextWaypoint = waypoints[currentWaypointIndex]
                pathfindToLocation(nextWaypoint)
            }
        } else {
            pathfindToLocation(currentWaypoint)
        }
        
        bot.setState(BotState.RETURNING)
    }
    
    private fun handleRespawn() {
        bot.setTarget(null)
        
        bot.setState(BotState.RESPAWNING)
        
        val fakePlayer = bot.getFakePlayer()
        
        fakePlayer.animation = com.zenyte.game.world.entity.masks.Animation(714)
        fakePlayer.graphics = com.zenyte.game.world.entity.masks.Graphics(308, 0, 100)
        
        com.zenyte.game.task.WorldTasksManager.schedule(3) {
            val respawnLocation = com.zenyte.game.world.entity.Location(3105, 3494, 0)
            bot.setLocation(respawnLocation)
            
            fakePlayer.animation = null
            fakePlayer.graphics = null
            
            performRestock()
        }
    }
    
    private fun handleRestock() {
        bot.restockAfterDeath()
    }
    
    private fun isBotMoving(): Boolean {
        val fakePlayer = bot.getFakePlayer()
        return fakePlayer.hasWalkSteps()
    }
    
    private fun pathfindToLocation(targetLocation: Location) {
        try {
            val fakePlayer = bot.getFakePlayer()
            
            val routeResult = com.zenyte.game.world.entity.pathfinding.RouteFinder.findRoute(
                fakePlayer.getX(), 
                fakePlayer.getY(), 
                0,
                fakePlayer.getSize(),
                com.zenyte.game.world.entity.pathfinding.strategy.TileStrategy(targetLocation.x, targetLocation.y),
                true
            )
            
            if (routeResult == com.zenyte.game.world.entity.pathfinding.RouteResult.ILLEGAL) {
                val currentLocation = bot.getLocation()
                val ditchLocation = Location(3100, 3520, 0)
                val distanceToDitch = currentLocation.getDistance(ditchLocation)
                
                if (distanceToDitch <= 5) {
                    crossWildernessDitch()
                    return
                }
                
                return
            }
            
            if (routeResult.getSteps() == 0) {
                return
            }
            
            fakePlayer.resetWalkSteps()
            
            fakePlayer.setRun(true)
            
            val bufferX = routeResult.getXBuffer()
            val bufferY = routeResult.getYBuffer()
            
            for (step in routeResult.getSteps() - 1 downTo 0) {
                if (!fakePlayer.addWalkSteps(bufferX[step], bufferY[step], 60, true)) {
                    break
                }
            }
            
        } catch (e: Exception) {
        }
    }
    
    private fun moveTowardsLocation(targetLocation: Location) {
        val currentLocation = bot.getLocation()
        val dx = targetLocation.x - currentLocation.x
        val dy = targetLocation.y - currentLocation.y
        
        val nextX = currentLocation.x + if (dx > 0) 1 else if (dx < 0) -1 else 0
        val nextY = currentLocation.y + if (dy > 0) 1 else if (dy < 0) -1 else 0
        
        val fakePlayer = bot.getFakePlayer()
        if (!fakePlayer.addWalkSteps(nextX, nextY, 1, true)) {
            return
        }
    }
    
    private fun refillInventory() {
        val inventory = bot.getInventory()
        
        inventory.getContainer().clear()
        
        inventory.addItem(Item(385, 20))
        inventory.addItem(Item(2434, 3))
        inventory.addItem(Item(2436, 2))
        inventory.addItem(Item(2440, 2))
        inventory.addItem(Item(5698, 1))
        
    }
    
    fun resetWaypoints() {
        currentWaypointIndex = 0
        hasCrossedDitch = false
    }
    
    private fun shouldRestockAfterTimeout(): Boolean {
        if (lastTargetTime == 0L) {
            return false
        }
        
        val currentTime = WorldThread.getCurrentCycle()
        val timeSinceLastTarget = currentTime - lastTargetTime
        
        if (timeSinceLastTarget % 3000L == 0L && timeSinceLastTarget > 0) {
            val secondsElapsed = timeSinceLastTarget / 600
        }
        
        val shouldRestock = timeSinceLastTarget >= RESTOCK_TIMER
        if (shouldRestock) {
        }
        
        return shouldRestock
    }
    
    private fun needsRestocking(): Boolean {
        val inventory = bot.getInventory()
        val currentHealth = bot.getHitpoints()
        val maxHealth = bot.getMaxHitpoints()
        val currentPrayer = bot.getPrayerPoints()
        val maxPrayer = bot.getSkills().getLevelForXp(com.zenyte.game.world.entity.player.SkillConstants.PRAYER)
        
        var foodCount = 0
        for (i in 0 until inventory.getContainer().getSize()) {
            val item = inventory.getContainer().get(i)
            if (item != null && (item.id == 385 || item.id == 386 || item.id == 387)) {
                foodCount += item.amount
            }
        }
        
        val lowFood = foodCount < 5
        val lowHealth = (currentHealth * 100) / maxHealth < 50
        val lowPrayer = (currentPrayer * 100) / maxPrayer < 30
        
        return lowFood || lowHealth || lowPrayer
    }
    
    fun teleportToRestock() {
        bot.setState(BotState.RESPAWNING)
        
        val fakePlayer = bot.getFakePlayer()
        
        fakePlayer.animation = com.zenyte.game.world.entity.masks.Animation(714)
        fakePlayer.graphics = com.zenyte.game.world.entity.masks.Graphics(308, 0, 100)
        
        com.zenyte.game.task.WorldTasksManager.schedule(3) {
            val respawnLocation = com.zenyte.game.world.entity.Location(3105, 3494, 0)
            bot.setLocation(respawnLocation)
            
            fakePlayer.animation = null
            fakePlayer.graphics = null
            
            performRestock()
        }
    }
    
    private fun performRestock() {
        bot.setHitpoints(bot.getMaxHitpoints())
        
        bot.getCombatDefinitions().setSpecialEnergy(100)
        
        val maxPrayer = bot.getSkills().getLevelForXp(com.zenyte.game.world.entity.player.SkillConstants.PRAYER)
        bot.setPrayerPoints(maxPrayer)
        
        bot.restockAfterDeath()
        
        lastTargetTime = 0L
        
        bot.setState(BotState.RETURNING)
        
    }
    
    private fun sendRandomPkMessage() {
        val messages = listOf(
            "Gl!",
            "Good luck noob!",
            "Let's go!",
            "Fight me!",
            "Come at me!",
            "You're dead!",
            "Rip!",
            "Ez!",
            "Get ready!",
            "Time to die!"
        )
        
        val randomMessage = messages.random()
        val fakePlayer = bot.getFakePlayer()
        
        fakePlayer.getChatMessage().set(randomMessage, 0, false)
        fakePlayer.getUpdateFlags().flag(com.zenyte.game.world.entity.masks.UpdateFlag.CHAT)
        
    }
    
    fun sendRandomKillMessage() {
        val messages = listOf(
            "Gf!",
            "Good fight!",
            "Rip!",
            "Ez!",
            "Get rekt!",
            "You died!",
            "Lol!",
            "Noob!",
            "Too easy!",
            "Better luck next time!"
        )
        
        val randomMessage = messages.random()
        val fakePlayer = bot.getFakePlayer()
        
        fakePlayer.getChatMessage().set(randomMessage, 0, false)
        fakePlayer.getUpdateFlags().flag(com.zenyte.game.world.entity.masks.UpdateFlag.CHAT)
        
    }
} 