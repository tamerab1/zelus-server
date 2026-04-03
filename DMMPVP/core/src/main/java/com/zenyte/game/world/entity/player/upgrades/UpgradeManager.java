package com.zenyte.game.world.entity.player.upgrades;

import com.google.common.collect.Lists;
import com.zenyte.game.item.Item;
import com.zenyte.game.packet.PacketDispatcher;
import com.zenyte.game.util.Colour;
import com.zenyte.game.util.Utils;
import com.zenyte.game.world.broadcasts.BroadcastType;
import com.zenyte.game.world.broadcasts.WorldBroadcasts;
import com.zenyte.game.world.entity.player.Player;
import mgi.types.config.items.ItemDefinitions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Advo on 11/18/2023
 */
public class UpgradeManager {

    private transient Player player;
    private UpgradeCategory lastCategory = UpgradeCategory.WEAPON;

    public UpgradeManager(final Player player) {
        this.player = player;
    }

    public void initialize(final UpgradeManager manager) {
        if (manager == null) {
            return;
        }
        lastCategory = manager.lastCategory;
    }

    public void upgrade(Player player, int option) {
        if(option != 1) {
            return;
        }
        if (upgrading) {
            player.sendMessage("You are already trying to upgrade an item!");
            return;
        }

        if (selectedItem == null) {
            return;
        }

        Item[] requiredIds = selectedItem.required;
        player.getInventory().deleteItemsIfContains(requiredIds, () -> {
            upgrading = true;
            player.lock();
            Item reward = new Item(selectedItem.id, 1);
            if (Utils.percentage(selectedItem.chance)) {
                player.getInventory().addOrDrop(reward);
                player.sendMessage(Colour.RS_GREEN.wrap("You have successfully upgraded a " + reward.getDefinitions().getName() + "."));
                WorldBroadcasts.sendMessage(player.getName() + " has just successfully upgraded a " + reward.getDefinitions().getName() + "!", BroadcastType.RARE_DROP, false);
            } else {
                player.sendMessage(Colour.RED.wrap("You have failed to upgrade a " + reward.getDefinitions().getName() + "."));
            }
            player.unlock();
            upgrading = false;
        });
    }

    public void selectCategory(UpgradeCategory category, Player player) {
        lastCategory = category;
        selectedItem = null;
        sendItems(player);
        sendInfo(true, player);
        clearRequired(player);
    }

    public void open() {
        UpgradeCategory lastCategory1 = lastCategory != null ? lastCategory : UpgradeCategory.WEAPON;
        lastCategory = lastCategory1;
        selectedItem = null;
        sendItems(player);
        sendInfo(true, player);
        clearRequired(player);
    }

    private static final int UPGRADE_INTERFACE = 1018;
    private UpgradableItems selectedItem;
    private boolean upgrading = false;


    private static ArrayList<UpgradableItems> forCategory(UpgradeCategory category) {
        ArrayList<UpgradableItems> items = Lists.newArrayList();
        for (UpgradableItems item : UpgradableItems.values()) {
            if (item.category.equals(category)) {
                items.add(item);
            }
        }
        return items;
    }

    private void sendItems(Player player) {
        List<UpgradableItems> items = forCategory(lastCategory);
        int size = items.size();
        int offset = 55;
        for(int i = 0; i < size; i++) {
            player.getPacketDispatcher().sendItems(UPGRADE_INTERFACE, offset++, 0, new Item(items.get(i).id));
        }
        for(int i = offset; i < 95; i++) {
            player.getPacketDispatcher().sendItems(UPGRADE_INTERFACE, i, 0, new Item(-1));
        }
    }

    public void checkInterface(Player player, int itemId) {
        List<UpgradableItems> itemList = forCategory(lastCategory);
        for (UpgradableItems item : itemList) {
            if (itemId == item.id) {
                selectedItem = item;
                sendInfo(false, player);
                sendRequired(player, selectedItem);
                return;
            }
        }
        ItemDefinitions def = ItemDefinitions.get(itemId);
        if (def == null)
            return;
        player.sendMessage((def.getExamine() == null ? "This item has no examine" : def.getExamine()) + ".");
    }

    private void sendInfo(boolean clear, Player player) {
        PacketDispatcher packetDispatcher = player.getPacketDispatcher();
        packetDispatcher.sendComponentText(UPGRADE_INTERFACE, 106, clear ? "0%" : selectedItem.chance + "%");
    }

    private static void clearRequired(Player player) {
        player.getPacketDispatcher().sendItems(UPGRADE_INTERFACE, 112, 0, new Item(-1));
        player.getPacketDispatcher().sendItems(UPGRADE_INTERFACE, 116, 0, new Item(-1));
        player.getPacketDispatcher().sendItems(UPGRADE_INTERFACE, 120, 0, new Item(-1));
        player.getPacketDispatcher().sendItems(UPGRADE_INTERFACE, 104, 0, new Item(-1));
    }

    private static void sendRequired(Player player, UpgradableItems selectedItem) {
        Item[] requiredIds = selectedItem.required;
        player.getPacketDispatcher().sendItems(UPGRADE_INTERFACE, 104, 0, new Item(selectedItem.id));

        if (requiredIds.length == 1) {
            player.getPacketDispatcher().sendItems(UPGRADE_INTERFACE, 112, 0, requiredIds[0]);
            player.getPacketDispatcher().sendItems(UPGRADE_INTERFACE, 116, 0, new Item(-1));
            player.getPacketDispatcher().sendItems(UPGRADE_INTERFACE, 120, 0, new Item(-1));
        } else if (requiredIds.length == 2) {
            player.getPacketDispatcher().sendItems(UPGRADE_INTERFACE, 112, 0, requiredIds[0]);
            player.getPacketDispatcher().sendItems(UPGRADE_INTERFACE, 116, 0, requiredIds[1]);
            player.getPacketDispatcher().sendItems(UPGRADE_INTERFACE, 120, 0, new Item(-1));
        } else if (requiredIds.length == 3) {
            player.getPacketDispatcher().sendItems(UPGRADE_INTERFACE, 112, 0, requiredIds[0]);
            player.getPacketDispatcher().sendItems(UPGRADE_INTERFACE, 116, 0, requiredIds[1]);
            player.getPacketDispatcher().sendItems(UPGRADE_INTERFACE, 120, 0, requiredIds[2]);
        } else {
            System.out.println("Pac fucked up...");
        }
    }
}
