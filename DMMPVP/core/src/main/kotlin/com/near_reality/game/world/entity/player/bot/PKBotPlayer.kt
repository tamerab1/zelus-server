package com.near_reality.game.world.entity.player.bot

import com.near_reality.game.world.entity.player.FakePlayer
import com.near_reality.game.world.entity.player.bot.BotConfiguration
import com.zenyte.game.world.entity.player.Player
import com.zenyte.game.world.entity.Location
import com.zenyte.game.world.entity.player.SkillConstants
import com.zenyte.game.world.entity.player.privilege.PlayerPrivilege
import com.zenyte.game.world.entity.Entity
import com.zenyte.game.world.entity.masks.Hit

/**
 * Represents a PK bot player that uses FakePlayer for realistic behavior.
 *
 * @author Riskers
 */
class PKBotPlayer(username: String) {

    private val fakePlayer: FakePlayer = FakePlayer(username)

    companion object {
        private val fakePlayerToBot = mutableMapOf<com.near_reality.game.world.entity.player.FakePlayer, PKBotPlayer>()

        fun getBotByFakePlayer(fakePlayer: com.near_reality.game.world.entity.player.FakePlayer): PKBotPlayer? {
            return fakePlayerToBot[fakePlayer]
        }

        fun registerBot(fakePlayer: com.near_reality.game.world.entity.player.FakePlayer, bot: PKBotPlayer) {
            fakePlayerToBot[fakePlayer] = bot
        }

        fun unregisterBot(fakePlayer: com.near_reality.game.world.entity.player.FakePlayer) {
            fakePlayerToBot.remove(fakePlayer)
        }
    }

    init {
        registerBot(fakePlayer, this)
    }

    private var targetPlayer: Player? = null
    private var lastCombatTick: Long = 0
    private var eatCounter: Int = 0
    private var specialAttackEnabled: Boolean = false

    private var lastSpecialAttackDamage: Int = 0
    private var consecutiveSpecialAttacks: Int = 0
    private var lastSpecialAttackTick: Long = 0
    
    private var dhAxeHits: Int = 0
    private var maxDhAxeHits: Int = (2..3).random()

    private var currentState: BotState = BotState.IDLE
    private var lastPrayerSwitch: Long = 0
    private var lastFoodEat: Long = 0

    private var configuration: BotConfiguration = BotConfiguration.DEFAULT_MELEE_CONFIG

    val aiController = PKBotAI(this)

    fun getFakePlayer(): FakePlayer = fakePlayer
    fun getRespawnLocation(): Location {
        return if (configuration.botType == BotType.PURE_BOT) {
            Location(3077, 3532, 0) // Pure bot spawn
        } else {
            Location(3093, 3536, 0) // Normal bot spawn
        }
    }

    fun getLocation(): Location = fakePlayer.location
    fun setLocation(location: Location) = fakePlayer.setLocation(location)
    fun getHitpoints(): Int = fakePlayer.hitpoints
    fun setHitpoints(hitpoints: Int) = fakePlayer.setHitpoints(hitpoints)
    fun getMaxHitpoints(): Int = fakePlayer.getMaxHitpoints()
    fun getPrayerPoints(): Int = fakePlayer.getPrayerManager().getPrayerPoints()
    fun setPrayerPoints(points: Int) = fakePlayer.getPrayerManager().setPrayerPoints(points)
    fun getSkills() = fakePlayer.getSkills()
    fun getCombatDefinitions() = fakePlayer.getCombatDefinitions()
    fun getInventory() = fakePlayer.getInventory()
    fun getEquipment() = fakePlayer.getEquipment()
    fun getUsername(): String = fakePlayer.username
    fun isDead(): Boolean = fakePlayer.isDead
    fun isDying(): Boolean = fakePlayer.isDying
    fun isFinished(): Boolean = fakePlayer.isFinished

    fun initialize() {
        val skills = getSkills()

        if (configuration.botType == BotType.PURE_BOT) {
            // PURE STATS
            skills.setSkill(SkillConstants.ATTACK, 60, 273742.0)
            skills.setSkill(SkillConstants.STRENGTH, 99, 13034431.0)
            skills.setSkill(SkillConstants.DEFENCE, 1, 0.0)
            skills.setSkill(SkillConstants.HITPOINTS, 99, 13034431.0)
            skills.setSkill(SkillConstants.RANGED, 99, 13034431.0)
            skills.setSkill(SkillConstants.MAGIC, 99, 13034431.0)
            skills.setSkill(SkillConstants.PRAYER, 52, 106038.0)
        } else {
            // GEWONE MAIN / MAXED PK BOT
            skills.setSkill(SkillConstants.ATTACK, 99, 13034431.0)
            skills.setSkill(SkillConstants.DEFENCE, 99, 13034431.0)
            skills.setSkill(SkillConstants.STRENGTH, 99, 13034431.0)
            skills.setSkill(SkillConstants.HITPOINTS, 99, 13034431.0)
            skills.setSkill(SkillConstants.RANGED, 99, 13034431.0)
            skills.setSkill(SkillConstants.MAGIC, 99, 13034431.0)
            skills.setSkill(SkillConstants.PRAYER, 99, 13034431.0)
        }

        skills.refresh()

        setHitpoints(getMaxHitpoints())
        setPrayerPoints(getSkills().getLevelForXp(SkillConstants.PRAYER))
        getCombatDefinitions().setSpecialEnergy(100)

        currentState = BotState.IDLE
        eatCounter = 0
        specialAttackEnabled = false
    }


    
    fun cleanup() {
        targetPlayer = null
        currentState = BotState.IDLE
    }

    fun isInCombat(): Boolean {
        return currentState == BotState.COMBAT || targetPlayer != null
    }

    fun setTarget(player: Player?) {
        if (targetPlayer != player) {
            resetSpecialAttackTracking()
        }

        targetPlayer = player
        if (player != null) {
            currentState = BotState.COMBAT
            aiController.activateCombatPrayers(player)
        } else {
            currentState = BotState.IDLE
        }
    }

    fun getTarget(): Player? = targetPlayer

    fun getTargetPlayer(): Player? = targetPlayer

    fun setTargetPlayer(player: Player?) {
        targetPlayer = player
        if (player != null) {
            currentState = BotState.COMBAT
        } else {
            currentState = BotState.IDLE
        }
    }

    fun getCurrentState(): BotState = currentState

    fun setState(state: BotState) {
        currentState = state
    }

    fun getEatCounter(): Int = eatCounter

    fun incrementEatCounter() {
        eatCounter++
    }

    fun resetEatCounter() {
        eatCounter = 0
    }

    fun isSpecialAttackEnabled(): Boolean = specialAttackEnabled

    fun getSpecialAttackPercentage(): Int {
        return fakePlayer.getCombatDefinitions().getSpecialEnergy()
    }

    fun setSpecialAttackEnabled(enabled: Boolean) {
        specialAttackEnabled = enabled
        fakePlayer.getCombatDefinitions().setSpecial(enabled, false)
    }

    fun getLastCombatTick(): Long = lastCombatTick

    fun setLastCombatTick(tick: Long) {
        lastCombatTick = tick
    }

    fun getLastPrayerSwitch(): Long = lastPrayerSwitch

    fun setLastPrayerSwitch(time: Long) {
        lastPrayerSwitch = time
    }

    fun getLastFoodEat(): Long = lastFoodEat

    fun setLastFoodEat(time: Long) {
        lastFoodEat = time
    }

    fun getConfiguration(): BotConfiguration = configuration

    fun setConfiguration(config: BotConfiguration) {
        configuration = config
    }

    fun hasEnoughSpecialEnergy(required: Int): Boolean {
        return getSpecialAttackPercentage() >= required
    }

    fun getHealthPercentage(): Int {
        val currentHealth = getHitpoints()
        val maxHealth = getMaxHitpoints()
        return if (maxHealth > 0) (currentHealth * 100) / maxHealth else 0
    }

    fun shouldEat(threshold: Int): Boolean {
        return getHealthPercentage() <= threshold
    }

    fun hasFood(foodItemId: Int): Boolean {
        val inventory = getInventory()
        for (i in 0 until inventory.getContainer().getSize()) {
            val item = inventory.getContainer().get(i)
            if (item != null && item.id == foodItemId) {
                return true
            }
        }
        return false
    }

    fun getFoodSlot(foodItemId: Int): Int {
        val inventory = getInventory()
        for (i in 0 until inventory.getContainer().getSize()) {
            val item = inventory.getContainer().get(i)
            if (item != null && item.id == foodItemId) {
                return i
            }
        }
        return -1
    }

    fun hasSpecialAttackWeapon(weaponIds: List<Int>): Boolean {
        val weapon = getEquipment().getItem(com.zenyte.game.world.entity.player.container.impl.equipment.EquipmentSlot.WEAPON)
        return weapon != null && weaponIds.contains(weapon.id)
    }

    fun getCurrentWeaponId(): Int {
        val weapon = getEquipment().getItem(com.zenyte.game.world.entity.player.container.impl.equipment.EquipmentSlot.WEAPON)
        return weapon?.id ?: -1
    }

    fun trackDamage(target: com.zenyte.game.world.entity.Entity, hit: com.zenyte.game.world.entity.masks.Hit) {
        val damage = hit.damage
        val isSpecial = hit.isSpecial()

        if (damage > 0) {
            val currentWeapon = getEquipment().getItem(com.zenyte.game.world.entity.player.container.impl.equipment.EquipmentSlot.WEAPON)
            if (currentWeapon?.id == 4718) {
                trackDhAxeHit()
            }
        }

        if (isSpecial) {
            incrementConsecutiveSpecialAttacks()
            lastSpecialAttackDamage = damage
            
            val loadout = getLoadout()
            val maxConsecutive = loadout?.specialAttackConfig?.maxConsecutiveSpecs ?: 2
            val switchBackWeaponId = loadout?.specialAttackConfig?.switchBackWeaponId ?: 4587

            if (consecutiveSpecialAttacks >= maxConsecutive) {
                resetSpecialAttackTracking()
                switchToPrimaryWeapon(switchBackWeaponId)
            } else {
                aiController.extendWeaponSwitchCooldown()
            }
        }
    }

    fun shouldDoConsecutiveSpecialAttack(): Boolean {
        val loadout = getLoadout()
        val maxConsecutive = loadout?.specialAttackConfig?.maxConsecutiveSpecs ?: 2
        return consecutiveSpecialAttacks > 0 && consecutiveSpecialAttacks < maxConsecutive
    }

    fun getLastSpecialAttackDamage(): Int = lastSpecialAttackDamage

    fun resetSpecialAttackTracking() {
        consecutiveSpecialAttacks = 0
        lastSpecialAttackDamage = 0
    }
    
    fun resetSpecialAttackSession() {
        consecutiveSpecialAttacks = 0
        lastSpecialAttackDamage = 0
    }
    
    fun triggerDeath(source: com.zenyte.game.world.entity.Entity? = null) {
        fakePlayer.handleDeath(source)
    }

    fun restockAfterDeath() {
        val skills = getSkills()
        setHitpoints(getMaxHitpoints())
        setPrayerPoints(skills.getLevelForXp(SkillConstants.PRAYER))
        getCombatDefinitions().setSpecialEnergy(100)

        val fakePlayer = getFakePlayer()

        val allPlayers = com.zenyte.game.world.World.getPlayers()
        for (player in allPlayers) {
            if (player.attacking == fakePlayer) {
                player.attacking = null
            }
        }

        if (fakePlayer.getAttacking() != null) {
            fakePlayer.setAttacking(null)
        }

        equipLoadoutGear()
        refillLoadoutInventory()

        currentState = BotState.IDLE
        targetPlayer = null
        resetSpecialAttackTracking()
        resetDhAxeTracking()

        aiController.resetWaypoints()

        // -----------------------------
        // NEW: MOVE BOT TO HIS RESPAWN SPOT
        // -----------------------------
        val respawn = getRespawnLocation()

// CORRECT — laat bot erheen lopen
        fakePlayer.addWalkSteps(respawn.x, respawn.y, -1, true)
        setState(BotState.RETURNING)

    }

    
    private fun equipLoadoutGear() {
        val loadout = getLoadout()
        if (loadout == null) {
            equipDefaultGear()
            return
        }
        
        val equipment = getEquipment()
        
        for (slot in com.zenyte.game.world.entity.player.container.impl.equipment.EquipmentSlot.values()) {
            equipment.set(slot, null)
        }
        
        loadout.equipment.forEach { (slot, item) ->
            equipment.set(slot, item)
        }
        
        equipment.refresh()
    }
    
    private fun equipDefaultGear() {
        val equipment = getEquipment()
        
        equipment.set(com.zenyte.game.world.entity.player.container.impl.equipment.EquipmentSlot.HELMET, com.zenyte.game.item.Item(10828, 1))
        equipment.set(com.zenyte.game.world.entity.player.container.impl.equipment.EquipmentSlot.CAPE, com.zenyte.game.item.Item(6570, 1))
        equipment.set(com.zenyte.game.world.entity.player.container.impl.equipment.EquipmentSlot.AMULET, com.zenyte.game.item.Item(6585, 1))
        equipment.set(com.zenyte.game.world.entity.player.container.impl.equipment.EquipmentSlot.WEAPON, com.zenyte.game.item.Item(4587, 1))
        equipment.set(com.zenyte.game.world.entity.player.container.impl.equipment.EquipmentSlot.PLATE, com.zenyte.game.item.Item(1127, 1))
        equipment.set(com.zenyte.game.world.entity.player.container.impl.equipment.EquipmentSlot.SHIELD, com.zenyte.game.item.Item(8850, 1))
        equipment.set(com.zenyte.game.world.entity.player.container.impl.equipment.EquipmentSlot.LEGS, com.zenyte.game.item.Item(1079, 1))
        equipment.set(com.zenyte.game.world.entity.player.container.impl.equipment.EquipmentSlot.HANDS, com.zenyte.game.item.Item(7462, 1))
        equipment.set(com.zenyte.game.world.entity.player.container.impl.equipment.EquipmentSlot.BOOTS, com.zenyte.game.item.Item(4127, 1))
        equipment.set(com.zenyte.game.world.entity.player.container.impl.equipment.EquipmentSlot.RING, com.zenyte.game.item.Item(2550, 1))
        
        equipment.refresh()
    }
    
    private fun refillLoadoutInventory() {
        val loadout = getLoadout()
        if (loadout == null) {
            refillInventory()
            return
        }
        
        val inventory = getInventory()
        
        inventory.getContainer().clear()
        
        loadout.inventory.forEach { item ->
            inventory.addItem(item)
        }
    }
    
    private fun refillInventory() {
        val inventory = getInventory()
        
        inventory.getContainer().clear()
        
        inventory.addItem(com.zenyte.game.item.Item(385, 20))
        inventory.addItem(com.zenyte.game.item.Item(2434, 3))
        inventory.addItem(com.zenyte.game.item.Item(2436, 2))
        inventory.addItem(com.zenyte.game.item.Item(2440, 2))
        inventory.addItem(com.zenyte.game.item.Item(5698, 1))
        
    }

    fun getBotType(): BotType = configuration.botType
    
    fun getLoadout(): BotLoadout? = configuration.loadout
    
    fun getConsecutiveSpecialAttacks(): Int = consecutiveSpecialAttacks
    
    fun incrementConsecutiveSpecialAttacks() {
        consecutiveSpecialAttacks++
    }
    
    private fun switchToPrimaryWeapon(weaponId: Int) {
        com.zenyte.game.task.WorldTasksManager.schedule(1) {
            val equipment = getEquipment()
            equipment.set(com.zenyte.game.world.entity.player.container.impl.equipment.EquipmentSlot.WEAPON, com.zenyte.game.item.Item(weaponId, 1))
            equipment.refresh()
        }
    }
    
    fun trackDhAxeHit() {
        dhAxeHits++
        
        if (dhAxeHits >= maxDhAxeHits) {
            val loadout = getLoadout()
            val switchBackWeaponId = loadout?.specialAttackConfig?.switchBackWeaponId ?: 4587
            switchToPrimaryWeapon(switchBackWeaponId)
            resetDhAxeTracking()
        }
    }
    
    fun resetDhAxeTracking() {
        dhAxeHits = 0
        maxDhAxeHits = (2..3).random()
    }
    
    fun shouldContinueUsingDhAxe(): Boolean {
        return dhAxeHits < maxDhAxeHits
    }
} 