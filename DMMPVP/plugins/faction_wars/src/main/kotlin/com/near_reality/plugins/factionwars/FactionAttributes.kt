package com.near_reality.plugins.factionwars

import com.zenyte.game.world.entity.persistentAttribute
import com.zenyte.game.world.entity.player.Player

/**
 * Faction membership stored on the player object.
 * `""` means the player has not yet chosen a faction (first login prompt pending).
 */
var Player.factionName: String by persistentAttribute("faction-name", "")

/** Personal war points this player has contributed to their faction this week. */
var Player.factionWarPoints: Int by persistentAttribute("faction-war-points", 0)

/** Whether the player is currently in the Zelus Sanctum winner zone. */
var Player.hasWinnerZoneAccess: Boolean by persistentAttribute("faction-winner-zone", false)
