package com.near_reality.game.content.commands

import com.near_reality.game.world.PKBotManager
import com.zenyte.game.world.entity.player.Player
import com.zenyte.game.world.entity.player.privilege.PlayerPrivilege
import com.zenyte.game.world.entity.player.GameCommands.Command
import com.zenyte.game.world.entity.player.SkillConstants
import com.zenyte.game.world.entity.player.Skills

/**
 * Test commands for the PK bot system.
 *
 * @author Riskers
 */
object BotTestCommands {

    fun register() {
        /**
         * Test command to verify bot system is working
         */
        Command(PlayerPrivilege.ADMINISTRATOR, "testbot", "Test command to verify bot system is working") { player, args ->
            val activeBots = PKBotManager.getActiveBots()
            val botCount = activeBots.size

            player.sendMessage("=== PK Bot System Test ===")
            player.sendMessage("Active bots: $botCount")
            player.sendMessage("Bot system is working!")

            if (activeBots.isNotEmpty()) {
                player.sendMessage("Active bot names:")
                activeBots.keys.forEach { botName ->
                    player.sendMessage("- $botName")
                }
            } else {
                player.sendMessage("No active bots found.")
            }
        }

        /**
         * Test command to check bot processing
         */
        Command(PlayerPrivilege.ADMINISTRATOR, "testbotprocess", "Test bot processing") { player, args ->
            val activeBots = PKBotManager.getActiveBots()

            if (activeBots.isEmpty()) {
                player.sendMessage("No active bots to test.")
                return@Command
            }

            player.sendMessage("=== Bot Processing Test ===")
            activeBots.values.forEach { bot ->
                val beforeState = bot.getCurrentState()
                val beforeHealth = bot.getHitpoints()

                // Process bot AI
                bot.aiController.process()

                val afterState = bot.getCurrentState()
                val afterHealth = bot.getHitpoints()

                player.sendMessage("Bot ${bot.getUsername()}:")
                player.sendMessage("  State: $beforeState -> $afterState")
                player.sendMessage("  Health: $beforeHealth -> $afterHealth")
            }
        }

        /**
         * Test command to manually trigger bot death
         */
        Command(PlayerPrivilege.ADMINISTRATOR, "testbotdeath", "Test bot death mechanics") { player, args ->
            val botName = if (args.isNotEmpty()) args[0] else "PKBot"
            val bot = PKBotManager.getActiveBots()[botName]

            if (bot == null) {
                player.sendMessage("Bot '$botName' not found. Available bots: ${PKBotManager.getActiveBots().keys.joinToString(", ")}")
                return@Command
            }

            player.sendMessage("Testing death mechanics for bot: $botName")
            player.sendMessage("Current health: ${bot.getHitpoints()}/${bot.getMaxHitpoints()}")
            player.sendMessage("Current state: ${bot.getCurrentState()}")

            // Set bot health to 0 to trigger death
            bot.setHitpoints(0)

            player.sendMessage("Set bot health to 0. Death sequence should trigger on next AI tick.")
        }

        /**
         * Command to check bot status
         */
        Command(PlayerPrivilege.ADMINISTRATOR, "botstatus", "Check bot status") { player, args ->
            val activeBots = PKBotManager.getActiveBots()

            player.sendMessage("=== Bot Status ===")
            player.sendMessage("Total bots: ${activeBots.size}")

            activeBots.forEach { (name, bot) ->
                val state = bot.getCurrentState().name
                val health = bot.getSkills().getLevel(Skills.HITPOINTS)
                val maxHealth = bot.getSkills().getLevelForXp(Skills.HITPOINTS)
                val location = bot.getLocation()
                val combatLevel = bot.getSkills().getCombatLevel()
                val botType = bot.getBotType()
                val loadout = bot.getLoadout()

                // Get combat stats
                val attack = bot.getSkills().getLevel(SkillConstants.ATTACK)
                val defence = bot.getSkills().getLevel(SkillConstants.DEFENCE)
                val strength = bot.getSkills().getLevel(SkillConstants.STRENGTH)
                val ranged = bot.getSkills().getLevel(SkillConstants.RANGED)
                val magic = bot.getSkills().getLevel(SkillConstants.MAGIC)
                val prayer = bot.getSkills().getLevel(SkillConstants.PRAYER)

                player.sendMessage("$name ($botType): $state, HP: $health/$maxHealth, Combat: $combatLevel, Location: (${location.x}, ${location.y}, ${location.plane})")
                player.sendMessage("  Stats: A:$attack D:$defence S:$strength R:$ranged M:$magic P:$prayer")

                // Show loadout info if available
                loadout?.let { l ->
                    player.sendMessage("  Loadout: ${l.name}")
                    val specialConfig = l.specialAttackConfig
                    player.sendMessage("  Special: ${specialConfig.weaponId} (${specialConfig.energyRequired}% energy, ${specialConfig.targetHealthThreshold}% target HP)")
                }
            }
        }

        /**
         * Command to spawn different bot types
         */
        Command(PlayerPrivilege.ADMINISTRATOR, "spawnbot", "Spawn a bot with specific type") { player, args ->
            if (args.isEmpty()) {
                player.sendMessage("Usage: ::spawnbot <type> [name]")
                player.sendMessage("Available types: MELEE_DDS, MELEE_WHIP, RANGER_MSB, HYBRID_AGS, DH_BOT, HYBRID_ADVANCED, MAGE_BOT")
                return@Command
            }

            val botTypeStr = args[0].uppercase()
            val botName = if (args.size > 1) args[1] else "PK_Bot_${System.currentTimeMillis() % 1000}"

            try {
                val botType = com.near_reality.game.world.entity.player.bot.BotType.valueOf(botTypeStr)
                val location = player.location.copy()

                val bot = PKBotManager.createBot(botName, botType, location)

                player.sendMessage("Spawned $botType bot: $botName")
                player.sendMessage("Location: ${location.x}, ${location.y}, ${location.plane}")

            } catch (e: IllegalArgumentException) {
                player.sendMessage("Invalid bot type: $botTypeStr")
                player.sendMessage("Available types: MELEE_DDS, MELEE_WHIP, RANGER_MSB, HYBRID_AGS, DH_BOT, HYBRID_ADVANCED, MAGE_BOT")
            } catch (e: Exception) {
                player.sendMessage("Error spawning bot: ${e.message}")
            }
        }

        /**
         * Command to test loadout system
         */
        Command(PlayerPrivilege.ADMINISTRATOR, "testloadout", "Test loadout system") { player, args ->
            val botName = if (args.isNotEmpty()) args[0] else "PKBot"
            val bot = PKBotManager.getActiveBots()[botName]

            if (bot == null) {
                player.sendMessage("Bot '$botName' not found. Available bots: ${PKBotManager.getActiveBots().keys.joinToString(", ")}")
                return@Command
            }

            val loadout = bot.getLoadout()
            val config = bot.getConfiguration()

            player.sendMessage("=== Loadout Test for $botName ===")
            player.sendMessage("Bot Type: ${bot.getBotType()}")
            player.sendMessage("Loadout: ${loadout?.name ?: "None"}")

            if (loadout != null) {
                player.sendMessage("Equipment slots: ${loadout.equipment.size}")
                player.sendMessage("Inventory items: ${loadout.inventory.size}")

                val specialConfig = loadout.specialAttackConfig
                player.sendMessage("Special Attack:")
                player.sendMessage("  Weapon: ${specialConfig.weaponId}")
                player.sendMessage("  Energy Required: ${specialConfig.energyRequired}%")
                player.sendMessage("  Target HP Threshold: ${specialConfig.targetHealthThreshold}%")
                player.sendMessage("  Consecutive Specs: ${specialConfig.consecutiveSpecs}")
                player.sendMessage("  Max Consecutive: ${specialConfig.maxConsecutiveSpecs}")

                val combatConfig = loadout.combatConfig
                player.sendMessage("Combat Config:")
                player.sendMessage("  Max Distance: ${combatConfig.maxCombatDistance}")
                player.sendMessage("  Retreat HP: ${combatConfig.retreatHealthPercentage}%")
                player.sendMessage("  Eat HP: ${combatConfig.eatHealthPercentage}%")
                player.sendMessage("  Use Prayers: ${combatConfig.usePrayers}")
                player.sendMessage("  Aggressive: ${combatConfig.aggressiveMode}")
            }
        }
    }
} 