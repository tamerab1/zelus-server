package com.near_reality.game.content.wilderness

import com.google.common.eventbus.Subscribe
import com.near_reality.game.world.PlayerEvent
import com.near_reality.game.world.WorldHooks
import com.near_reality.game.world.entity.player.pvpKillStreak
import com.near_reality.game.world.entity.player.pvpKills
import com.near_reality.game.world.entity.player.pvpDeaths
import com.zenyte.game.item.Item
import com.zenyte.game.world.World
import com.zenyte.game.world.broadcasts.BroadcastType
import com.zenyte.game.world.broadcasts.WorldBroadcasts
import com.zenyte.game.world.entity.masks.Graphics
import com.zenyte.game.world.entity.player.MessageType
import com.zenyte.game.world.entity.player.Player
import com.zenyte.game.world.region.area.wilderness.WildernessArea
import com.zenyte.plugins.events.ServerLaunchEvent
import com.zenyte.utils.StaticInitializer
import org.slf4j.LoggerFactory

/**
 * Handles Wilderness killstreak tracking, milestone broadcasts, skull graphics,
 * and the bounty system.
 *
 * ## Killstreak milestones
 * | Streak | Broadcast | Skull overhead |
 * |--------|-----------|----------------|
 * |   5    | Yes       | White skull    |
 * |  10    | Yes       | Red skull      |
 * |  15    | Yes       | Flaming skull  |
 *
 * ## Bounty system
 * A player with a streak of 5+ has a "Bounty" on their head.
 * Whoever kills them earns a reward (Blood Money + Mystery Box) and the streak resets.
 *
 * ## Integration
 * Hooks into [PlayerEvent.Died] which is already fired from [DeathMechanics.death].
 * No changes to existing files are needed beyond registering this handler on server
 * launch via the [onServerLaunch] subscriber.
 */
@StaticInitializer
object WildernessKillstreakHandler {

    private val log = LoggerFactory.getLogger(WildernessKillstreakHandler::class.java)

    // -----------------------------------------------------------------------
    // Configuration
    // -----------------------------------------------------------------------

    /** Item IDs for bounty rewards. Change these to match your custom item IDs. */
    private const val BLOOD_MONEY_ID = 13307      // Standard Blood Money item ID
    private const val MYSTERY_BOX_ID = 6199       // Replace with your custom Mystery Box ID

    private const val BLOOD_MONEY_REWARD = 500
    private const val MYSTERY_BOX_REWARD = 1

    /** Overhead graphics applied at each streak milestone. */
    private val SKULL_5_GFX  = Graphics(56)   // white skull
    private val SKULL_10_GFX = Graphics(37)   // red skull-ish
    private val SKULL_15_GFX = Graphics(1666) // fire/infernal skull

    private val STREAK_MILESTONES = setOf(5, 10, 15)

    // -----------------------------------------------------------------------
    // Server-launch hook registration
    // -----------------------------------------------------------------------

    @Subscribe
    @JvmStatic
    fun onServerLaunch(event: ServerLaunchEvent) {
        val hooks: WorldHooks = event.worldThread.hooks
        hooks.register(PlayerEvent.Died::class.java) { e -> onPlayerDied(e) }
        log.info("WildernessKillstreakHandler registered.")
    }

    // -----------------------------------------------------------------------
    // Core logic
    // -----------------------------------------------------------------------

    private fun onPlayerDied(event: PlayerEvent.Died) {
        val victim = event.player
        val killer = event.killer as? Player ?: return

        // Only process deaths that occur inside the Wilderness.
        if (!WildernessArea.isWithinWilderness(victim.position)) return

        val victimStreak = victim.pvpKillStreak
        val hasBounty    = victimStreak >= 5

        // ── Reset victim streak ──────────────────────────────────────────────
        victim.pvpDeaths++
        victim.pvpKillStreak = 0
        victim.sendMessage("<col=ff4500>Your killstreak has been reset.</col>")

        // ── Award bounty to killer ───────────────────────────────────────────
        if (hasBounty) {
            awardBounty(killer, victim, victimStreak)
        }

        // ── Increment killer streak ──────────────────────────────────────────
        killer.pvpKills++
        val newStreak = killer.pvpKillStreak + 1
        killer.pvpKillStreak = newStreak
        killer.sendMessage("<col=00ff00>Killstreak: $newStreak</col>")

        // ── Milestone check ──────────────────────────────────────────────────
        if (newStreak in STREAK_MILESTONES) {
            broadcastMilestone(killer, newStreak)
            applySkullGraphic(killer, newStreak)
        }
    }

    // -----------------------------------------------------------------------
    // Milestone broadcast
    // -----------------------------------------------------------------------

    private fun broadcastMilestone(player: Player, streak: Int) {
        val message = "<col=B22222>[Wilderness] ${player.name} is on a " +
                "<col=ffd700>$streak Killstreak</col> in the Wilderness!</col>"
        WorldBroadcasts.sendMessage(message, BroadcastType.WILDERNESS_EVENT, true)
    }

    // -----------------------------------------------------------------------
    // Overhead skull graphic
    // -----------------------------------------------------------------------

    /**
     * Plays a graphic over the player's head and schedules its removal after
     * a short duration so it doesn't permanently persist.
     */
    private fun applySkullGraphic(player: Player, streak: Int) {
        val gfx = when {
            streak >= 15 -> SKULL_15_GFX
            streak >= 10 -> SKULL_10_GFX
            else         -> SKULL_5_GFX
        }
        player.graphics = gfx
    }

    // -----------------------------------------------------------------------
    // Bounty reward
    // -----------------------------------------------------------------------

    private fun awardBounty(killer: Player, victim: Player, victimStreak: Int) {
        val bloodMoney = Item(BLOOD_MONEY_ID, BLOOD_MONEY_REWARD)
        val mysteryBox = Item(MYSTERY_BOX_ID, MYSTERY_BOX_REWARD)

        killer.inventory.addItem(bloodMoney)
        killer.inventory.addItem(mysteryBox)

        killer.sendMessage(
            "<col=ffd700>[Bounty] You claimed the bounty on ${victim.name}'s head " +
            "(streak: $victimStreak)! Reward: ${BLOOD_MONEY_REWARD}x Blood Money + Mystery Box.</col>",
            MessageType.UNFILTERABLE
        )

        // Global announcement so others know the streak was ended.
        WorldBroadcasts.sendMessage(
            "<col=B22222>[Bounty] ${killer.name} has ended ${victim.name}'s " +
            "$victimStreak killstreak and claimed the bounty!</col>",
            BroadcastType.WILDERNESS_EVENT, true
        )
    }
}
