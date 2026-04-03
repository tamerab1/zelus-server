package com.zenyte.plugins.drop.slayer;

import com.near_reality.game.item.HiddenItems;
import com.zenyte.game.item.Item;
import com.zenyte.game.item.ItemId;
import com.zenyte.game.util.Utils;
import com.zenyte.game.world.broadcasts.BroadcastType;
import com.zenyte.game.world.broadcasts.WorldBroadcasts;
import com.zenyte.game.world.entity.npc.NPC;
import com.zenyte.game.world.entity.npc.drop.matrix.Drop;
import com.zenyte.game.world.entity.npc.drop.matrix.DropProcessor;
import com.zenyte.game.world.entity.player.Player;

/**
 * Handles Demonic Gorilla custom drop behaviour.
 *
 * @author Kris
 * Modified to include Halloween tokens.
 */
public class DemonicGorillaDropProcessor extends DropProcessor {

    @Override
    public void attach() {

        appendDrop(new DisplayedDrop(ItemId.ZENYTE_SHARD, 1, 1, 150));
        appendDrop(new DisplayedDrop(19592, 1, 1, 50)); // Limbs
        appendDrop(new DisplayedDrop(19601, 1, 1, 50)); // Spring
        appendDrop(new DisplayedDrop(19586, 1, 1, 50)); // Light frame
        appendDrop(new DisplayedDrop(19589, 1, 1, 50)); // Heavy frame
        appendDrop(new DisplayedDrop(19610, 1, 1, 50)); // Monkey tail

        appendDrop(new DisplayedDrop(13307, 50, 100, 1));

        appendDrop(new DisplayedDrop(30116, 50, 350, 1));
    }

    @Override
    public void onDeath(final NPC npc, final Player killer) {
        // Rare meme drop
        if (randomDrop(killer, 12500) == 0) {
            npc.dropItem(killer, new Item(HiddenItems.GNOME_SCARF));
            WorldBroadcasts.broadcast(killer, BroadcastType.SUPER_RARE_DROP,
                    " just killed a Demonic Gorilla and found ... a gnome's scarf?");
        }

        int bloodMoneyAmount = Utils.random(50, 100);
        npc.dropItem(killer, new Item(13307, bloodMoneyAmount));


        int tokenAmount = Utils.random(50, 350);
        npc.dropItem(killer, new Item(30116, tokenAmount));
    }

    @Override
    public Item drop(final NPC npc, final Player killer, final Drop drop, final Item item) {
        if (!drop.isAlways()) {
            int random = random(100);
            if (random <= 1) {
                return random == 0 ? new Item(19589) : new Item(19610);
            }
            random = random(50);
            if (random == 0) {
                return new Item(19586);
            }
            random = random(100);
            if (random <= 1) {
                return random == 0 ? new Item(19592) : new Item(19601);
            }
            if (random(150) == 0) {
                return new Item(ItemId.ZENYTE_SHARD);
            }
        }
        return super.drop(npc, killer, drop, item);
    }

    @Override
    public int[] ids() {
        return new int[]{7144, 7145, 7146, 7147, 7148, 7149, 7152};
    }
}
