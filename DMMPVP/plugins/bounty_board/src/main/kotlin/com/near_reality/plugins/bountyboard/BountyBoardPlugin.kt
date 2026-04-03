package com.near_reality.plugins.bountyboard

import com.google.common.eventbus.Subscribe
import com.near_reality.api.dao.Db
import com.near_reality.game.item.CustomObjectId
import com.near_reality.game.world.PlayerEvent
import com.near_reality.game.world.WorldHooks
import com.zenyte.game.GameInterface
import com.zenyte.game.model.ui.NewInterfaceHandler
import com.zenyte.game.world.World
import com.zenyte.game.world.`object`.ObjectAction
import com.zenyte.game.world.`object`.ObjectHandler
import com.zenyte.game.world.`object`.WorldObject
import com.zenyte.game.world.entity.Location
import com.zenyte.game.world.entity.player.Player
import com.zenyte.game.world.region.area.wilderness.WildernessArea
import com.zenyte.plugins.events.ServerLaunchEvent
import com.zenyte.utils.StaticInitializer
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

/**
 * Entry point for the Bounty Board system.
 *
 * On server launch this class:
 * 1. Creates the [ActiveBounties] DB table.
 * 2. Registers [BountyBoardInterface] with [NewInterfaceHandler].
 * 3. Registers [BountyBoardObjectAction] with [ObjectHandler] — no PluginScanner run needed.
 * 4. Spawns the BH Board crate in the world.
 * 5. Hooks into [PlayerEvent.Died] to resolve Wilderness bounty claims.
 */
@StaticInitializer
object BountyBoardPlugin {

    private val log = LoggerFactory.getLogger(BountyBoardPlugin::class.java)

    /**
     * World location of the Bounty Board crate.
     * TODO: Change to your preferred coordinates.
     */
    private val BOARD_LOCATION = Location(3108, 3490, 0)

    @Subscribe
    @JvmStatic
    fun onServerLaunch(event: ServerLaunchEvent) {
        // 1. DB
        transaction(Db.mainDatabase) { create(ActiveBounties) }

        // 2. Interface
        NewInterfaceHandler.add(BountyBoardInterface::class.java)

        // 3. ObjectAction — manual registration bypasses PluginScanner
        ObjectHandler.add(BountyBoardObjectAction::class.java)

        // 4. Spawn crate object in the world
        World.spawnObject(WorldObject(CustomObjectId.BH_BOARD, 10, 0, BOARD_LOCATION))

        // 5. Kill hook
        val hooks: WorldHooks = event.worldThread.hooks
        hooks.register(PlayerEvent.Died::class.java) { e -> onPlayerDied(e) }

        log.info("BountyBoardPlugin registered — board spawned at $BOARD_LOCATION")
    }

    private fun onPlayerDied(event: PlayerEvent.Died) {
        val victim = event.player
        val killer = event.killer as? Player ?: return
        if (!WildernessArea.isWithinWilderness(victim.position)) return
        BountyManager.onPvpKill(killer, victim)
    }
}

// ── Object action ────────────────────────────────────────────────────────────

/**
 * Handles right-click interactions on the Bounty Board crate (object ID 55003).
 *
 * Registered manually in [BountyBoardPlugin.onServerLaunch] via [ObjectHandler.add] —
 * does NOT require running PluginScanner.
 *
 * Options defined on the object in the cache:
 *   Option 1 "View"         → opens the Active Bounties tab.
 *   Option 2 "Place-Bounty" → opens directly on the Place Bounty form.
 */
class BountyBoardObjectAction : ObjectAction {

    override fun handleObjectAction(
        player: Player?,
        `object`: WorldObject?,
        name: String?,
        optionId: Int,
        option: String?
    ) {
        if (player == null) return
        if (option == "Place-Bounty") {
            player.temporaryAttributes["bb_pre_place"] = true
        }
        GameInterface.BOUNTY_BOARD.open(player)
    }

    override fun getObjects(): Array<Any> = arrayOf(CustomObjectId.BH_BOARD)
}
