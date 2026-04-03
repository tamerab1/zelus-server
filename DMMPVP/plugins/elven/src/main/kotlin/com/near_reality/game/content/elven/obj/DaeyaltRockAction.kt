package com.near_reality.game.content.elven.obj

import com.near_reality.game.content.crystal.TrahaearnMineRocks
import com.zenyte.game.content.skills.mining.MiningDefinitions
import com.zenyte.game.content.skills.mining.actions.Mining
import com.zenyte.game.content.skills.mining.actions.Prospect
import com.zenyte.game.world.entity.player.Player
import com.zenyte.game.world.`object`.ObjectAction
import com.zenyte.game.world.`object`.WorldObject

/**
 * Handles the mining of rocks in the daeyalt mine.
 *
 * @author Xander
 */
@Suppress("UNUSED")
class DaeyaltRockAction : ObjectAction {

    override fun handleObjectAction(
        player: Player,
        obj: WorldObject,
        name: String,
        optionId: Int,
        option: String,
    ) {
        val ore = MiningDefinitions.OreDefinitions.getDef(obj.id) ?: return
        when (option) {
            "Mine" -> player.actionManager.action = Mining(obj, ore)
            "Prospect" -> player.actionManager.action = Prospect(obj, ore)
        }
    }

    override fun getObjects() = arrayOf(17962)
}

