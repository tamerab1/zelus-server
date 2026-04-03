package com.zenyte.game.world.entity.player.bounty;

import com.google.common.collect.Lists;
import com.zenyte.game.item.Item;
import com.zenyte.game.packet.PacketDispatcher;
import com.zenyte.game.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Bounty Hunter Shop Manager
 * Werkt hetzelfde principe als UpgradeManager, maar simpeler:
 * - Geen kansberekening
 * - Geen required items
 * - Alleen kosten in BH points
 */
public class BHShopManager {

    private transient Player player;
    private BHCategory lastCategory = BHCategory.WEAPON;

    public BHShopManager(final Player player) {
        this.player = player;
    }

    public void initialize(final BHShopManager manager) {
        if (manager == null) {
            return;
        }
        lastCategory = manager.lastCategory;
    }

    private static final int BH_INTERFACE = 5125; // jouw shop interface ID
    private static final int ITEMS_COMPONENT_START = 38; // waar je items wilt tekenen

    public void open() {
        BHCategory category = lastCategory != null ? lastCategory : BHCategory.WEAPON;
        lastCategory = category;
        sendItems(player);
        clearInfo(player);
    }

    public void selectCategory(BHCategory category, Player player) {
        lastCategory = category;
        sendItems(player);
        clearInfo(player);
    }

    public void buy(Player player, int slotId) {
        List<BHShopItems> items = forCategory(lastCategory);
        if (slotId < 0 || slotId >= items.size()) {
            return;
        }
        BHShopItems shopItem = items.get(slotId);

        if (player.getBountyHunterPoints() >= shopItem.cost) {
            player.setBountyHunterPoints(player.getBountyHunterPoints() - shopItem.cost);
            player.getInventory().addOrDrop(new Item(shopItem.id, 1));
            player.sendMessage("You bought a " + new Item(shopItem.id, 1).getName()
                    + " for " + shopItem.cost + " BH points.");
        } else {
            player.sendMessage("You don't have enough BH points.");
        }
    }

    private static ArrayList<BHShopItems> forCategory(BHCategory category) {
        ArrayList<BHShopItems> items = Lists.newArrayList();
        for (BHShopItems item : BHShopItems.values()) {
            if (item.category.equals(category)) {
                items.add(item);
            }
        }
        return items;
    }

    private void sendItems(Player player) {
        List<BHShopItems> items = forCategory(lastCategory);
        int size = items.size();
        int offset = ITEMS_COMPONENT_START;

        for (int i = 0; i < size; i++) {
            player.getPacketDispatcher().sendItems(BH_INTERFACE, offset++, 0, new Item(items.get(i).id));
        }
        // leegmaken van overige slots
        for (int i = offset; i < ITEMS_COMPONENT_START + 50; i++) { // max 50 items bijvoorbeeld
            player.getPacketDispatcher().sendItems(BH_INTERFACE, i, 0, new Item(-1));
        }
    }

    private void clearInfo(Player player) {
        PacketDispatcher packetDispatcher = player.getPacketDispatcher();
        // optioneel tekst resetten als je een component hebt voor info
        packetDispatcher.sendComponentText(BH_INTERFACE, 40, "");
    }
}
