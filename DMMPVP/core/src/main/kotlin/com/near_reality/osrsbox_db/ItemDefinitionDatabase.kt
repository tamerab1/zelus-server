package com.near_reality.osrsbox_db

import mgi.types.config.items.ItemDefinitions
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Jire
 */
object ItemDefinitionDatabase : AbstractDefinitionDatabase<ItemDefinition>(
    ItemDefinition::class.java,
    "data/osrsbox-db/items-complete.json"
) {

    override val logger: Logger = LoggerFactory.getLogger(ItemDefinitionDatabase::class.java)

    override fun buildConfigs() {
        for ((id, def) in definitions) {
            def.apply(id)
        }
        // Ancient Godsword (26233) - forced bonuses after all JSON loading
        // Order: stab, slash, crush, magic_atk, range_atk, def_stab, def_slash, def_crush, def_magic, def_range, strength, range_str, magic_dmg, prayer
        ItemDefinitions.get(26233)?.setBonuses(intArrayOf(0, 136, 83, 0, 0, 0, 0, 0, 12, 0, 142, 0, 0, 12))
        ItemDefinitions.get(27184)?.setBonuses(intArrayOf(0, 136, 83, 0, 0, 0, 0, 0, 12, 0, 142, 0, 0, 12))
    }

}