package com.near_reality.game.world

import com.near_reality.game.world.entity.player.bot.*
import com.near_reality.game.world.entity.player.bot.loadouts.MeleeDDSLoadout
import com.near_reality.game.world.entity.player.bot.loadouts.MeleeWhipLoadout
import com.near_reality.game.world.entity.player.bot.loadouts.DHLoadout
import com.near_reality.game.world.entity.player.bot.loadouts.PureLoadout
import com.zenyte.game.world.entity.Location
import com.zenyte.game.world.entity.player.Player
import com.zenyte.game.item.Item
import com.zenyte.game.world.entity.player.container.impl.equipment.EquipmentSlot
import com.zenyte.game.world.World
import com.zenyte.game.task.WorldTasksManager
import com.zenyte.net.io.RSBuffer

/**
 * Manages all active PK bots in the world.
 *
 * @author Riskers
 */
object PKBotManager {
    private val activeBots = mutableMapOf<String, PKBotPlayer>()
    private var spawnIndex = 0


    private val loadoutRegistry = mapOf(
        BotType.MELEE_DDS to MeleeDDSLoadout.loadout,
        BotType.MELEE_WHIP to MeleeWhipLoadout.loadout,
        BotType.DH_BOT to DHLoadout.loadout,
        BotType.PURE_BOT to PureLoadout.loadout

    )

    fun createBot(username: String, location: Location = Location(3083, 3535, 0)): PKBotPlayer {
        return createBot(username, BotType.MELEE_DDS, location)
    }

    fun createBot(
        username: String,
        botType: BotType = BotType.MELEE_DDS,
        location: Location = Location(3083, 3535, 0)
    ): PKBotPlayer {

        val loadout = loadoutRegistry[botType]
            ?: throw IllegalArgumentException("Unknown bot type: $botType")

        val bot = PKBotPlayer(username)
        bot.setConfiguration(BotConfiguration.fromLoadout(loadout))

        // ----------------------------------------
        // CUSTOM SPAWN LOCATION FOR PURE BOT
        // ----------------------------------------
        val spawnLoc = if (botType == BotType.PURE_BOT) {

            // EXACT pure bot spawn position
            Location(3077, 3532, 0)

        } else {

            // Normal bots spread automatically
            val base = Location(3083, 3535, 0)
            val offsetX = spawnIndex * 5
            spawnIndex++

            Location(base.x + offsetX, base.y, base.plane)
        }

        bot.setLocation(spawnLoc)
        bot.initialize()
        equipLoadout(bot, loadout)

        val fakePlayer = bot.getFakePlayer()
        fakePlayer.setDefaultSettings()
        fakePlayer.forceLocation(spawnLoc.copy())

        val playerInfo = fakePlayer.getPlayerViewport()
        val initBuffer = RSBuffer(255)
        playerInfo.init(initBuffer)

        fakePlayer.loadMapRegions(true)
        fakePlayer.lastLoadedMapRegionTile = spawnLoc.copy()
        fakePlayer.afterLoadMapRegions()
        fakePlayer.putBooleanAttribute("registered", true)

        World.addPlayer(fakePlayer)

        WorldTasksManager.schedule(1) {
            fakePlayer.isInitialized = true
        }

        activeBots[username] = bot
        bot.setState(BotState.IDLE)

        return bot
    }


    private fun equipLoadout(bot: PKBotPlayer, loadout: BotLoadout) {
        val equipment = bot.getEquipment()
        val inventory = bot.getInventory()

        for (slot in EquipmentSlot.values()) {
            equipment.set(slot, null)
        }
        inventory.getContainer().clear()

        loadout.equipment.forEach { (slot, item) ->
            equipment.set(slot, item)
        }
        equipment.refresh()

        loadout.inventory.forEach { item ->
            inventory.addItem(item)
        }
    }

    fun createDefaultBot(): PKBotPlayer {
        val botNumber = activeBots.size + 1
        val username = "PK_Bot_$botNumber"
        return createBot(username)
    }

    private fun equipDefaultMeleeSet(bot: PKBotPlayer) {
        val equipment = bot.getEquipment()

        equipment.set(EquipmentSlot.HELMET, Item(10828, 1))
        equipment.set(EquipmentSlot.CAPE, Item(6570, 1))
        equipment.set(EquipmentSlot.AMULET, Item(6585, 1))
        equipment.set(EquipmentSlot.WEAPON, Item(4587, 1))
        equipment.set(EquipmentSlot.PLATE, Item(1127, 1))
        equipment.set(EquipmentSlot.SHIELD, Item(8850, 1))
        equipment.set(EquipmentSlot.LEGS, Item(1079, 1))
        equipment.set(EquipmentSlot.HANDS, Item(7462, 1))
        equipment.set(EquipmentSlot.BOOTS, Item(4127, 1))
        equipment.set(EquipmentSlot.RING, Item(2550, 1))
        equipment.set(EquipmentSlot.AMMUNITION, Item(892, 100))

        val config = bot.getConfiguration()
        bot.setConfiguration(config.copy(
            useSpecialAttacks = true,
            specialAttackChance = 3,
            specialAttackTargetHealth = 50,
            autoWeaponSwitch = true,
            weaponId = 4587,
            specialAttackWeapons = listOf(5698, 5699),
            eatHealthPercentage = 50,
            eatUntilHealthPercentage = 70
        ))
    }

    private fun fillDefaultInventory(bot: PKBotPlayer) {
        val inventory = bot.getInventory()

        inventory.addItem(Item(385, 20))
        inventory.addItem(Item(2434, 3))
        inventory.addItem(Item(2436, 2))
        inventory.addItem(Item(2440, 2))
        inventory.addItem(Item(5698, 1))
    }

    fun removeBot(username: String): Boolean {
        val bot = activeBots[username] ?: return false

        World.removePlayer(bot.getFakePlayer())

        activeBots.remove(username)

        return true
    }

    fun getActiveBots(): Map<String, PKBotPlayer> = activeBots.toMap()

    fun processBots() {
        activeBots.values.forEach { bot ->
            try {
                bot.aiController.process()
            } catch (e: Exception) {
            }
        }
    }

    fun getBotStatus(username: String): String? {
        val bot = activeBots[username] ?: return null

        val location = bot.getLocation()
        val health = bot.getHitpoints()
        val maxHealth = bot.getMaxHitpoints()
        val prayerPoints = bot.getPrayerPoints()
        val maxPrayerPoints = bot.getSkills().getLevelForXp(com.zenyte.game.world.entity.player.SkillConstants.PRAYER)
        val state = bot.getCurrentState()
        val target = bot.getTarget()?.username ?: "None"
        val botType = bot.getConfiguration().botType

        return """
            Bot: $username
            Type: $botType
            Location: ${location.x}, ${location.y}, ${location.plane}
            Health: $health/$maxHealth
            Prayer: $prayerPoints/$maxPrayerPoints
            State: $state
            Target: $target
        """.trimIndent()
    }

    fun getAllBotStatuses(): String {
        if (activeBots.isEmpty()) {
            return "No active bots"
        }

        return activeBots.keys.joinToString("\n") { username ->
            getBotStatus(username) ?: "Bot $username not found"
        }
    }
}