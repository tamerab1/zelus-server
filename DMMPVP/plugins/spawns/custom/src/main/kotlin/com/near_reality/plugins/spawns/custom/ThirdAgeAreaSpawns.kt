package com.near_reality.plugins.spawns.custom

import com.near_reality.scripts.npc.spawns.NPCSpawnsScript
import com.zenyte.game.util.Direction.NORTH
import com.zenyte.game.util.Direction.SOUTH
import com.zenyte.game.util.Direction.EAST
import com.zenyte.game.util.Direction.WEST
import com.zenyte.game.world.entity.npc.NpcId.THIRD_AGE_WARRIOR
import com.zenyte.game.world.entity.npc.NpcId.THIRD_AGE_RANGER
import com.zenyte.game.world.entity.npc.NpcId.THIRD_AGE_MAGE

/**
 * Spawns for the 3rd Age NPC area.
 * These NPCs drop Kharix Elixir doses used to enter the Mimic boss fight.
 */
class ThirdAgeAreaSpawns : NPCSpawnsScript() {
    init {
        // 3rd Age Warriors (melee) - main area
        THIRD_AGE_WARRIOR(3369, 3408, 0, SOUTH, 3)
        THIRD_AGE_WARRIOR(3369, 3406, 0, SOUTH, 3)
        THIRD_AGE_WARRIOR(3369, 3426, 0, SOUTH, 3)

        // 3rd Age Rangers (ranged) - main area
        THIRD_AGE_RANGER(3361, 3409, 0, EAST, 3)
        THIRD_AGE_RANGER(3361, 3405, 0, EAST, 3)

        // 3rd Age Mages (magic) - main area
        THIRD_AGE_MAGE(3354, 3406, 0, SOUTH, 3)
        THIRD_AGE_MAGE(3353, 3410, 0, SOUTH, 3)

        // Mixed group - one of each type
        THIRD_AGE_WARRIOR(3356, 3427, 0, SOUTH, 3)
        THIRD_AGE_RANGER(3357, 3427, 0, SOUTH, 3)
        THIRD_AGE_MAGE(3358, 3427, 0, SOUTH, 3)
    }
}
