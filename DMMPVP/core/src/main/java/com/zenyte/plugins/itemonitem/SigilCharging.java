package com.zenyte.plugins.itemonitem;

import com.zenyte.game.item.Item;
import com.zenyte.game.model.item.PairedItemOnItemPlugin;
import com.zenyte.game.world.entity.masks.Graphics;
import com.zenyte.game.world.entity.player.Player;

/**
 * Handles charging the unattuned sigil using blood money.
 * Unattuned Sigil ID: 28526
 * Unattuned Remote Storage Sigil ID: 26141
 * Unattuned sigil of titanium ID: 28523
 * Blood Money ID: 13307
 */
public class SigilCharging implements PairedItemOnItemPlugin {
    private static final Graphics graphics = new Graphics(1963);
    @Override
    public void handleItemOnItemAction(Player player, Item from, Item to, int fromSlot, int toSlot) {
        final Item bloodMoney = from.getId() == 13307 ? from : to;
        final Item sigil = bloodMoney == from ? to : from;
        if (sigil.getId() != 28526 && sigil.getId() != 26141 && sigil.getId() != 28523) {
            player.sendMessage("This item cannot be charged.");
            return;
        }
        if (bloodMoney.getAmount() < 10000) {
            player.sendMessage("You need at least 10,000 blood money to charge the sigil.");
            return;
        }
        player.getInventory().deleteItem(new Item(13307, 10000));
        if (sigil.getId() == 28526) {
            player.pauseSigilTimer("sigil_ninja",6L);

        } else if (sigil.getId() == 26141) {
            player.pauseSigilTimer("sigil_remote_storage",6L);

        } else if (sigil.getId() == 28523) {
            player.pauseSigilTimer("sigil_titanium",6L);

        }
        player.getInventory().refreshAll();
        player.sendFilteredMessage("You charge the sigil using 10,000 blood money, adding 6 hours of time.");
        player.setGraphics(graphics);
    }
    @Override
    public ItemPair[] getMatchingPairs() {
        return new ItemPair[]{
                ItemPair.of(13307, 28526),
                ItemPair.of(13307, 28523),
                ItemPair.of(13307, 26141)
        };
    }
}
