package com.near_reality.plugins.factionwars

import com.google.common.eventbus.Subscribe
import com.near_reality.api.dao.Db
import com.near_reality.game.world.PlayerEvent
import com.near_reality.game.world.WorldHooks
import com.near_reality.scripts.npc.actions.NPCActionScript
import com.zenyte.game.world.World
import com.zenyte.game.world.broadcasts.BroadcastType
import com.zenyte.game.world.broadcasts.WorldBroadcasts
import com.zenyte.game.world.entity.player.Player
import com.zenyte.game.world.region.area.wilderness.WildernessArea
import com.zenyte.plugins.events.ServerLaunchEvent
import com.zenyte.utils.StaticInitializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

/**
 * Entry point for the Faction Wars system.
 *
 * Wires:
 * - [PlayerEvent.Died] → cross-faction kill scoring
 * - [PlayerEvent.LoggedIn] → first-login faction assignment
 * - [FactionWarScheduler] → Sunday 20:00 weekly reset
 * - Zelus Sanctum NPC → winner-zone entry guard
 */
@StaticInitializer
object FactionPlugin {

    private val log = LoggerFactory.getLogger(FactionPlugin::class.java)
    private val scope = CoroutineScope(Dispatchers.IO)

    // ── Server launch ────────────────────────────────────────────────────────

    @Subscribe
    @JvmStatic
    fun onServerLaunch(event: ServerLaunchEvent) {
        // Create tables if they don't exist yet
        transaction(Db.mainDatabase) {
            create(FactionWarScores)
            create(FactionWarHistory)
        }

        val hooks: WorldHooks = event.worldThread.hooks

        hooks.register(PlayerEvent.Died::class.java)     { e -> onPlayerDied(e) }
        hooks.register(PlayerEvent.LoggedIn::class.java) { e -> onPlayerLogin(e) }

        // Give the scheduler a live reference to the online player list
        FactionWarScheduler.onlinePlayers = { World.getPlayers().toList() }
        FactionWarScheduler.start()

        // Ensure DB rows exist for both factions
        scope.launch { FactionManager.initFactionRows() }

        log.info("FactionPlugin registered.")
    }

    // ── Login hook ───────────────────────────────────────────────────────────

    private fun onPlayerLogin(event: PlayerEvent.LoggedIn) {
        val player = event.player

        if (player.factionName.isBlank()) {
            // First login — prompt faction selection on the next tick so the
            // client is fully loaded before we send the dialogue/message.
            com.zenyte.game.task.WorldTasksManager.schedule(1) {
                showFactionSelectionPrompt(player)
            }
        } else {
            // Remind returning player of their faction
            val faction = Faction.fromName(player.factionName) ?: return
            player.sendMessage(
                "<col=${faction.color}>[Faction Wars] Welcome back, soldier of " +
                "${faction.displayName}! Personal War Points: ${player.factionWarPoints}</col>"
            )
        }
    }

    /**
     * Presents the faction allegiance choice.
     *
     * TODO: Replace the sendMessage fallback with a proper two-option dialogue
     *       (e.g. DialogueManager with two NPC response branches) once the UI exists.
     *       For now players type ::faction corrupted or ::faction lightbringers.
     */
    private fun showFactionSelectionPrompt(player: Player) {
        player.sendMessage(
            "<col=ffd700>[Faction Wars] Choose your allegiance! Type:</col>"
        )
        player.sendMessage("<col=8b0000>  ::faction corrupted</col>")
        player.sendMessage("<col=1e90ff>  ::faction lightbringers</col>")
        player.sendMessage("<col=ffd700>[Faction Wars] Your choice is permanent!</col>")
    }

    // ── Kill hook ────────────────────────────────────────────────────────────

    private fun onPlayerDied(event: PlayerEvent.Died) {
        val victim = event.player
        val killer = event.killer as? Player ?: return
        if (!WildernessArea.isWithinWilderness(victim.position)) return

        FactionManager.recordWildernessKill(killer, victim)
    }

    // ── Command entry point (wire to your command system) ───────────────────

    /**
     * Called from your server's command handler when a player types
     * `::faction <factionName>`.
     *
     * Wiring example (add to your existing CommandScript):
     * ```kotlin
     * "faction" -> FactionPlugin.handleFactionCommand(player, args)
     * ```
     */
    @JvmStatic
    fun handleFactionCommand(player: Player, args: Array<String>) {
        if (player.factionName.isNotBlank()) {
            player.sendMessage("<col=ff4500>[Faction Wars] You have already chosen your faction.</col>")
            return
        }
        val chosen = args.getOrNull(0)?.let { Faction.fromName(it) }
        if (chosen == null) {
            player.sendMessage("<col=ff4500>[Faction Wars] Unknown faction. Use 'corrupted' or 'lightbringers'.</col>")
            return
        }
        FactionManager.assignFaction(player)  // assignFaction reads factionName == "" as trigger
        // Directly set after the blank-check so the player gets the chosen faction, not the balanced one.
        // (assignFaction's balance logic can be replaced by this direct assignment)
        player.factionName = chosen.name
        val broadcast = "<col=${chosen.color}>[Faction Wars] " +
            "<col=ffffff>${player.name}</col>" +
            "<col=${chosen.color}> has sworn allegiance to ${chosen.displayName}!</col>"
        WorldBroadcasts.sendMessage(broadcast, BroadcastType.WILDERNESS_EVENT, false)
    }
}

// ── Zelus Sanctum NPC guard ──────────────────────────────────────────────────

/**
 * Guards the entrance to the Zelus Sanctum winner zone.
 * Only members of the winning faction who have [Player.hasWinnerZoneAccess] may enter.
 *
 * TODO: Replace [SANCTUM_GUARDIAN_NPC_ID] with the real NPC ID.
 * TODO: Replace [SANCTUM_ENTRANCE] with the real teleport coordinates.
 */
class SanctumGuardianNpcAction : NPCActionScript() {

    companion object {
        private const val SANCTUM_GUARDIAN_NPC_ID = 9700 // TODO: define in cache
    }

    init {
        npcs(SANCTUM_GUARDIAN_NPC_ID)

        "Enter" {
            if (!player.hasWinnerZoneAccess) {
                player.sendMessage(
                    "<col=ff4500>[Zelus Sanctum] Only members of the winning faction " +
                    "may enter the Sanctum this week.</col>"
                )
                return@invoke
            }
            // TODO: teleport player to the Sanctum interior coordinates
            player.sendMessage("<col=ffd700>[Zelus Sanctum] Welcome, champion.</col>")
        }

        "Talk-To" {
            val factionStr = if (player.factionName.isBlank()) "None" else player.factionName
            player.sendMessage(
                "<col=ffd700>[Sanctum Guardian] Your faction: $factionStr. " +
                "Personal war points: ${player.factionWarPoints}</col>"
            )
        }
    }
}
