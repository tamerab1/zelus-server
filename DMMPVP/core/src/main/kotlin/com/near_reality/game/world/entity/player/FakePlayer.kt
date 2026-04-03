package com.near_reality.game.world.entity.player

import com.near_reality.net.HardwareInfo
import com.zenyte.game.world.entity.player.Player
import com.zenyte.game.world.entity.player.PlayerInformation
import com.zenyte.game.world.entity.masks.Hit
import com.zenyte.net.Session
import com.zenyte.net.game.ServerEvent
import com.zenyte.game.world.entity.Entity
import com.zenyte.game.world.entity.masks.Animation
import com.zenyte.game.task.WorldTask
import com.zenyte.game.task.WorldTasksManager
import com.zenyte.game.content.skills.prayer.Prayer

class FakePlayer(username: String) : Player(
    fakePlayerInformation(username),
    null
) {
    init {
        session = FakeSession()
    }

    /**
     * Override handleOutgoingHit to track damage for bot special attacks
     */
    override fun handleOutgoingHit(target: com.zenyte.game.world.entity.Entity, hit: com.zenyte.game.world.entity.masks.Hit) {
        super.handleOutgoingHit(target, hit)

        val bot = com.near_reality.game.world.entity.player.bot.PKBotPlayer.getBotByFakePlayer(this)
        if (bot != null) {
            // Track special attacks for ALL bots (unchanged)
            if (hit.isSpecial()) {
                bot.trackDamage(target, hit)
            }

            // Track ALL attacks for DH bot ONLY (for DH axe tracking)
            if (bot.getBotType() == com.near_reality.game.world.entity.player.bot.BotType.DH_BOT) {
                bot.trackDamage(target, hit)
            }
        }
    }

    /**
     * Handle death like a real player - with proper death mechanics
     */
    fun handleDeath(source: Entity? = null) {
        // Stop all actions and lock the player
        animation = Animation.STOP
        lock()
        stopAll()

        // Handle retribution prayer if active
        if (prayerManager.isActive(Prayer.RETRIBUTION)) {
            prayerManager.applyRetributionEffect(source)
        }

        // Schedule death sequence
        WorldTasksManager.schedule(object : WorldTask {
            var ticks = 0
            override fun run() {
                if (isFinished || isNulled) {
                    stop()
                    return
                }
                when (ticks) {
                    0 -> {
                        // Start death animation
                        animation = Player.DEATH_ANIMATION
                    }
                    2 -> {
                        // Death message and mechanics
                        sendMessage("Oh dear, you have died.")

                        // Handle death mechanics (drop items, lose experience, etc.)
                        handleDeathMechanics(source)

                        // Reset player state
                        reset()
                        blockIncomingHits(5)
                        animation = Animation.STOP

                        // Remove skull if active
                        if (variables.isSkulled) {
                            variables.setSkull(false)
                        }

                        // Set respawn location (use proper respawn system)
                        val respawnLocation = getRespawnLocation()
                        setLocation(respawnLocation)

                        // Notify the bot system about the death
                        notifyBotDeath()
                    }
                    3 -> {
                        // Unlock and finish death sequence
                        unlock()
                        appearance.resetRenderAnimation()
                        animation = Animation.STOP
                        stop()
                    }
                }
                ticks++
            }
        }, 0, 1)
    }

    /**
     * Handle death mechanics like a real player
     */
    private fun handleDeathMechanics(source: Entity?) {
        // Drop bones at death location
        com.zenyte.game.world.World.spawnFloorItem(
            com.zenyte.game.item.Item(526, 1), // Bones
            this,
            0,
            300
        )

        // Handle item dropping based on location
        if (isInWilderness()) {
            // In wilderness - drop items to killer
            handleWildernessDeath(source)
        } else {
            // Outside wilderness - use gravestone system
            handleSafeDeath(source)
        }

        // Send death message to killer if it's a player
        if (source is Player) {
            val deathMessages = arrayOf(
                "You have defeated %s.",
                "You have killed %s.",
                "%s has fallen to your might.",
                "You have slain %s."
            )
            val message = deathMessages.random()
            source.sendMessage(message.format(username))
        }
    }

    /**
     * Handle wilderness death (drop items to killer)
     */
    private fun handleWildernessDeath(source: Entity?) {
        // Drop all equipment and inventory items
        val equipment = getEquipment()
        val inventory = getInventory()

        // Drop equipment items
        for (slot in 0..13) {
            val item = equipment.getItem(slot)
            if (item != null) {
                com.zenyte.game.world.World.spawnFloorItem(item, this, 0, 300)
                equipment.set(slot, null)
            }
        }

        // Drop inventory items
        for (slot in 0 until inventory.getContainer().getSize()) {
            val item = inventory.getContainer().get(slot)
            if (item != null) {
                com.zenyte.game.world.World.spawnFloorItem(item, this, 0, 300)
                inventory.getContainer().set(slot, null)
            }
        }

        // Refresh containers
        equipment.refresh()
        inventory.refresh()
    }

    /**
     * Handle safe death (use gravestone system)
     */
    private fun handleSafeDeath(source: Entity?) {
        // For bots, we'll use a simplified gravestone system
        // In a real implementation, this would use the proper gravestone mechanics

        // Clear inventory and equipment (items go to gravestone)
        val equipment = getEquipment()
        val inventory = getInventory()

        // Clear equipment
        for (slot in 0..13) {
            equipment.set(slot, null)
        }

        // Clear inventory
        inventory.getContainer().clear()

        // Refresh containers
        equipment.refresh()
        inventory.refresh()
    }

    /**
     * Check if player is in wilderness
     */
    private fun isInWilderness(): Boolean {
        val x = location.x
        val y = location.y
        return y >= 3520 && y <= 3967 && x >= 2944 && x <= 3391
    }

    /**
     * Get proper respawn location
     */
    private fun getRespawnLocation(): com.zenyte.game.world.entity.Location {
        // For bots, use the default respawn location
        // In a real implementation, this would check area plugins and respawn points
        return com.zenyte.game.world.entity.Location(3105, 3494, 0) // Edgeville respawn
    }

    /**
     * Notify the bot system about the death
     */
    private fun notifyBotDeath() {
        val bot = com.near_reality.game.world.entity.player.bot.PKBotPlayer.getBotByFakePlayer(this)
        if (bot != null) {
            // Set bot state to respawning
            bot.setState(com.near_reality.game.world.entity.player.bot.BotState.RESPAWNING)

            // Schedule restocking after respawn
            com.zenyte.game.task.WorldTasksManager.schedule(3) {
                bot.restockAfterDeath()
            }
        }
    }
}

private class FakeSession : Session {
    override fun getHostAddress(): String = "0.0.0.0"
    override fun process(): Boolean = true
    override fun send(event: ServerEvent): Boolean = true
    override fun flush() = Unit
    override fun close() = Unit
    override fun isActive(): Boolean = true
    override fun isExpired(): Boolean = false
}

private fun fakePlayerInformation(username: String) =
    PlayerInformation(username, username, -1, ByteArray(25), fakeHardwareInfo())

private fun fakeHardwareInfo() = HardwareInfo(
    cpuFeatures = intArrayOf(0, 0, 0),
    osId = 1,
    osVersion = 11,
    javaVendorId = 5,
    javaVersionMajor = 18,
    javaVersionMinor = 0,
    javaVersionUpdate = 2,
    heap = 4079,
    logicalProcessors = 12,
    physicalMemory = 0,
    clockSpeed = 0,
    graphicCardReleaseMonth = 0,
    graphicCardReleaseYear = 0,
    cpuCount = 0,
    cpuBrandType = 0,
    cpuModel = 0,
    graphicCardManufacture = "",
    graphicCardName = "",
    dxVersion = "",
    cpuManufacture = "",
    cpuName = "",
    isArch64Bit = true,
    isApplet = true
)
