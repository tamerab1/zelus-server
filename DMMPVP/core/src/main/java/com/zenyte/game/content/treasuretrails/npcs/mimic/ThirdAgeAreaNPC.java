package com.zenyte.game.content.treasuretrails.npcs.mimic;

import com.zenyte.game.util.Direction;
import com.zenyte.game.world.entity.Location;
import com.zenyte.game.world.entity.npc.NPC;
import com.zenyte.game.world.entity.npc.Spawnable;

/**
 * Overworld 3rd Age NPC (warrior / ranger / mage) used in the Kharix Elixir farming area.
 * These NPCs are passive: they do not initiate combat, but will retaliate if attacked.
 *
 * Note: The identical NPC IDs inside the Mimic fight instance are spawned as
 * {@link ThirdAgeMonster} directly (bypassing this class), so they remain aggressive.
 */
public class ThirdAgeAreaNPC extends NPC implements Spawnable {

    public ThirdAgeAreaNPC(final int id, final Location tile, final Direction direction, final int radius) {
        super(id, tile, direction, radius);
    }

    @Override
    public boolean checkAggressivity() {
        return false;
    }

    @Override
    public boolean validate(final int id, final String name) {
        return id == ThirdAgeMonster.WARRIOR || id == ThirdAgeMonster.RANGER || id == ThirdAgeMonster.MAGE;
    }
}
