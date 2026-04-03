package com.near_reality.game.content.cosmetics;

import com.zenyte.game.item.Item;
import com.zenyte.game.item.ItemId;
import com.zenyte.game.model.item.pluginextensions.ItemPlugin;
import com.zenyte.game.world.entity.masks.UpdateFlag;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.plugins.Plugin;
import com.zenyte.plugins.PluginManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Consumable "Dark Fantasy Recolor Scroll" item plugin.
 *
 * <h3>Design</h3>
 * Each scroll is a separate item ID mapped to:
 * <ol>
 *   <li>The target weapon/armour it can recolor.</li>
 *   <li>The replacement (recolored) item ID.</li>
 * </ol>
 *
 * Using the scroll on an item in the inventory checks if the player has the target
 * item equipped <em>or</em> in their inventory, then swaps it for the recolored variant.
 * The scroll is consumed on success.
 *
 * <h3>Adding new scrolls</h3>
 * <ol>
 *   <li>Add the scroll item and its recolored weapon to your cache.</li>
 *   <li>Add a {@link RecolorDef} entry to the {@link #DEFINITIONS} map below.</li>
 *   <li>Add the scroll item ID to {@link #getItems()}.</li>
 * </ol>
 *
 * <h3>Item IDs</h3>
 * Replace the placeholder constants with your actual custom item IDs from
 * {@code CustomItemId}.
 */
public class WeaponRecolorScrollPlugin extends ItemPlugin {

    // -----------------------------------------------------------------------
    // Scroll & recolored item IDs  (replace with your real custom IDs)
    // -----------------------------------------------------------------------

    // --- Scrolls ---
    /** Icy Blue recolor scroll for Armadyl Godsword */
    private static final int SCROLL_ICY_AGS           = 32100;
    /** Corrupted (dark) recolor scroll for Dragon Claws */
    private static final int SCROLL_CORRUPTED_CLAWS    = 32101;
    /** Icy Blue recolor scroll for Abyssal Whip */
    private static final int SCROLL_ICY_WHIP           = 32102;
    /** Dark Corrupted recolor scroll for Armadyl Crossbow */
    private static final int SCROLL_DARK_ACB            = 32103;

    // --- Recolored variants ---
    private static final int ICY_AGS                   = 32200; // glowing icy blue AGS
    private static final int CORRUPTED_CLAWS           = 32201; // dark corrupted Claws
    private static final int ICY_WHIP                  = 32202; // icy blue Abyssal Whip
    private static final int DARK_ACB                  = 32203; // dark Armadyl Crossbow

    // -----------------------------------------------------------------------
    // Recolor definition table
    // -----------------------------------------------------------------------

    /**
     * Maps scroll item ID → {@link RecolorDef}.
     */
    private static final Map<Integer, RecolorDef> DEFINITIONS = new HashMap<>();

    static {
        DEFINITIONS.put(SCROLL_ICY_AGS,        new RecolorDef(ItemId.ARMADYL_GODSWORD, ICY_AGS,         "Icy Blue Armadyl Godsword"));
        DEFINITIONS.put(SCROLL_CORRUPTED_CLAWS, new RecolorDef(ItemId.DRAGON_CLAWS,    CORRUPTED_CLAWS,  "Corrupted Dragon Claws"));
        DEFINITIONS.put(SCROLL_ICY_WHIP,        new RecolorDef(ItemId.ABYSSAL_WHIP,    ICY_WHIP,         "Icy Blue Abyssal Whip"));
        DEFINITIONS.put(SCROLL_DARK_ACB,        new RecolorDef(ItemId.ARMADYL_CROSSBOW, DARK_ACB,        "Dark Armadyl Crossbow"));
    }

    // -----------------------------------------------------------------------
    // ItemPlugin interface
    // -----------------------------------------------------------------------

    @Override
    public void handle() {
        bind("Use", (player, scrollItem, slotId) -> {
            RecolorDef def = DEFINITIONS.get(scrollItem.getId());
            if (def == null) return; // shouldn't happen

            // Check equipped first, then inventory.
            if (hasItemEquipped(player, def.targetItemId)) {
                applyRecolorToEquipment(player, scrollItem, slotId, def);
            } else if (player.getInventory().containsItem(def.targetItemId, 1)) {
                applyRecolorToInventory(player, scrollItem, slotId, def);
            } else {
                player.sendMessage("You need a " + getItemName(def.targetItemId)
                        + " in your inventory or equipped to use this scroll.");
            }
        });
    }

    @Override
    public int[] getItems() {
        return new int[]{
            SCROLL_ICY_AGS,
            SCROLL_CORRUPTED_CLAWS,
            SCROLL_ICY_WHIP,
            SCROLL_DARK_ACB
        };
    }

    // -----------------------------------------------------------------------
    // Recolor application
    // -----------------------------------------------------------------------

    private static void applyRecolorToEquipment(Player player, Item scroll, int scrollSlot,
                                                  RecolorDef def) {
        // Find the equipment slot that holds the target item.
        for (int slot = 0; slot < 14; slot++) {
            Item equipped = player.getEquipment().getContainer().get(slot);
            if (equipped != null && equipped.getId() == def.targetItemId) {
                player.getEquipment().getContainer().set(slot, new Item(def.recoloredItemId));
                player.getEquipment().getContainer().refresh(player);
                player.getInventory().deleteItem(scrollSlot, 1);
                confirmRecolor(player, def);
                return;
            }
        }
    }

    private static void applyRecolorToInventory(Player player, Item scroll, int scrollSlot,
                                                   RecolorDef def) {
        int invSlot = player.getInventory().getContainer().getSlotOf(def.targetItemId);
        if (invSlot == -1) return;

        player.getInventory().getContainer().set(invSlot, new Item(def.recoloredItemId));
        player.getInventory().getContainer().refresh(player);
        player.getInventory().deleteItem(scrollSlot, 1);
        confirmRecolor(player, def);
    }

    private static void confirmRecolor(Player player, RecolorDef def) {
        player.sendMessage("<col=9400d3>Your item has been transformed into a "
                + def.displayName + "!</col>");
        player.getUpdateFlags().flag(UpdateFlag.APPEARANCE);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static boolean hasItemEquipped(Player player, int itemId) {
        for (int slot = 0; slot < 14; slot++) {
            Item item = player.getEquipment().getContainer().get(slot);
            if (item != null && item.getId() == itemId) return true;
        }
        return false;
    }

    private static String getItemName(int itemId) {
        mgi.types.config.items.ItemDefinitions def =
                mgi.types.config.items.ItemDefinitions.get(itemId);
        return def == null ? "that item" : def.getName();
    }

    // -----------------------------------------------------------------------
    // Inner types
    // -----------------------------------------------------------------------

    private record RecolorDef(int targetItemId, int recoloredItemId, String displayName) {}
}
