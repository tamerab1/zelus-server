package com.near_reality.game.world.entity.player.bot

import com.google.common.eventbus.Subscribe
import com.zenyte.plugins.events.ServerLaunchEvent
import com.near_reality.game.world.PKBotManager
import com.zenyte.game.world.entity.Location

/**
 * Automatically spawns three PK bots on server startup.
 */
object PKBotAutoSpawner {
    @Subscribe
    @JvmStatic
    fun onServerLaunch(event: ServerLaunchEvent) {
        // All bots start at the same respawn location (3105, 3494, 0)
        val respawnLocation = Location(3105, 3494, 0)
        
        // Spawn the first PK bot (DDS)
        val botName1 = "PKBot"
        if (!PKBotManager.getActiveBots().containsKey(botName1)) {
            PKBotManager.createBot(botName1, BotType.MELEE_DDS, respawnLocation)
            //println("Auto-spawned PKBot (DDS) at server startup.")
        }
        
        // Spawn the second PK bot (Whip + AGS)
        val botName2 = "PKBot2"
        if (!PKBotManager.getActiveBots().containsKey(botName2)) {
            PKBotManager.createBot(botName2, BotType.MELEE_WHIP, respawnLocation)
            //println("Auto-spawned PKBot2 (Whip + AGS) at server startup.")
        }
        
        // Spawn the third PK bot (DH)
        val botName3 = "PKBot3"
        if (!PKBotManager.getActiveBots().containsKey(botName3)) {
            PKBotManager.createBot(botName3, BotType.DH_BOT, respawnLocation)
            //println("Auto-spawned PKBot3 (DH) at server startup.")
        }
        // PURE bot
        val botName4 = "PureBot"
        if (!PKBotManager.getActiveBots().containsKey(botName4)) {
            PKBotManager.createBot(botName4, BotType.PURE_BOT, respawnLocation)
        }

    }
} 