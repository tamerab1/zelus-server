package com.near_reality.game.content.thirdage

import com.google.common.eventbus.Subscribe
import com.zenyte.game.world.World
import com.zenyte.game.world.entity.Location
import com.zenyte.game.world.`object`.ObjectId
import com.zenyte.game.world.`object`.WorldObject
import com.zenyte.plugins.events.ServerLaunchEvent

/**
 * Spawns objects for the 3rd Age NPC area on server launch.
 * Bank chests allow players to manage their inventory while farming Kharix Elixir doses.
 */
@Suppress("unused", "UNUSED_PARAMETER")
object ThirdAgeAreaModule {

    @Subscribe
    @JvmStatic
    fun onServerLaunchEvent(event: ServerLaunchEvent) {
        spawnObjects()
    }

    private fun spawnObjects() {
        World.spawnObject(WorldObject(id = ObjectId.BANK_CHEST, tile = Location(3365, 3397, 0)))
        World.spawnObject(WorldObject(id = ObjectId.BANK_CHEST, tile = Location(3359, 3397, 0)))
    }
}
