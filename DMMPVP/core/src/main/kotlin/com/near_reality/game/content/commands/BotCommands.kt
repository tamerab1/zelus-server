package com.near_reality.game.content.commands

import com.near_reality.game.world.PKBotManager
import com.near_reality.game.world.entity.player.bot.PKBotPlayer
import com.zenyte.game.world.entity.player.Player
import com.zenyte.game.world.entity.Location
import com.zenyte.game.world.entity.player.privilege.PlayerPrivilege
import com.zenyte.game.world.entity.player.GameCommands.Command

/**
 * Handles all bot-related commands for the PK bot system.
 * 
 * @author Riskers
 */
object BotCommands {
    
    fun register() {
        /**
         * Command to spawn a melee PK bot next to the player
         */
        Command(PlayerPrivilege.ADMINISTRATOR, "addmeleebot", "Spawns a melee PK bot next to the player") { player, args ->
            val botName = if (args.isNotEmpty()) args[0] else "MeleeBot_${System.currentTimeMillis()}"
            val botLocation = player.location
            
            try {
                val bot = PKBotManager.createBot(botName, botLocation)
                player.sendMessage("Successfully created melee bot: $botName")
                player.sendMessage("Bot spawned at: ${botLocation.x}, ${botLocation.y}, ${botLocation.plane}")
            } catch (e: Exception) {
                player.sendMessage("Failed to create bot: ${e.message}")
            }
        }
        
        /**
         * Command to remove a specific bot by name
         */
        Command(PlayerPrivilege.ADMINISTRATOR, "removebot", "Removes a bot by name") { player, args ->
            if (args.isEmpty()) {
                player.sendMessage("Usage: ::removebot <bot_name>")
                return@Command
            }
            
            val botName = args[0]
            val removed = PKBotManager.removeBot(botName)
            
            if (removed) {
                player.sendMessage("Successfully removed bot: $botName")
            } else {
                player.sendMessage("Bot not found: $botName")
            }
        }
        
        /**
         * Command to list all active bots
         */
        Command(PlayerPrivilege.ADMINISTRATOR, "listbots", "Lists all active bots") { player, args ->
            val bots = PKBotManager.getActiveBots()
            
            if (bots.isEmpty()) {
                player.sendMessage("No active bots found.")
                return@Command
            }
            
            player.sendMessage("Active bots (${bots.size}):")
            bots.forEach { (name, bot) ->
                val status = bot.getCurrentState().name
                val location = bot.getLocation()
                player.sendMessage("- $name: $status at (${location.x}, ${location.y}, ${location.plane})")
            }
        }
        
        /**
         * Command to spawn multiple bots in an area (admin command)
         */
        Command(PlayerPrivilege.ADMINISTRATOR, "spawnbotarmy", "Spawns multiple bots in an area") { player, args ->
            val count = if (args.isNotEmpty()) args[0].toIntOrNull() ?: 5 else 5
            
            if (count > 20) {
                player.sendMessage("Maximum 20 bots allowed for army spawn.")
                return@Command
            }
            
            val baseLocation = player.location
            var spawned = 0
            
            for (i in 1..count) {
                val botName = "ArmyBot_${System.currentTimeMillis()}_$i"
                val offsetX = (i % 5) * 2
                val offsetY = (i / 5) * 2
                val botLocation = Location(baseLocation.x + offsetX, baseLocation.y + offsetY, baseLocation.plane)
                
                try {
                    PKBotManager.createBot(botName, botLocation)
                    spawned++
                } catch (e: Exception) {
                    player.sendMessage("Failed to create bot $botName: ${e.message}")
                }
            }
            
            player.sendMessage("Successfully spawned $spawned bots in army formation.")
        }
        
        /**
         * Command to open bot configuration interface
         */
        Command(PlayerPrivilege.ADMINISTRATOR, "botconfig", "Opens bot configuration interface") { player, args ->
            // TODO: Implement bot configuration interface
            player.sendMessage("Bot configuration interface coming soon!")
        }
    }
} 