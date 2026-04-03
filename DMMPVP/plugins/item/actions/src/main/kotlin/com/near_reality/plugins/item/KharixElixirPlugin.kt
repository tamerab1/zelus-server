package com.near_reality.plugins.item

import com.zenyte.game.item.Item
import com.zenyte.game.model.item.pluginextensions.ItemPlugin
import com.zenyte.game.world.entity.player.Player

/**
 * Handles the Kharix Elixir drinking behaviour.
 *
 * Doses:
 *   22667 = 4/4 (drinking this gives Mimic Casket 23184)
 *   22668 = 1/4
 *   22669 = 2/4
 *   22670 = 3/4
 *
 * @author Zelus Dev
 */
@Suppress("unused")
class KharixElixirPlugin : ItemPlugin() {

    override fun handle() {
        bind("Drink") { player, item, _, slot ->
            drinkElixir(player, item, slot)
        }
    }

    override fun getItems(): IntArray = intArrayOf(
        22667, // Kharix Elixir 4/4
        22668, // Kharix Elixir 3/4
        22669, // Kharix Elixir 2/4
        22670  // Kharix Elixir 1/4
    )

    private fun drinkElixir(player: Player, item: Item, slot: Int) {
        // Remove current dose
        player.inventory.deleteItem(slot, Item(item.id, 1))

        when (item.id) {
            22667 -> {
                // 4/4 - give Mimic Casket
                player.inventory.addItem(Item(23184))
                player.sendMessage("You have consumed the entire Kharix Elixir. A portal shimmers before you...")
            }
            22668 -> {
                // 3/4 -> leftover is 2/4
                player.inventory.addItem(Item(22669))
                player.sendMessage("You drink some of the Kharix Elixir. You feel a dark energy pulling at you...")
                player.sendMessage("You have 2 doses of potion left.")
            }
            22669 -> {
                // 2/4 -> leftover is 1/4
                player.inventory.addItem(Item(22670))
                player.sendMessage("You drink some of the Kharix Elixir. You feel a dark energy pulling at you...")
                player.sendMessage("You have 1 dose of potion left.")
            }
            22670 -> {
                // 1/4 -> empty, no leftover
                player.sendMessage("You drink the last of the Kharix Elixir.")
            }
        }
    }
}