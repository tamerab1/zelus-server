package com.zenyte.game.content

import com.near_reality.game.item.CustomItemId
import com.near_reality.scripts.npc.drops.NPCDropTableScript
import com.near_reality.scripts.npc.drops.table.DropTableType.*
import com.near_reality.scripts.npc.drops.table.noted
import com.zenyte.game.item.ItemId
import com.zenyte.game.util.Colour
import com.zenyte.game.world.entity.npc.NpcId.*
import mgi.utilities.StringFormatUtil

@Suppress("unused")
class AvianseDropTable : NPCDropTableScript() {

    init {
        // Icefiend variants this table applies to.
        npcs(HARPIE_EAGLE)

        // ---- MVP + top-damage flow (ported from Gano-style script) ----
        onDeath {
            killer.sendDeveloperMessage("Killed NPC id=${npc.id}")

            if (playerDamageContributions.isEmpty()) {
                killer.sendDeveloperMessage("No damage dealers recorded; skipping Harpie Eagle drops.")
                return@onDeath
            }

            // MVP = highest damage contributor
            val mvp = playerDamageContributions.maxBy { it.value }.key
            mvp.sendDeveloperMessage("You are MVP (Harpie Eagle) with ${(playerDamageContributions[mvp] ?: 0.0) * 100}% damage.")

            // Rank participants by damage
            val rankedPlayers = playerDamageContributions.entries
                .sortedByDescending { it.value }
                .map { it.key }
            val top10 = rankedPlayers.take(10)

            // Award tables:
            //  - Top 3 get the independent 25%/50% rolls (Tertiary table below)
            //  - MVP additionally rolls the 0.5% uniques (Unique table below)
            top10.forEachIndexed { index, p ->
                if (index < 3) {
                    rollStaticTableAndDrop(p, type = Tertiary)
                }
                if (p == mvp) {
                    rollStaticTableAndDrop(p, type = Unique)
                }
            }

            // Broadcast MVP info to all contributors
            val pct = ((playerDamageContributions[mvp] ?: 0.0) * 100.0).toInt()
            playerDamageContributions.keys.forEach { viewer ->
                viewer.sendMessage(
                    "${mvp.name} is the MVP and dealt " +
                            Colour.ORANGE_RED.wrap(StringFormatUtil.formatNumberUS(pct)) +
                            "% of the damage to the Harpie Eagle!"
                )
            }
        }

        // ---- Drop tables ----
        buildTable {
            // MVP-ONLY: each at 0.5% (1/200)
            Unique {
                // -- existing
                ItemId.VOIDWAKER_27690         quantity 1   oneIn 12500
                ItemId.DRAGON_CLAWS            quantity 1   oneIn 1000
                CustomItemId.DEATH_CAPE        quantity 1   oneIn 30000        // adjust to your custom id if named differently
                ItemId.SCYTHE_OF_VITUR         quantity 1   oneIn 25000       // change if your constant differs

                // -- high-end raid/mega rares (very rare)
                ItemId.TWISTED_BOW             quantity 1   oneIn 25000
                ItemId.ELYSIAN_SPIRIT_SHIELD   quantity 1   oneIn 25000
                ItemId.TORVA_FULLHELM        quantity 1   oneIn 10000       // if present in your base
                ItemId.TORVA_PLATEBODY         quantity 1   oneIn 10000
                ItemId.TORVA_PLATELEGS         quantity 1   oneIn 10000
                ItemId.URSINE_CHAINMACE_U_27657 quantity 1 oneIn 2500
                ItemId.WEBWEAVER_BOW_U_27652 quantity 1 oneIn 2500
                ItemId.LIME_WHIP quantity 1 oneIn 20000
                ItemId.VOLCANIC_ABYSSAL_WHIP quantity 1 oneIn 12500
                ItemId.VESTAS_CHAINBODY quantity 1 oneIn 5000
                ItemId.VESTAS_PLATESKIRT quantity 1 oneIn 5000
                ItemId.VESTAS_LONGSWORD quantity 1 oneIn 5000
                ItemId.MASORI_BODY_F quantity 1 oneIn 7500
                ItemId.MASORI_CHAPS_F quantity 1 oneIn 7500
                ItemId.PURGING_STAFF quantity 1 oneIn 10000
                ItemId.ULTOR_RING_28307 quantity 1 oneIn 10000
                ItemId.VENATOR_RING_28310 quantity 1 oneIn 7500
                ItemId.MAGUS_RING_A quantity 1 oneIn 7500
                ItemId.PVP_MYSTERY_BOX quantity 1 oneIn 2000


                // -- top-tier PvM uniques (rare)
                ItemId.GHRAZI_RAPIER           quantity 1   oneIn 3000
                ItemId.OSMUMTENS_FANG quantity 1 oneIn 5000
                ItemId.SANGUINESTI_STAFF       quantity 1   oneIn 3500
                ItemId.KODAI_WAND              quantity 1   oneIn 3500
                ItemId.ARMADYL_GODSWORD        quantity 1   oneIn 1000
                ItemId.BANDOS_GODSWORD         quantity 1   oneIn 800
                ItemId.DRAGON_WARHAMMER        quantity 1   oneIn 1750
                ItemId.DINHS_BULWARK           quantity 1   oneIn 7500
                ItemId.RING_OF_SUFFERING       quantity 1   oneIn 1100
                ItemId.NECKLACE_OF_ANGUISH     quantity 1   oneIn 900
                ItemId.AMULET_OF_TORTURE       quantity 1   oneIn 900
            }

            //
            // Independent rolls for top 3 damage dealers
            //
            Tertiary {
                // ===================== “25% EACH” (mid/high gear & rolls) =====================
                // Higher value => rarer (larger oneIn)
                ItemId.DRAGON_HUNTER_CROSSBOW  quantity 1     oneIn 1500
                ItemId.FEROCIOUS_GLOVES        quantity 1     oneIn 2500
                ItemId.IMBUED_HEART            quantity 1     oneIn 1500
                ItemId.ARMADYL_HELMET          quantity 1     oneIn 1000
                ItemId.BASILISK_JAW            quantity 1     oneIn 1500
                ItemId.TOXIC_STAFF_OF_THE_DEAD quantity 1     oneIn 500
                ItemId.UNCUT_ONYX              quantity 1     oneIn 500
                ItemId.DRAGON_CHAINBODY        quantity 1     oneIn 250
                ItemId.DRAGON_KITESHIELD       quantity 1     oneIn 150
                ItemId.ARMADYL_CROSSBOW        quantity 1     oneIn 1500
                ItemId.ZAMORAKIAN_SPEAR        quantity 1     oneIn 500
                ItemId.SARADOMIN_SWORD         quantity 1     oneIn 200
                ItemId.CLUE_SCROLL_MASTER      quantity 1     oneIn 200
                ItemId.MYSTERY_BOX             quantity 2     oneIn 100       // skewed 40% better (original 1/25 -> ~1/15)

                // ===================== “50% EACH” (supplies & skilling) =====================
                // Lower value => more common (smaller oneIn), big stacks to feel good
                ItemId.SUPER_COMBAT_POTION4    quantity 100.noted  oneIn 8
                ItemId.ZULRAHS_SCALES          quantity 100        oneIn 8
                ItemId.SANFEW_SERUM4           quantity 100.noted  oneIn 8
                ItemId.SARADOMIN_BREW4         quantity 100.noted  oneIn 8
                ItemId.SUPER_RESTORE4          quantity 100.noted  oneIn 8
                ItemId.STAMINA_POTION4         quantity 100.noted  oneIn 8
                ItemId.PRAYER_POTION4          quantity 100.noted  oneIn 7
                ItemId.BLOOD_RUNE              quantity 500 oneIn 9
                ItemId.DEATH_RUNE              quantity 500 oneIn 9
                ItemId.SOUL_RUNE               quantity 500 oneIn 9
                ItemId.GRIMY_LANTADYME           quantity 100.noted  oneIn 8
                ItemId.GRIMY_SNAPDRAGON       quantity 50.noted  oneIn 8
                ItemId.GRIMY_TORSTOL          quantity 50.noted  oneIn 9
                ItemId.RANARR_SEED             quantity 5   oneIn 12
                ItemId.SNAPDRAGON_SEED         quantity 5   oneIn 12
                ItemId.TORSTOL_SEED            quantity 5   oneIn 14
                ItemId.MYSTERY_BOX             quantity 1          oneIn 50 // skewed 40% better (original 1/30 -> ~1/18)
                ItemId.MYSTERY_BOX             quantity 1          oneIn 50  // skewed 40% better (original 1/10 -> ~1/6)
            }
        }
    }
}
