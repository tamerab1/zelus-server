package com.near_reality.game.content.tournament;

import com.zenyte.game.item.Item;
import com.zenyte.game.item.ItemId;
import com.zenyte.game.world.entity.masks.UpdateFlag;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.game.world.entity.player.SkillConstants;
import com.zenyte.game.world.entity.player.container.impl.equipment.EquipmentSlot;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines a fixed combat loadout for a tournament.
 *
 * Add as many presets as you need (Pure NH, Max Dharok, F2P, etc.).
 * Each preset stores:
 * <ul>
 *   <li>A name displayed to players</li>
 *   <li>A full equipment map (slot → item)</li>
 *   <li>Inventory items</li>
 *   <li>Locked skill levels (overriding the player's real levels)</li>
 * </ul>
 */
public enum TournamentPreset {

    // -----------------------------------------------------------------------
    // Pure NH  (60 Attack / 99 Str / 1 Def / 94 Mage / 99 Range / 52 Prayer)
    // -----------------------------------------------------------------------
    PURE_NH("Pure NH") {
        @Override
        public Map<Integer, Item> buildEquipment() {
            Map<Integer, Item> eq = new HashMap<>();
            eq.put(EquipmentSlot.HELMET.getSlot(),    new Item(ItemId.AHRIMS_HOOD));
            eq.put(EquipmentSlot.CAPE.getSlot(),      new Item(ItemId.IMBUED_SARADOMIN_CAPE));
            eq.put(EquipmentSlot.AMULET.getSlot(),    new Item(ItemId.OCCULT_NECKLACE));
            eq.put(EquipmentSlot.WEAPON.getSlot(),    new Item(ItemId.TOXIC_STAFF_OF_THE_DEAD));
            eq.put(EquipmentSlot.PLATE.getSlot(),     new Item(ItemId.AHRIMS_ROBETOP));
            eq.put(EquipmentSlot.SHIELD.getSlot(),    new Item(ItemId.MAGES_BOOK));
            eq.put(EquipmentSlot.LEGS.getSlot(),      new Item(ItemId.AHRIMS_ROBESKIRT));
            eq.put(EquipmentSlot.HANDS.getSlot(),     new Item(ItemId.TORMENTED_BRACELET));
            eq.put(EquipmentSlot.BOOTS.getSlot(),      new Item(ItemId.INFINITY_BOOTS));
            eq.put(EquipmentSlot.RING.getSlot(),      new Item(ItemId.SEERS_RING_I));
            eq.put(EquipmentSlot.AMMUNITION.getSlot(),new Item(ItemId.DEATH_RUNE, 500));
            return eq;
        }

        @Override
        public Item[] buildInventory() {
            return new Item[]{
                new Item(ItemId.SARADOMIN_BREW4,   8),
                new Item(ItemId.SUPER_RESTORE4,    6),
                new Item(ItemId.RANGING_POTION4,   2),
                new Item(ItemId.ANCIENT_STAFF),
                new Item(ItemId.RUNE_CROSSBOW),
                new Item(ItemId.RUBY_DRAGON_BOLTS_E, 100),
                new Item(ItemId.DIAMOND_DRAGON_BOLTS_E, 60),
            };
        }

        @Override
        public int[] buildSkillLevels() {
            // Index order matches SkillConstants (Attack=0, Defence=1, Strength=2, …)
            int[] levels = defaultLevels();
            levels[SkillConstants.ATTACK]   = 60;
            levels[SkillConstants.STRENGTH] = 99;
            levels[SkillConstants.DEFENCE]  = 1;
            levels[SkillConstants.MAGIC]    = 94;
            levels[SkillConstants.RANGED]   = 99;
            levels[SkillConstants.PRAYER]   = 52;
            levels[SkillConstants.HITPOINTS]= 99;
            return levels;
        }
    },

    // -----------------------------------------------------------------------
    // Max Dharok  (99 all combat stats, Dharok's set)
    // -----------------------------------------------------------------------
    MAX_DHAROK("Max Dharok") {
        @Override
        public Map<Integer, Item> buildEquipment() {
            Map<Integer, Item> eq = new HashMap<>();
            eq.put(EquipmentSlot.HELMET.getSlot(),    new Item(ItemId.DHAROKS_HELM));
            eq.put(EquipmentSlot.CAPE.getSlot(),      new Item(ItemId.INFERNAL_CAPE));
            eq.put(EquipmentSlot.AMULET.getSlot(),    new Item(ItemId.AMULET_OF_TORTURE));
            eq.put(EquipmentSlot.WEAPON.getSlot(),    new Item(ItemId.DHAROKS_GREATAXE));
            eq.put(EquipmentSlot.PLATE.getSlot(),     new Item(ItemId.DHAROKS_PLATEBODY));
            eq.put(EquipmentSlot.LEGS.getSlot(),      new Item(ItemId.DHAROKS_PLATELEGS));
            eq.put(EquipmentSlot.HANDS.getSlot(),     new Item(ItemId.FEROCIOUS_GLOVES));
            eq.put(EquipmentSlot.BOOTS.getSlot(),      new Item(ItemId.PRIMORDIAL_BOOTS));
            eq.put(EquipmentSlot.RING.getSlot(),      new Item(ItemId.BERSERKER_RING_I));
            eq.put(EquipmentSlot.AMMUNITION.getSlot(),new Item(ItemId.RUNE_ARROW, 1));
            return eq;
        }

        @Override
        public Item[] buildInventory() {
            return new Item[]{
                new Item(ItemId.SARADOMIN_BREW4,    8),
                new Item(ItemId.SUPER_RESTORE4,     8),
                new Item(ItemId.SHARK,                5),
                new Item(ItemId.SUPER_COMBAT_POTION4, 2),
            };
        }

        @Override
        public int[] buildSkillLevels() {
            int[] levels = defaultLevels();
            levels[SkillConstants.ATTACK]   = 99;
            levels[SkillConstants.STRENGTH] = 99;
            levels[SkillConstants.DEFENCE]  = 99;
            levels[SkillConstants.HITPOINTS]= 99;
            levels[SkillConstants.PRAYER]   = 99;
            return levels;
        }
    },

    // -----------------------------------------------------------------------
    // F2P  (40 Attack / 40 Strength / 40 Defence / no prayer gear)
    // -----------------------------------------------------------------------
    F2P("F2P") {
        @Override
        public Map<Integer, Item> buildEquipment() {
            Map<Integer, Item> eq = new HashMap<>();
            eq.put(EquipmentSlot.HELMET.getSlot(),    new Item(ItemId.RUNE_FULL_HELM));
            eq.put(EquipmentSlot.CAPE.getSlot(),      new Item(ItemId.TEAM_CAPE_I));
            eq.put(EquipmentSlot.AMULET.getSlot(),    new Item(ItemId.AMULET_OF_POWER));
            eq.put(EquipmentSlot.WEAPON.getSlot(),    new Item(ItemId.RUNE_SCIMITAR));
            eq.put(EquipmentSlot.PLATE.getSlot(),     new Item(ItemId.RUNE_PLATEBODY));
            eq.put(EquipmentSlot.SHIELD.getSlot(),    new Item(ItemId.RUNE_KITESHIELD));
            eq.put(EquipmentSlot.LEGS.getSlot(),      new Item(ItemId.RUNE_PLATELEGS));
            eq.put(EquipmentSlot.HANDS.getSlot(),     new Item(ItemId.RUNE_GLOVES));
            eq.put(EquipmentSlot.BOOTS.getSlot(),      new Item(ItemId.RUNE_BOOTS));
            eq.put(EquipmentSlot.RING.getSlot(),      new Item(ItemId.RING_OF_RECOIL));
            return eq;
        }

        @Override
        public Item[] buildInventory() {
            return new Item[]{
                new Item(ItemId.LOBSTER, 20),
                new Item(ItemId.SWORDFISH, 7),
            };
        }

        @Override
        public int[] buildSkillLevels() {
            int[] levels = defaultLevels();
            levels[SkillConstants.ATTACK]   = 40;
            levels[SkillConstants.STRENGTH] = 40;
            levels[SkillConstants.DEFENCE]  = 40;
            levels[SkillConstants.HITPOINTS]= 40;
            levels[SkillConstants.PRAYER]   = 31;
            return levels;
        }
    };

    // -----------------------------------------------------------------------
    // Abstract contract
    // -----------------------------------------------------------------------

    private final String displayName;

    TournamentPreset(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }

    /** Returns the equipment to force-equip on the player. */
    public abstract Map<Integer, Item> buildEquipment();

    /** Returns the inventory to give the player. */
    public abstract Item[] buildInventory();

    /**
     * Returns a 25-element skill-level array (indices = SkillConstants).
     * Skills not explicitly overridden are left at 1.
     */
    public abstract int[] buildSkillLevels();

    // -----------------------------------------------------------------------
    // Application
    // -----------------------------------------------------------------------

    /**
     * Forces this preset onto {@code player}: clears their equipment/inventory,
     * applies the defined gear, sets skill levels, and restores HP to max.
     */
    public void apply(Player player) {
        // --- Clear current loadout ---
        player.getEquipment().getContainer().getItems().clear();
        player.getInventory().getContainer().getItems().clear();

        // --- Force-equip items ---
        buildEquipment().forEach((slot, item) ->
            player.getEquipment().getContainer().getItems().put(slot, item));
        player.getEquipment().getContainer().refresh(player);

        // --- Fill inventory ---
        Item[] inv = buildInventory();
        for (int i = 0; i < inv.length && i < 28; i++) {
            player.getInventory().getContainer().set(i, inv[i]);
        }
        player.getInventory().getContainer().refresh(player);

        // --- Lock skill levels ---
        int[] levels = buildSkillLevels();
        for (int skill = 0; skill < levels.length; skill++) {
            if (levels[skill] > 0) {
                player.getSkills().setLevel(skill, levels[skill]);
            }
        }

        // --- Restore HP to the preset's max ---
        int hp = levels[SkillConstants.HITPOINTS] > 0 ? levels[SkillConstants.HITPOINTS] : 10;
        player.setHitpoints(hp);

        // --- Drain prayer to 0 (fair start) ---
        player.getPrayerManager().setPrayerPoints(0);

        // Refresh appearance
        player.getUpdateFlags().flag(UpdateFlag.APPEARANCE);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static int[] defaultLevels() {
        int[] lvls = new int[25];
        // HP defaults to 10 for any skill not set
        lvls[SkillConstants.HITPOINTS] = 10;
        return lvls;
    }
}
