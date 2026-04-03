package com.near_reality.game.content.bountyhunter


import com.zenyte.game.item.Item
import com.zenyte.game.item.ItemId
import com.zenyte.game.world.entity.player.Player
import java.util.*

/**
 *
 * @author Xander
 */
enum class BountyHunterCrate(val tier: Int, val itemId: Int) {
    T1(1, 28082),
    T2(2, 28084),
    T3(3, 28086),
    T4(4, 28088),
    T5(5, 28090),
    T6(6, 28092),
    T7(7, 28094),
    T8(8, 28096),
    T9(9, 28098);

    companion object {
        fun forTier(tier: Int): BountyHunterCrate {
            return values().first { it.tier == tier }
        }
    }
}
