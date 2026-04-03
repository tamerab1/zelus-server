package com.near_reality.game.content.cosmetics

import com.google.common.eventbus.Subscribe
import com.near_reality.game.world.PlayerEvent
import com.near_reality.game.world.WorldHooks
import com.zenyte.game.world.broadcasts.BroadcastType
import com.zenyte.game.world.broadcasts.WorldBroadcasts
import com.zenyte.game.world.entity.player.MessageType
import com.zenyte.game.world.entity.player.Player
import com.zenyte.game.world.region.area.wilderness.WildernessArea
import com.zenyte.plugins.events.ServerLaunchEvent
import com.zenyte.utils.StaticInitializer

/**
 * Overrides the default PvP kill message with a custom, colour-branded
 * announcement when the killer owns a "Custom Kill Announcement" perk.
 *
 * ## Integration
 * This hooks into [PlayerEvent.Died].  No changes to existing files are needed.
 *
 * ## Granting a perk
 * From the store / staff tool:
 * ```
 * KillAnnouncementHandler.grantPerk(player, KillAnnouncementPerk.OBLITERATOR)
 * ```
 *
 * ## Adding new perks
 * Add an entry to [KillAnnouncementPerk].  Each perk defines its own
 * [KillAnnouncementPerk.format] lambda so the broadcast string stays self-contained.
 */
@StaticInitializer
object KillAnnouncementHandler {

    // -----------------------------------------------------------------------
    // Perk catalogue
    // -----------------------------------------------------------------------

    /**
     * Every purchasable kill-announcement style.
     *
     * @param persistKey  the attribute key stored on the player (keep stable, it's saved to DB)
     * @param format      lambda that builds the broadcast string given (killerName, victimName)
     */
    enum class KillAnnouncementPerk(
        val persistKey: String,
        val format: (killerName: String, victimName: String) -> String
    ) {
        OBLITERATOR(
            persistKey = "kill-perk-obliterator",
            format     = { k, v -> "<col=ff4500>🔥 $k completely obliterated $v! 🔥</col>" }
        ),
        EXECUTIONER(
            persistKey = "kill-perk-executioner",
            format     = { k, v -> "<col=8b0000>⚔ $k executed $v without mercy! ⚔</col>" }
        ),
        PHANTOM(
            persistKey = "kill-perk-phantom",
            format     = { k, v -> "<col=9400d3>👻 $v never saw $k coming…</col>" }
        ),
        DARK_OVERLORD(
            persistKey = "kill-perk-darkoverlord",
            format     = { k, v -> "<col=1c1c1c>☠ The darkness claimed $v. $k reigns supreme! ☠</col>" }
        );
    }

    // -----------------------------------------------------------------------
    // Server-launch hook registration
    // -----------------------------------------------------------------------

    @Subscribe
    @JvmStatic
    fun onServerLaunch(event: ServerLaunchEvent) {
        val hooks: WorldHooks = event.worldThread.hooks
        hooks.register(PlayerEvent.Died::class.java) { e -> onPlayerDied(e) }
    }

    // -----------------------------------------------------------------------
    // Death handler
    // -----------------------------------------------------------------------

    private fun onPlayerDied(event: PlayerEvent.Died) {
        val victim = event.player
        val killer = event.killer as? Player ?: return

        // Only broadcast for PvP kills inside the Wilderness (or wherever you want).
        // Remove this guard if you want the custom message everywhere.
        if (!WildernessArea.isWithinWilderness(killer.position)) return

        val activePerk = getActivePerk(killer) ?: return

        val message = activePerk.format(killer.name, victim.name)

        // Send as an unfilterable global message so it appears in everyone's chatbox.
        WorldBroadcasts.sendMessage(message, BroadcastType.WILDERNESS_EVENT, false)
    }

    // -----------------------------------------------------------------------
    // Perk management
    // -----------------------------------------------------------------------

    /**
     * Returns the first active [KillAnnouncementPerk] the player owns, or null.
     * Checks in declaration order — the first match wins (highest rarity first if
     * you order the enum accordingly).
     */
    fun getActivePerk(player: Player): KillAnnouncementPerk? {
        return KillAnnouncementPerk.entries.firstOrNull { perk ->
            (player.attributes[perk.persistKey] as? Number)?.toInt() == 1
        }
    }

    /**
     * Grants the specified perk to a player (call this from your store handler).
     */
    @JvmStatic
    fun grantPerk(player: Player, perk: KillAnnouncementPerk) {
        player.attributes[perk.persistKey] = 1
        player.sendMessage(
            "<col=ffd700>You have unlocked the '${perk.name.replace('_', ' ')}' " +
            "kill announcement!</col>",
            MessageType.UNFILTERABLE
        )
    }

    /**
     * Revokes a perk (e.g. if the player wants to switch).
     */
    @JvmStatic
    fun revokePerk(player: Player, perk: KillAnnouncementPerk) {
        player.attributes.remove(perk.persistKey)
    }

    /**
     * Helper: checks whether the player owns a specific perk.
     */
    @JvmStatic
    fun hasPerk(player: Player, perk: KillAnnouncementPerk): Boolean {
        return (player.attributes[perk.persistKey] as? Number)?.toInt() == 1
    }
}
