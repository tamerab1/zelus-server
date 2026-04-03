package com.zenyte.game.content

import com.near_reality.scripts.npc.drops.NPCDropTableScript
import com.near_reality.scripts.npc.drops.table.DropTableType.Always
import com.near_reality.scripts.npc.drops.table.DropTableType.Main
import com.near_reality.scripts.npc.drops.table.DropTableType.Unique
import com.near_reality.scripts.npc.drops.table.always
import com.zenyte.game.item.ItemId.BLOOD_MONEY
import com.zenyte.game.item.ItemId.COINS_995
import com.zenyte.game.item.ItemId.RUNE_SWORD
import com.zenyte.game.item.ItemId.RUNE_FULL_HELM
import com.zenyte.game.item.ItemId.RUNE_PLATEBODY
import com.zenyte.game.item.ItemId.RUNE_PLATELEGS
import com.zenyte.game.item.ItemId.DEATH_RUNE
import com.zenyte.game.item.ItemId.BLOOD_RUNE
import com.zenyte.game.item.ItemId.SOUL_RUNE
import com.zenyte.game.world.entity.npc.NpcId.THIRD_AGE_WARRIOR
import com.zenyte.game.world.entity.npc.NpcId.THIRD_AGE_RANGER
import com.zenyte.game.world.entity.npc.NpcId.THIRD_AGE_MAGE

// Kharix Elixir item IDs
private const val KHARIX_ELIXIR_3_4 = 22668
private const val KHARIX_ELIXIR_2_4 = 22669
private const val KHARIX_ELIXIR_1_4 = 22670

/**
 * Drop table for the 3rd Age area NPCs.
 * These NPCs drop Kharix Elixir doses that can be combined into a 4/4
 * to receive the Mimic Casket and enter the Mimic boss fight.
 */
class ThirdAgeElixirDropTable : NPCDropTableScript() {
    init {
        npcs(THIRD_AGE_WARRIOR, THIRD_AGE_RANGER, THIRD_AGE_MAGE)

        buildTable {
            Always {
                BLOOD_MONEY quantity 15..40 rarity always
            }
            Unique(128) {
                KHARIX_ELIXIR_3_4 quantity 1 rarity 10  // 3/4 dose - main elixir drop
                KHARIX_ELIXIR_2_4 quantity 1 rarity 15  // 2/4 dose
                KHARIX_ELIXIR_1_4 quantity 1 rarity 20  // 1/4 dose
            }
            Main(64) {
                RUNE_PLATEBODY quantity 1 rarity 3
                RUNE_PLATELEGS quantity 1 rarity 3
                RUNE_FULL_HELM quantity 1 rarity 4
                RUNE_SWORD quantity 1 rarity 5
                DEATH_RUNE quantity 30..60 rarity 6
                BLOOD_RUNE quantity 15..30 rarity 5
                SOUL_RUNE quantity 20..40 rarity 5
                COINS_995 quantity 5000..15000 rarity 8
            }
        }
    }
}
