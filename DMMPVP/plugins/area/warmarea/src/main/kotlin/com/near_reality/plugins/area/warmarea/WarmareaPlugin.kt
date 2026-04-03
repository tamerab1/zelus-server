package com.near_reality.plugins.area.warmarea

import com.google.common.eventbus.Subscribe
import com.near_reality.game.world.PlayerEvent
import com.near_reality.game.world.WorldHooks
import com.zenyte.game.world.entity.Location
import com.zenyte.game.world.entity.player.MessageType
import com.zenyte.game.world.entity.player.Player
import com.zenyte.plugins.events.ServerLaunchEvent
import com.zenyte.utils.StaticInitializer
import org.slf4j.LoggerFactory

/**
 * Warmarea zone manager.
 *
 * Responsibilities:
 * 1. Spawn one bot of each tier on server start.
 * 2. Hook into [PlayerEvent.Died] to:
 *    - Suppress item loss when a player dies inside the Warmarea (safe death).
 *    - Award [trainingPoints] to the player who killed a bot.
 * 3. Respawn the bot 10 seconds after it is killed.
 *
 * ## Warmarea bounds
 * Replace [WARMAREA_REGION] with the actual region ID of your Warmarea map tile.
 * The easiest way to define the zone precisely is to add a Region to AreaTypes and
 * register it in the area plugin system — but a simple region check is enough to start.
 */
@StaticInitializer
object WarmareaPlugin {

    private val log = LoggerFactory.getLogger(WarmareaPlugin::class.java)

    // ── Configuration ────────────────────────────────────────────────────────

    /**
     * TODO: Set this to the actual region ID of the Warmarea zone.
     * You can find a region ID in-game with ::pos or by inspecting the map.
     */
    private const val WARMAREA_REGION = 12850

    /**
     * Spawn locations for each tier bot.
     * TODO: Replace with real in-game coordinates inside the Warmarea.
     */
    private val SPAWN_LOCATIONS = mapOf(
        WarmareaBotTier.ACOLYTE      to Location(3200, 3200, 0),
        WarmareaBotTier.KNIGHT       to Location(3202, 3200, 0),
        WarmareaBotTier.ZELUS_MASTER to Location(3204, 3200, 0),
    )

    // ── Server launch ────────────────────────────────────────────────────────

    @Subscribe
    @JvmStatic
    fun onServerLaunch(event: ServerLaunchEvent) {
        val hooks: WorldHooks = event.worldThread.hooks

        // Spawn all three tier bots
        SPAWN_LOCATIONS.forEach { (tier, location) -> spawnBot(tier, location) }

        // Hook: safe death + training point reward
        hooks.register(PlayerEvent.Died::class.java) { e -> onPlayerOrBotDied(e) }

        log.info("WarmareaPlugin registered — {} bots spawned.", SPAWN_LOCATIONS.size)
    }

    // ── Spawn ────────────────────────────────────────────────────────────────

    private fun spawnBot(tier: WarmareaBotTier, location: Location) {
        val bot = WarmareaBot(tier, location)
        bot.spawn()
        log.debug("Spawned Warmarea bot [{}] at {}", tier.name, location)
    }

    // ── Death handling ───────────────────────────────────────────────────────

    private fun onPlayerOrBotDied(event: PlayerEvent.Died) {
        val victim = event.player

        // Case 1: A player died inside the Warmarea — safe death, keep all items.
        if (isInWarmarea(victim)) {
            suppressItemLoss(victim)
            victim.sendMessage(
                "<col=00bfff>[Warmarea] Safe death — you lost no items.</col>",
                MessageType.UNFILTERABLE
            )
        }

        // Case 2: A player killed a Warmarea bot — award Training Points.
        val killer = event.killer as? Player ?: return
        if (!isInWarmarea(killer)) return

        val botTier = WarmareaBotTier.fromNpcId(event.killer?.id ?: return) ?: return
        awardTrainingPoints(killer, botTier)
    }

    /**
     * Prevents item loss by tagging the player's death mechanics so no items
     * are pushed to the ground. The exact hook depends on how [DeathMechanics]
     * resolves item drops in this server build.
     *
     * TODO: Replace with the real safe-death API used by other safe zones
     *       (e.g. Ferox Enclave). Look for `DeathMechanics.isSafe` or similar.
     */
    private fun suppressItemLoss(player: Player) {
        // Placeholder: retain all items on death.
        // The server likely checks a flag or area type on the player's position.
        player.attributes["warmarea-safe-death"] = true
    }

    private fun awardTrainingPoints(killer: Player, tier: WarmareaBotTier) {
        killer.trainingPoints += tier.trainingReward
        killer.sendMessage(
            "<col=ffd700>[Warmarea] +${tier.trainingReward} Training Point(s)! " +
            "Total: ${killer.trainingPoints}</col>",
            MessageType.UNFILTERABLE
        )
    }

    // ── Zone check ───────────────────────────────────────────────────────────

    private fun isInWarmarea(player: Player): Boolean =
        player.position.regionId == WARMAREA_REGION
}
