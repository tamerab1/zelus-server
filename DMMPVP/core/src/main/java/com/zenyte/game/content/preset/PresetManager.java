    package com.zenyte.game.content.preset;

    import com.google.common.eventbus.Subscribe;
    import com.zenyte.game.content.skills.magic.Spellbook;
    import com.zenyte.game.item.Item;
    import com.zenyte.game.item.ItemId;
    import com.zenyte.game.model.ui.testinterfaces.PresetManagerInterface;
    import com.zenyte.game.task.WorldTasksManager;
    import com.zenyte.game.world.entity.masks.UpdateFlag;
    import com.zenyte.game.world.entity.player.Player;
    import com.zenyte.game.world.entity.player.SkillConstants;
    import com.zenyte.game.world.entity.player.container.impl.equipment.EquipmentSlot;
    import com.zenyte.game.world.entity.player.privilege.GameMode;
    import com.zenyte.plugins.events.InitializationEvent;
    import it.unimi.dsi.fastutil.objects.ObjectArrayList;
    import org.jetbrains.annotations.NotNull;
    import org.jetbrains.annotations.Nullable;

    import java.lang.ref.WeakReference;
    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;
    import java.util.Optional;
    //import static com.zenyte.game.content.grandexchange.GrandExchange.initBlockedItemsFromPresets;

    /**
     * @author Tommeh | 20/04/2020 | 00:32
     * @see <a href="https://www.rune-server.ee/members/tommeh/">Rune-Server profile</a>
     */
    public class PresetManager {
        private final transient WeakReference<Player> player;
        private final List<Preset> presets;
        private int defaultPreset;
        private int unlockedSlots;
        public String getName() {
            return getName();
        }


        public PresetManager(@NotNull final Player player) {
            this.player = new WeakReference<>(player);
            this.presets = new ObjectArrayList<>();
        }

        @Subscribe
        public static final void onInitialization(final InitializationEvent event) {

            final Player player = event.getPlayer();
            final Player savedPlayer = event.getSavedPlayer();
            final PresetManager manager = savedPlayer.getPresetManager();

            if (manager == null) {
                player.sendMessage("Preset is null");
                return;
            }
            player.sendMessage("Preset is not null");
            final PresetManager thisManager = player.getPresetManager();
            thisManager.defaultPreset = manager.defaultPreset;
            thisManager.unlockedSlots = manager.unlockedSlots;
            if (manager.presets != null) {
                for (final Preset preset : manager.presets) {
                    thisManager.presets.add(new Preset(preset));
                }
            }
            player.getPresetManager().getPremadePresets();
            //initBlockedItemsFromPresets(player.getPresetManager());

        }

        public void ensureDefaultPresets() {
//            final Player player = this.player.get();
//
//            Optional<Preset> existing = this.presets.stream().filter(p -> "[pre-made]126 Max Melee".equalsIgnoreCase(p.getName())).findFirst();
//            if (!existing.isPresent() || existing.get().getSkillLevels() == null || existing.get().getSkillLevels().isEmpty()) {
//                System.err.println("[FIX] Replacing missing or broken Max Melee preset");
//                existing.ifPresent(this.presets::remove);
//                Map<Integer, Item> meleeEquipment = new HashMap<>();
//                meleeEquipment.put(EquipmentSlot.HELMET.getSlot(), new Item(ItemId.HELM_OF_NEITIZNOT));
//                meleeEquipment.put(EquipmentSlot.CAPE.getSlot(), new Item(ItemId.CAPE_OF_LEGENDS));
//                meleeEquipment.put(EquipmentSlot.AMULET.getSlot(), new Item(ItemId.AMULET_OF_GLORY));
//                meleeEquipment.put(EquipmentSlot.WEAPON.getSlot(), new Item(ItemId.DRAGON_SCIMITAR));
//                meleeEquipment.put(EquipmentSlot.PLATE.getSlot(), new Item(ItemId.RUNE_PLATEBODY));
//                meleeEquipment.put(EquipmentSlot.SHIELD.getSlot(), new Item(ItemId.RUNE_DEFENDER));
//                meleeEquipment.put(EquipmentSlot.LEGS.getSlot(), new Item(ItemId.RUNE_PLATELEGS));
//                meleeEquipment.put(EquipmentSlot.HANDS.getSlot(), new Item(ItemId.BARROWS_GLOVES));
//                meleeEquipment.put(EquipmentSlot.BOOTS.getSlot(), new Item(ItemId.RUNE_BOOTS));
//                meleeEquipment.put(EquipmentSlot.RING.getSlot(), new Item(ItemId.RING_OF_RECOIL));
//                Map<Integer, Item> meleeInventory = new HashMap<>();
//                meleeInventory.put(0, new Item(ItemId.SARADOMIN_BREW4));
//                meleeInventory.put(1, new Item(ItemId.SARADOMIN_BREW4));
//                meleeInventory.put(2, new Item(ItemId.SHARK));
//                meleeInventory.put(3, new Item(ItemId.SHARK));
//                meleeInventory.put(4, new Item(ItemId.SANFEW_SERUM4));
//                meleeInventory.put(5, new Item(ItemId.SANFEW_SERUM4));
//                meleeInventory.put(6, new Item(ItemId.SHARK));
//                meleeInventory.put(7, new Item(ItemId.SHARK));
//                meleeInventory.put(8, new Item(ItemId.SUPER_STRENGTH4));
//                meleeInventory.put(9, new Item(ItemId.SUPER_ATTACK4));
//                meleeInventory.put(10, new Item(ItemId.SHARK));
//                meleeInventory.put(11, new Item(ItemId.SHARK));
//                meleeInventory.put(12, new Item(ItemId.SHARK));
//                meleeInventory.put(13, new Item(ItemId.SHARK));
//                meleeInventory.put(14, new Item(ItemId.SHARK));
//                meleeInventory.put(15, new Item(ItemId.SHARK));
//                meleeInventory.put(16, new Item(ItemId.SHARK));
//                meleeInventory.put(17, new Item(ItemId.SHARK));
//                meleeInventory.put(18, new Item(ItemId.SHARK));
//                meleeInventory.put(19, new Item(ItemId.SHARK));
//                meleeInventory.put(20, new Item(ItemId.DRAGON_DAGGER));
//                meleeInventory.put(21, new Item(ItemId.SHARK));
//                meleeInventory.put(22, new Item(ItemId.SHARK));
//                meleeInventory.put(23, new Item(ItemId.SHARK));
//                meleeInventory.put(24, new Item(ItemId.SHARK));
//                meleeInventory.put(25, new Item(ItemId.DEATH_RUNE, 250));
//                meleeInventory.put(26, new Item(ItemId.ASTRAL_RUNE, 250));
//                meleeInventory.put(27, new Item(ItemId.EARTH_RUNE, 500));
//
//                Map<Integer, Integer> meleeStats = new HashMap<>();
//                meleeStats.put(SkillConstants.ATTACK, 99);
//                meleeStats.put(SkillConstants.STRENGTH, 99);
//                meleeStats.put(SkillConstants.DEFENCE, 99);
//                meleeStats.put(SkillConstants.HITPOINTS, 99);
//                meleeStats.put(SkillConstants.PRAYER, 99);
//                meleeStats.put(SkillConstants.RANGED, 99);
//                meleeStats.put(SkillConstants.MAGIC, 99);
//
//                Preset meleePreset = new Preset("[pre-made]126 Max Melee", meleeEquipment, meleeInventory, Spellbook.LUNAR, meleeStats);
//
//                meleePreset.setLocked(true);
//                if (meleePreset.getSkillLevels() == null && meleePreset.isLocked()) {
//                    System.err.println("[BUG] Locked preset created without skill levels: " + meleePreset.getName());
//                }
//                presets.add(meleePreset);
//            }
//
//            Optional<Preset> tribrid = this.presets.stream()
//                    .filter(p -> "[pre-made]126 Tribrid".equalsIgnoreCase(p.getName()))
//                    .findFirst();
//
//            if (!tribrid.isPresent() || tribrid.get().getSkillLevels() == null || tribrid.get().getSkillLevels().isEmpty()) {
//                System.err.println("[FIX] Replacing missing or broken Tribrid preset");
//                tribrid.ifPresent(this.presets::remove);
//
//                Map<Integer, Item> tribridEquipment = new HashMap<>();
//                tribridEquipment.put(EquipmentSlot.HELMET.getSlot(), new Item(ItemId.HELM_OF_NEITIZNOT));
//                tribridEquipment.put(EquipmentSlot.CAPE.getSlot(), new Item(ItemId.SARADOMIN_CAPE));
//                tribridEquipment.put(EquipmentSlot.AMULET.getSlot(), new Item(ItemId.AMULET_OF_GLORY));
//                tribridEquipment.put(EquipmentSlot.WEAPON.getSlot(), new Item(ItemId.ANCIENT_STAFF));
//                tribridEquipment.put(EquipmentSlot.PLATE.getSlot(), new Item(ItemId.MYSTIC_ROBE_TOP));
//                tribridEquipment.put(EquipmentSlot.SHIELD.getSlot(), new Item(ItemId.SPIRIT_SHIELD));
//                tribridEquipment.put(EquipmentSlot.LEGS.getSlot(), new Item(ItemId.MYSTIC_ROBE_BOTTOM));
//                tribridEquipment.put(EquipmentSlot.HANDS.getSlot(), new Item(ItemId.BARROWS_GLOVES));
//                tribridEquipment.put(EquipmentSlot.BOOTS.getSlot(), new Item(ItemId.CLIMBING_BOOTS));
//                tribridEquipment.put(EquipmentSlot.RING.getSlot(), new Item(ItemId.RING_OF_RECOIL));
//                tribridEquipment.put(EquipmentSlot.AMMUNITION.getSlot(), new Item(ItemId.DRAGONSTONE_BOLTS_E, 100));
//
//                Map<Integer, Item> tribridInventory = new HashMap<>();
//                tribridInventory.put(0, new Item(ItemId.SANFEW_SERUM4));
//                tribridInventory.put(1, new Item(ItemId.SANFEW_SERUM4));
//                tribridInventory.put(2, new Item(ItemId.SANFEW_SERUM4));
//                tribridInventory.put(3, new Item(ItemId.SANFEW_SERUM4));
//                tribridInventory.put(4, new Item(ItemId.SARADOMIN_BREW4));
//                tribridInventory.put(5, new Item(ItemId.SARADOMIN_BREW4));
//                tribridInventory.put(6, new Item(ItemId.SARADOMIN_BREW4));
//                tribridInventory.put(7, new Item(ItemId.SARADOMIN_BREW4));
//                tribridInventory.put(8, new Item(ItemId.RUNE_CROSSBOW));
//                tribridInventory.put(9, new Item(ItemId.DRAGON_SCIMITAR));
//                tribridInventory.put(10, new Item(ItemId.SARADOMIN_BREW4));
//                tribridInventory.put(11, new Item(ItemId.SARADOMIN_BREW4));
//                tribridInventory.put(12, new Item(ItemId.BLACK_DHIDE_BODY));
//                tribridInventory.put(13, new Item(ItemId.RUNE_DEFENDER));
//                tribridInventory.put(14, new Item(ItemId.SARADOMIN_BREW4));
//                tribridInventory.put(15, new Item(ItemId.SARADOMIN_BREW4));
//                tribridInventory.put(16, new Item(ItemId.RUNE_PLATELEGS));
//                tribridInventory.put(17, new Item(ItemId.DRAGON_DAGGERP));
//                tribridInventory.put(18, new Item(ItemId.SHARK));
//                tribridInventory.put(19, new Item(ItemId.SARADOMIN_BREW4));
//                tribridInventory.put(20, new Item(ItemId.MAGIC_POTION4));
//                tribridInventory.put(21, new Item(ItemId.SUPER_STRENGTH4));
//                tribridInventory.put(22, new Item(ItemId.SUPER_ATTACK4));
//                tribridInventory.put(23, new Item(ItemId.SHARK));
//                tribridInventory.put(24, new Item(ItemId.SHARK));
//                tribridInventory.put(25, new Item(ItemId.BLOOD_RUNE, 10000));
//                tribridInventory.put(26, new Item(ItemId.WATER_RUNE, 10000));
//                tribridInventory.put(27, new Item(ItemId.DEATH_RUNE, 10000));
//
//                Map<Integer, Integer> tribridStats = new HashMap<>();
//                tribridStats.put(SkillConstants.ATTACK, 99);
//                tribridStats.put(SkillConstants.STRENGTH, 99);
//                tribridStats.put(SkillConstants.DEFENCE, 99);
//                tribridStats.put(SkillConstants.HITPOINTS, 99);
//                tribridStats.put(SkillConstants.RANGED, 99);
//                tribridStats.put(SkillConstants.PRAYER, 99);
//                tribridStats.put(SkillConstants.MAGIC, 99);
//
//                Preset tribridPreset = new Preset("[pre-made]126 Tribrid", tribridEquipment, tribridInventory, Spellbook.ANCIENT, tribridStats);
//                tribridPreset.setLocked(true);
//                if (tribridPreset.getSkillLevels() == null && tribridPreset.isLocked()) {
//                    System.err.println("[BUG] Locked preset created without skill levels: " + tribridPreset.getName());
//                }
//                presets.add(tribridPreset);
//            }
//
//
//            Optional<Preset> hybrid = this.presets.stream()
//                    .filter(p -> "[pre-made]126 Hybrid".equalsIgnoreCase(p.getName()))
//                    .findFirst();
//            if (!hybrid.isPresent() || hybrid.get().getSkillLevels() == null || hybrid.get().getSkillLevels().isEmpty()) {
//                System.err.println("[FIX] Replacing missing or broken Hybrid preset");
//                hybrid.ifPresent(this.presets::remove);
//
//
//                Map<Integer, Item> hybridEquipment = new HashMap<>();
//                hybridEquipment.put(EquipmentSlot.HELMET.getSlot(), new Item(ItemId.HELM_OF_NEITIZNOT));
//                hybridEquipment.put(EquipmentSlot.CAPE.getSlot(), new Item(ItemId.SARADOMIN_CAPE));
//                hybridEquipment.put(EquipmentSlot.AMULET.getSlot(), new Item(ItemId.AMULET_OF_GLORY));
//                hybridEquipment.put(EquipmentSlot.WEAPON.getSlot(), new Item(ItemId.ANCIENT_STAFF));
//                hybridEquipment.put(EquipmentSlot.PLATE.getSlot(), new Item(ItemId.MYSTIC_ROBE_TOP));
//                hybridEquipment.put(EquipmentSlot.SHIELD.getSlot(), new Item(ItemId.SPIRIT_SHIELD));
//                hybridEquipment.put(EquipmentSlot.LEGS.getSlot(), new Item(ItemId.MYSTIC_ROBE_BOTTOM));
//                hybridEquipment.put(EquipmentSlot.HANDS.getSlot(), new Item(ItemId.BARROWS_GLOVES));
//                hybridEquipment.put(EquipmentSlot.BOOTS.getSlot(), new Item(ItemId.CLIMBING_BOOTS));
//                hybridEquipment.put(EquipmentSlot.RING.getSlot(), new Item(ItemId.RING_OF_RECOIL));
//                // DRAGONSTONE_BOLTS_E intentionally omitted
//
//                Map<Integer, Item> hybridInventory = new HashMap<>();
//                hybridInventory.put(0, new Item(ItemId.SANFEW_SERUM4));
//                hybridInventory.put(1, new Item(ItemId.SANFEW_SERUM4));
//                hybridInventory.put(2, new Item(ItemId.SANFEW_SERUM4));
//                hybridInventory.put(3, new Item(ItemId.SANFEW_SERUM4));
//                hybridInventory.put(4, new Item(ItemId.SARADOMIN_BREW4));
//                hybridInventory.put(5, new Item(ItemId.SARADOMIN_BREW4));
//                hybridInventory.put(6, new Item(ItemId.SARADOMIN_BREW4));
//                hybridInventory.put(7, new Item(ItemId.SARADOMIN_BREW4));
//                hybridInventory.put(8, new Item(ItemId.SHARK)); // was RUNE_CROSSBOW
//                hybridInventory.put(9, new Item(ItemId.DRAGON_SCIMITAR));
//                hybridInventory.put(10, new Item(ItemId.SARADOMIN_BREW4));
//                hybridInventory.put(11, new Item(ItemId.SARADOMIN_BREW4));
//                hybridInventory.put(12, new Item(ItemId.BLACK_DHIDE_BODY));
//                hybridInventory.put(13, new Item(ItemId.RUNE_DEFENDER));
//                hybridInventory.put(14, new Item(ItemId.SARADOMIN_BREW4));
//                hybridInventory.put(15, new Item(ItemId.SARADOMIN_BREW4));
//                hybridInventory.put(16, new Item(ItemId.RUNE_PLATELEGS));
//                hybridInventory.put(17, new Item(ItemId.DRAGON_DAGGERP));
//                hybridInventory.put(18, new Item(ItemId.SHARK));
//                hybridInventory.put(19, new Item(ItemId.SARADOMIN_BREW4));
//                hybridInventory.put(20, new Item(ItemId.MAGIC_POTION4));
//                hybridInventory.put(21, new Item(ItemId.SUPER_STRENGTH4));
//                hybridInventory.put(22, new Item(ItemId.SUPER_ATTACK4));
//                hybridInventory.put(23, new Item(ItemId.SHARK));
//                hybridInventory.put(24, new Item(ItemId.SHARK));
//                hybridInventory.put(25, new Item(ItemId.BLOOD_RUNE, 10000));
//                hybridInventory.put(26, new Item(ItemId.WATER_RUNE, 10000));
//                hybridInventory.put(27, new Item(ItemId.DEATH_RUNE, 10000));
//
//                Map<Integer, Integer> hybridStats = new HashMap<>();
//                hybridStats.put(SkillConstants.ATTACK, 99);
//                hybridStats.put(SkillConstants.STRENGTH, 99);
//                hybridStats.put(SkillConstants.DEFENCE, 99);
//                hybridStats.put(SkillConstants.HITPOINTS, 99);
//                hybridStats.put(SkillConstants.RANGED, 99);
//                hybridStats.put(SkillConstants.PRAYER, 99);
//                hybridStats.put(SkillConstants.MAGIC, 99);
//
//                Preset hybridPreset = new Preset("[pre-made]126 Hybrid", hybridEquipment, hybridInventory, Spellbook.ANCIENT, hybridStats);  hybridPreset.setLocked(true);
//                if (hybridPreset.getSkillLevels() == null && hybridPreset.isLocked()) {
//                    System.err.println("[BUG] Locked preset created without skill levels: " + hybridPreset.getName());
//                }
//
//                presets.add(hybridPreset);
//            }
//            Optional<Preset> pure = this.presets.stream()
//                    .filter(p -> "[pre-made]Pure Melee".equalsIgnoreCase(p.getName()))
//                    .findFirst();
//            if (!pure.isPresent() || pure.get().getSkillLevels() == null || pure.get().getSkillLevels().isEmpty()) {
//                System.err.println("[FIX] Replacing missing or broken Pure Melee preset");
//                pure.ifPresent(this.presets::remove);
//                Map<Integer, Item> pureMeleeEquipment = new HashMap<>();
//                pureMeleeEquipment.put(EquipmentSlot.HELMET.getSlot(), new Item(ItemId.BEARHEAD));
//                pureMeleeEquipment.put(EquipmentSlot.CAPE.getSlot(), new Item(ItemId.CAPE_OF_LEGENDS));
//                pureMeleeEquipment.put(EquipmentSlot.AMULET.getSlot(), new Item(ItemId.AMULET_OF_GLORY));
//                pureMeleeEquipment.put(EquipmentSlot.WEAPON.getSlot(), new Item(ItemId.DRAGON_SCIMITAR));
//                pureMeleeEquipment.put(EquipmentSlot.PLATE.getSlot(), new Item(ItemId.MONKS_ROBE_TOP));
//                pureMeleeEquipment.put(EquipmentSlot.SHIELD.getSlot(), new Item(ItemId.BOOK_OF_LAW));
//                pureMeleeEquipment.put(EquipmentSlot.LEGS.getSlot(), new Item(ItemId.BLACK_DHIDE_CHAPS));
//                pureMeleeEquipment.put(EquipmentSlot.HANDS.getSlot(), new Item(ItemId.MITHRIL_GLOVES));
//                pureMeleeEquipment.put(EquipmentSlot.BOOTS.getSlot(), new Item(ItemId.CLIMBING_BOOTS));
//                pureMeleeEquipment.put(EquipmentSlot.RING.getSlot(), new Item(ItemId.RING_OF_RECOIL));
//
//                Map<Integer, Item> pureMeleeInventory = new HashMap<>();
//                pureMeleeInventory.put(0, new Item(ItemId.SARADOMIN_BREW4));
//                pureMeleeInventory.put(1, new Item(ItemId.SARADOMIN_BREW4));
//                pureMeleeInventory.put(2, new Item(ItemId.SHARK));
//                pureMeleeInventory.put(3, new Item(ItemId.SHARK));
//                pureMeleeInventory.put(4, new Item(ItemId.SANFEW_SERUM4));
//                pureMeleeInventory.put(5, new Item(ItemId.SANFEW_SERUM4));
//                pureMeleeInventory.put(6, new Item(ItemId.SHARK));
//                pureMeleeInventory.put(7, new Item(ItemId.SHARK));
//                pureMeleeInventory.put(8, new Item(ItemId.SUPER_STRENGTH4));
//                pureMeleeInventory.put(9, new Item(ItemId.SUPER_ATTACK4));
//                pureMeleeInventory.put(10, new Item(ItemId.SHARK));
//                pureMeleeInventory.put(11, new Item(ItemId.SHARK));
//                pureMeleeInventory.put(12, new Item(ItemId.SHARK));
//                pureMeleeInventory.put(13, new Item(ItemId.SHARK));
//                pureMeleeInventory.put(14, new Item(ItemId.SHARK));
//                pureMeleeInventory.put(15, new Item(ItemId.SHARK));
//                pureMeleeInventory.put(16, new Item(ItemId.SHARK));
//                pureMeleeInventory.put(17, new Item(ItemId.SHARK));
//                pureMeleeInventory.put(18, new Item(ItemId.SHARK));
//                pureMeleeInventory.put(19, new Item(ItemId.SHARK));
//                pureMeleeInventory.put(20, new Item(ItemId.DRAGON_DAGGER));
//                pureMeleeInventory.put(21, new Item(ItemId.SHARK));
//                pureMeleeInventory.put(22, new Item(ItemId.SHARK));
//                pureMeleeInventory.put(23, new Item(ItemId.SHARK));
//                pureMeleeInventory.put(24, new Item(ItemId.DRAGON_2H_SWORD));
//                pureMeleeInventory.put(25, new Item(ItemId.SHARK));
//                pureMeleeInventory.put(26, new Item(ItemId.SHARK));
//                pureMeleeInventory.put(27, new Item(ItemId.SHARK));
//
//                Map<Integer, Integer> pureMeleeStats = new HashMap<>();
//                pureMeleeStats.put(SkillConstants.ATTACK, 75);
//                pureMeleeStats.put(SkillConstants.STRENGTH, 99);
//                pureMeleeStats.put(SkillConstants.DEFENCE, 1);
//                pureMeleeStats.put(SkillConstants.HITPOINTS, 99);
//                pureMeleeStats.put(SkillConstants.RANGED, 99);
//                pureMeleeStats.put(SkillConstants.PRAYER, 52);
//                pureMeleeStats.put(SkillConstants.MAGIC, 99);
//
//                Preset pureMeleePreset = new Preset("[pre-made]Pure Melee", pureMeleeEquipment, pureMeleeInventory, Spellbook.NORMAL, pureMeleeStats);
//                pureMeleePreset.setLocked(true);
//
//                if (pureMeleePreset.getSkillLevels() == null && pureMeleePreset.isLocked()) {
//                    System.err.println("[BUG] Locked preset created without skill levels: " + pureMeleePreset.getName());
//                }
//
//                presets.add(pureMeleePreset);
//            }
//            Optional<Preset> pureNh = this.presets.stream()
//                    .filter(p -> "[pre-made]Pure NH".equalsIgnoreCase(p.getName()))
//                    .findFirst();
//            if (!pureNh.isPresent() || pureNh.get().getSkillLevels() == null || pureNh.get().getSkillLevels().isEmpty()) {
//                System.err.println("[FIX] Replacing missing or broken Pure NH preset");
//                pureNh.ifPresent(this.presets::remove);
//                Map<Integer, Item> pureNhEquipment = new HashMap<>();
//                pureNhEquipment.put(EquipmentSlot.HELMET.getSlot(), new Item(ItemId.GHOSTLY_HOOD));
//                pureNhEquipment.put(EquipmentSlot.CAPE.getSlot(), new Item(ItemId.SARADOMIN_CAPE));
//                pureNhEquipment.put(EquipmentSlot.AMULET.getSlot(), new Item(ItemId.AMULET_OF_GLORY));
//                pureNhEquipment.put(EquipmentSlot.WEAPON.getSlot(), new Item(ItemId.ANCIENT_STAFF));
//                pureNhEquipment.put(EquipmentSlot.PLATE.getSlot(), new Item(ItemId.GHOSTLY_ROBE));
//                pureNhEquipment.put(EquipmentSlot.SHIELD.getSlot(), new Item(ItemId.BOOK_OF_LAW));
//                pureNhEquipment.put(EquipmentSlot.LEGS.getSlot(), new Item(ItemId.GHOSTLY_ROBE_6108));
//                pureNhEquipment.put(EquipmentSlot.HANDS.getSlot(), new Item(ItemId.MITHRIL_GLOVES));
//                pureNhEquipment.put(EquipmentSlot.BOOTS.getSlot(), new Item(ItemId.CLIMBING_BOOTS));
//                pureNhEquipment.put(EquipmentSlot.RING.getSlot(), new Item(ItemId.RING_OF_RECOIL));
//                pureNhEquipment.put(EquipmentSlot.AMMUNITION.getSlot(), new Item(ItemId.DRAGONSTONE_BOLTS, 2000));
//
//                Map<Integer, Item> pureNhInventory = new HashMap<>();
//                pureNhInventory.put(0, new Item(ItemId.SANFEW_SERUM4));
//                pureNhInventory.put(1, new Item(ItemId.SANFEW_SERUM4));
//                pureNhInventory.put(2, new Item(ItemId.SHARK));
//                pureNhInventory.put(3, new Item(ItemId.SHARK));
//                pureNhInventory.put(4, new Item(ItemId.SANFEW_SERUM4));
//                pureNhInventory.put(5, new Item(ItemId.SANFEW_SERUM4));
//                pureNhInventory.put(6, new Item(ItemId.SHARK));
//                pureNhInventory.put(7, new Item(ItemId.SHARK));
//                pureNhInventory.put(8, new Item(ItemId.SUPER_STRENGTH4));
//                pureNhInventory.put(9, new Item(ItemId.SUPER_ATTACK4));
//                pureNhInventory.put(10, new Item(ItemId.RANGING_POTION4));
//                pureNhInventory.put(11, new Item(ItemId.SARADOMIN_BREW4));
//                pureNhInventory.put(12, new Item(ItemId.SARADOMIN_BREW4));
//                pureNhInventory.put(13, new Item(ItemId.SARADOMIN_BREW4));
//                pureNhInventory.put(14, new Item(ItemId.SARADOMIN_BREW4));
//                pureNhInventory.put(15, new Item(ItemId.SARADOMIN_BREW4));
//                pureNhInventory.put(16, new Item(ItemId.AVAS_ACCUMULATOR));
//                pureNhInventory.put(17, new Item(ItemId.BLACK_DHIDE_CHAPS));
//                pureNhInventory.put(18, new Item(ItemId.SARADOMIN_BREW4));
//                pureNhInventory.put(19, new Item(ItemId.SARADOMIN_BREW4));
//                pureNhInventory.put(20, new Item(ItemId.DRAGON_DAGGER));
//                pureNhInventory.put(21, new Item(ItemId.RUNE_CROSSBOW));
//                pureNhInventory.put(22, new Item(ItemId.SARADOMIN_BREW4));
//                pureNhInventory.put(23, new Item(ItemId.SHARK));
//                pureNhInventory.put(24, new Item(ItemId.DRAGON_SCIMITAR));
//                pureNhInventory.put(25, new Item(ItemId.BLOOD_RUNE, 2000));
//                pureNhInventory.put(26, new Item(ItemId.WATER_RUNE, 2000));
//                pureNhInventory.put(27, new Item(ItemId.DEATH_RUNE, 2000));
//
//                Map<Integer, Integer> pureNhStats = new HashMap<>();
//                pureNhStats.put(SkillConstants.ATTACK, 75);
//                pureNhStats.put(SkillConstants.STRENGTH, 99);
//                pureNhStats.put(SkillConstants.DEFENCE, 1);
//                pureNhStats.put(SkillConstants.HITPOINTS, 99);
//                pureNhStats.put(SkillConstants.RANGED, 99);
//                pureNhStats.put(SkillConstants.PRAYER, 52);
//                pureNhStats.put(SkillConstants.MAGIC, 99);
//
//                Preset pureNhPreset = new Preset("[pre-made]Pure NH", pureNhEquipment, pureNhInventory, Spellbook.ANCIENT, pureNhStats);
//                pureNhPreset.setLocked(true);
//
//                if (pureNhPreset.getSkillLevels() == null && pureNhPreset.isLocked()) {
//                    System.err.println("[BUG] Locked preset created without skill levels: " + pureNhPreset.getName());
//                }
//
//                presets.add(pureNhPreset);
//            }
//            Optional<Preset> zerkMelee = this.presets.stream()
//                    .filter(p -> "[pre-made]Zerk Melee".equalsIgnoreCase(p.getName()))
//                    .findFirst();
//            if (!zerkMelee.isPresent() || zerkMelee.get().getSkillLevels() == null || zerkMelee.get().getSkillLevels().isEmpty()) {
//                System.err.println("[FIX] Replacing missing or broken Zerk Melee preset");
//                zerkMelee.ifPresent(this.presets::remove);
//
//                Map<Integer, Item> zerkMeleeEquipment = new HashMap<>();
//                zerkMeleeEquipment.put(EquipmentSlot.HELMET.getSlot(), new Item(ItemId.BERSERKER_HELM));
//                zerkMeleeEquipment.put(EquipmentSlot.CAPE.getSlot(), new Item(ItemId.FIRE_CAPE));
//                zerkMeleeEquipment.put(EquipmentSlot.AMULET.getSlot(), new Item(ItemId.AMULET_OF_GLORY));
//                zerkMeleeEquipment.put(EquipmentSlot.WEAPON.getSlot(), new Item(ItemId.DRAGON_SCIMITAR));
//                zerkMeleeEquipment.put(EquipmentSlot.PLATE.getSlot(), new Item(ItemId.FIGHTER_TORSO));
//                zerkMeleeEquipment.put(EquipmentSlot.SHIELD.getSlot(), new Item(ItemId.RUNE_DEFENDER));
//                zerkMeleeEquipment.put(EquipmentSlot.LEGS.getSlot(), new Item(ItemId.RUNE_PLATELEGS));
//                zerkMeleeEquipment.put(EquipmentSlot.HANDS.getSlot(), new Item(ItemId.BARROWS_GLOVES));
//                zerkMeleeEquipment.put(EquipmentSlot.BOOTS.getSlot(), new Item(ItemId.CLIMBING_BOOTS));
//                zerkMeleeEquipment.put(EquipmentSlot.RING.getSlot(), new Item(ItemId.BERSERKER_RING));
//
//                Map<Integer, Item> zerkMeleeInventory = new HashMap<>();
//                zerkMeleeInventory.put(0, new Item(ItemId.SANFEW_SERUM4));
//                zerkMeleeInventory.put(1, new Item(ItemId.SANFEW_SERUM4));
//                zerkMeleeInventory.put(2, new Item(ItemId.SHARK));
//                zerkMeleeInventory.put(3, new Item(ItemId.SHARK));
//                zerkMeleeInventory.put(4, new Item(ItemId.SARADOMIN_BREW4));
//                zerkMeleeInventory.put(5, new Item(ItemId.SARADOMIN_BREW4));
//                zerkMeleeInventory.put(6, new Item(ItemId.SHARK));
//                zerkMeleeInventory.put(7, new Item(ItemId.SHARK));
//                zerkMeleeInventory.put(8, new Item(ItemId.SUPER_STRENGTH4));
//                zerkMeleeInventory.put(9, new Item(ItemId.SUPER_ATTACK4));
//                zerkMeleeInventory.put(10, new Item(ItemId.SHARK));
//                zerkMeleeInventory.put(11, new Item(ItemId.SHARK));
//                zerkMeleeInventory.put(12, new Item(ItemId.SHARK));
//                zerkMeleeInventory.put(13, new Item(ItemId.SHARK));
//                zerkMeleeInventory.put(14, new Item(ItemId.SHARK));
//                zerkMeleeInventory.put(15, new Item(ItemId.SHARK));
//                zerkMeleeInventory.put(16, new Item(ItemId.SHARK));
//                zerkMeleeInventory.put(17, new Item(ItemId.SHARK));
//                zerkMeleeInventory.put(18, new Item(ItemId.SHARK));
//                zerkMeleeInventory.put(19, new Item(ItemId.SHARK));
//                zerkMeleeInventory.put(20, new Item(ItemId.DRAGON_DAGGER));
//                zerkMeleeInventory.put(21, new Item(ItemId.SHARK));
//                zerkMeleeInventory.put(22, new Item(ItemId.SARADOMIN_BREW4));
//                zerkMeleeInventory.put(23, new Item(ItemId.SHARK));
//                zerkMeleeInventory.put(24, new Item(ItemId.SHARK));
//                zerkMeleeInventory.put(25, new Item(ItemId.ASTRAL_RUNE, 2000));
//                zerkMeleeInventory.put(26, new Item(ItemId.EARTH_RUNE, 2000));
//                zerkMeleeInventory.put(27, new Item(ItemId.DEATH_RUNE, 2000));
//
//                Map<Integer, Integer> zerkMeleeStats = new HashMap<>();
//                zerkMeleeStats.put(SkillConstants.ATTACK, 75);
//                zerkMeleeStats.put(SkillConstants.STRENGTH, 99);
//                zerkMeleeStats.put(SkillConstants.DEFENCE, 45);
//                zerkMeleeStats.put(SkillConstants.HITPOINTS, 99);
//                zerkMeleeStats.put(SkillConstants.RANGED, 99);
//                zerkMeleeStats.put(SkillConstants.PRAYER, 52);
//                zerkMeleeStats.put(SkillConstants.MAGIC, 99);
//
//                Preset zerkMeleePreset = new Preset("[pre-made]Zerk Melee", zerkMeleeEquipment, zerkMeleeInventory, Spellbook.LUNAR, zerkMeleeStats);
//                zerkMeleePreset.setLocked(true);
//
//                if (zerkMeleePreset.getSkillLevels() == null && zerkMeleePreset.isLocked()) {
//                    System.err.println("[BUG] Locked preset created without skill levels: " + zerkMeleePreset.getName());
//                }
//
//                presets.add(zerkMeleePreset);
//            }
//            Optional<Preset> zerkTribrid = this.presets.stream()
//                    .filter(p -> "[pre-made]Zerktribrid".equalsIgnoreCase(p.getName()))
//                    .findFirst();
//            if (!zerkTribrid.isPresent() || zerkTribrid.get().getSkillLevels() == null || zerkTribrid.get().getSkillLevels().isEmpty()) {
//                System.err.println("[FIX] Replacing missing or broken Zerktribrid preset");
//                zerkTribrid.ifPresent(this.presets::remove);
//
//                Map<Integer, Item> zerkTribridEquipment = new HashMap<>();
//                zerkTribridEquipment.put(EquipmentSlot.HELMET.getSlot(), new Item(ItemId.BERSERKER_HELM));
//                zerkTribridEquipment.put(EquipmentSlot.CAPE.getSlot(), new Item(ItemId.SARADOMIN_CAPE));
//                zerkTribridEquipment.put(EquipmentSlot.AMULET.getSlot(), new Item(ItemId.AMULET_OF_GLORY));
//                zerkTribridEquipment.put(EquipmentSlot.WEAPON.getSlot(), new Item(ItemId.ANCIENT_STAFF));
//                zerkTribridEquipment.put(EquipmentSlot.PLATE.getSlot(), new Item(ItemId.MYSTIC_ROBE_TOP));
//                zerkTribridEquipment.put(EquipmentSlot.SHIELD.getSlot(), new Item(ItemId.SPIRIT_SHIELD));
//                zerkTribridEquipment.put(EquipmentSlot.LEGS.getSlot(), new Item(ItemId.MYSTIC_ROBE_BOTTOM));
//                zerkTribridEquipment.put(EquipmentSlot.HANDS.getSlot(), new Item(ItemId.BARROWS_GLOVES));
//                zerkTribridEquipment.put(EquipmentSlot.BOOTS.getSlot(), new Item(ItemId.CLIMBING_BOOTS));
//                zerkTribridEquipment.put(EquipmentSlot.RING.getSlot(), new Item(ItemId.BERSERKER_RING));
//                zerkTribridEquipment.put(EquipmentSlot.AMMUNITION.getSlot(), new Item(ItemId.DRAGONSTONE_BOLTS, 1000));
//
//                Map<Integer, Item> zerkTribridInventory = new HashMap<>();
//                zerkTribridInventory.put(0, new Item(ItemId.SARADOMIN_BREW4));
//                zerkTribridInventory.put(1, new Item(ItemId.SUPER_RESTORE4));
//                zerkTribridInventory.put(2, new Item(ItemId.SHARK));
//                zerkTribridInventory.put(3, new Item(ItemId.SHARK));
//                zerkTribridInventory.put(4, new Item(ItemId.SUPER_STRENGTH4));
//                zerkTribridInventory.put(5, new Item(ItemId.SUPER_ATTACK4));
//                zerkTribridInventory.put(6, new Item(ItemId.BLACK_DHIDE_BODY));
//                zerkTribridInventory.put(7, new Item(ItemId.BLACK_DHIDE_CHAPS));
//                zerkTribridInventory.put(8, new Item(ItemId.RUNE_CROSSBOW));
//                zerkTribridInventory.put(9, new Item(ItemId.SHARK));
//                zerkTribridInventory.put(10, new Item(ItemId.FIGHTER_TORSO));
//                zerkTribridInventory.put(11, new Item(ItemId.RUNE_PLATELEGS));
//                zerkTribridInventory.put(12, new Item(ItemId.DRAGON_SCIMITAR));
//                zerkTribridInventory.put(13, new Item(ItemId.DRAGON_DAGGER));
//                zerkTribridInventory.put(14, new Item(ItemId.SHARK));
//                zerkTribridInventory.put(15, new Item(ItemId.SHARK));
//                zerkTribridInventory.put(16, new Item(ItemId.SHARK));
//                zerkTribridInventory.put(17, new Item(ItemId.SHARK));
//                zerkTribridInventory.put(18, new Item(ItemId.SHARK));
//                zerkTribridInventory.put(19, new Item(ItemId.SHARK));
//                zerkTribridInventory.put(20, new Item(ItemId.SHARK));
//                zerkTribridInventory.put(21, new Item(ItemId.SHARK));
//                zerkTribridInventory.put(22, new Item(ItemId.SHARK));
//                zerkTribridInventory.put(23, new Item(ItemId.SHARK));
//                zerkTribridInventory.put(24, new Item(ItemId.BLOOD_RUNE, 1000));
//                zerkTribridInventory.put(25, new Item(ItemId.FIRE_RUNE, 2000));
//                zerkTribridInventory.put(26, new Item(ItemId.DEATH_RUNE, 2000));
//                zerkTribridInventory.put(27, new Item(ItemId.TELEPORT_TO_HOUSE));
//
//                Map<Integer, Integer> zerkTribridStats = new HashMap<>();
//                zerkTribridStats.put(SkillConstants.ATTACK, 75);
//                zerkTribridStats.put(SkillConstants.STRENGTH, 99);
//                zerkTribridStats.put(SkillConstants.DEFENCE, 45);
//                zerkTribridStats.put(SkillConstants.HITPOINTS, 99);
//                zerkTribridStats.put(SkillConstants.RANGED, 99);
//                zerkTribridStats.put(SkillConstants.PRAYER, 52);
//                zerkTribridStats.put(SkillConstants.MAGIC, 99);
//
//                Preset zerkTribridPreset = new Preset("[pre-made]Zerktribrid", zerkTribridEquipment, zerkTribridInventory, Spellbook.ANCIENT, zerkTribridStats);
//                zerkTribridPreset.setLocked(true);
//
//                if (zerkTribridPreset.getSkillLevels() == null && zerkTribridPreset.isLocked()) {
//                    System.err.println("[BUG] Locked preset created without skill levels: " + zerkTribridPreset.getName());
//                }
//
//                presets.add(zerkTribridPreset);
//            }
//
//
//
//            revalidatePresets();

        }
        public static Map<String, Preset> getPremadePresets() {
            Map<String, Preset> premades = new HashMap<>();

            // ========== MAX MELEE ==========
            Map<Integer, Item> meleeEquipment = new HashMap<>();
            meleeEquipment.put(EquipmentSlot.HELMET.getSlot(), new Item(ItemId.HELM_OF_NEITIZNOT));
            meleeEquipment.put(EquipmentSlot.CAPE.getSlot(), new Item(ItemId.CAPE_OF_LEGENDS));
            meleeEquipment.put(EquipmentSlot.AMULET.getSlot(), new Item(ItemId.AMULET_OF_GLORY));
            meleeEquipment.put(EquipmentSlot.WEAPON.getSlot(), new Item(ItemId.DRAGON_SCIMITAR));
            meleeEquipment.put(EquipmentSlot.PLATE.getSlot(), new Item(ItemId.RUNE_PLATEBODY));
            meleeEquipment.put(EquipmentSlot.SHIELD.getSlot(), new Item(ItemId.RUNE_DEFENDER));
            meleeEquipment.put(EquipmentSlot.LEGS.getSlot(), new Item(ItemId.RUNE_PLATELEGS));
            meleeEquipment.put(EquipmentSlot.HANDS.getSlot(), new Item(ItemId.BARROWS_GLOVES));
            meleeEquipment.put(EquipmentSlot.BOOTS.getSlot(), new Item(ItemId.RUNE_BOOTS));
            meleeEquipment.put(EquipmentSlot.RING.getSlot(), new Item(ItemId.RING_OF_RECOIL));

            Map<Integer, Item> meleeInventory = new HashMap<>();
            meleeInventory.put(0, new Item(ItemId.SARADOMIN_BREW4));
            meleeInventory.put(1, new Item(ItemId.SARADOMIN_BREW4));
            meleeInventory.put(2, new Item(ItemId.SHARK));
            meleeInventory.put(3, new Item(ItemId.SHARK));
            meleeInventory.put(4, new Item(ItemId.SANFEW_SERUM4));
            meleeInventory.put(5, new Item(ItemId.SANFEW_SERUM4));
            meleeInventory.put(6, new Item(ItemId.SHARK));
            meleeInventory.put(7, new Item(ItemId.SHARK));
            meleeInventory.put(8, new Item(ItemId.SUPER_STRENGTH4));
            meleeInventory.put(9, new Item(ItemId.SUPER_ATTACK4));
            meleeInventory.put(10, new Item(ItemId.SHARK));
            meleeInventory.put(11, new Item(ItemId.SHARK));
            meleeInventory.put(12, new Item(ItemId.SHARK));
            meleeInventory.put(13, new Item(ItemId.SHARK));
            meleeInventory.put(14, new Item(ItemId.SHARK));
            meleeInventory.put(15, new Item(ItemId.SHARK));
            meleeInventory.put(16, new Item(ItemId.SHARK));
            meleeInventory.put(17, new Item(ItemId.SHARK));
            meleeInventory.put(18, new Item(ItemId.SHARK));
            meleeInventory.put(19, new Item(ItemId.SHARK));
            meleeInventory.put(20, new Item(ItemId.DRAGON_DAGGER));
            meleeInventory.put(21, new Item(ItemId.SHARK));
            meleeInventory.put(22, new Item(ItemId.SHARK));
            meleeInventory.put(23, new Item(ItemId.SHARK));
            meleeInventory.put(24, new Item(ItemId.SHARK));
            meleeInventory.put(25, new Item(ItemId.DEATH_RUNE, 250));
            meleeInventory.put(26, new Item(ItemId.ASTRAL_RUNE, 250));
            meleeInventory.put(27, new Item(ItemId.EARTH_RUNE, 500));

            Map<Integer, Integer> meleeStats = new HashMap<>();
            meleeStats.put(SkillConstants.ATTACK, 99);
            meleeStats.put(SkillConstants.STRENGTH, 99);
            meleeStats.put(SkillConstants.DEFENCE, 99);
            meleeStats.put(SkillConstants.HITPOINTS, 99);
            meleeStats.put(SkillConstants.PRAYER, 99);
            meleeStats.put(SkillConstants.RANGED, 99);
            meleeStats.put(SkillConstants.MAGIC, 99);

            Preset meleePreset = new Preset("126 Max Melee", meleeEquipment, meleeInventory, Spellbook.LUNAR, meleeStats);
            meleePreset.setLocked(true);
            premades.put("MAX_MELEE", meleePreset);

            Map<Integer, Item> tribridEquipment = new HashMap<>();
            tribridEquipment.put(EquipmentSlot.HELMET.getSlot(), new Item(ItemId.HELM_OF_NEITIZNOT));
            tribridEquipment.put(EquipmentSlot.CAPE.getSlot(), new Item(ItemId.SARADOMIN_CAPE));
            tribridEquipment.put(EquipmentSlot.AMULET.getSlot(), new Item(ItemId.AMULET_OF_GLORY));
            tribridEquipment.put(EquipmentSlot.WEAPON.getSlot(), new Item(ItemId.ANCIENT_STAFF));
            tribridEquipment.put(EquipmentSlot.PLATE.getSlot(), new Item(ItemId.MYSTIC_ROBE_TOP));
            tribridEquipment.put(EquipmentSlot.SHIELD.getSlot(), new Item(ItemId.SPIRIT_SHIELD));
            tribridEquipment.put(EquipmentSlot.LEGS.getSlot(), new Item(ItemId.MYSTIC_ROBE_BOTTOM));
            tribridEquipment.put(EquipmentSlot.HANDS.getSlot(), new Item(ItemId.BARROWS_GLOVES));
            tribridEquipment.put(EquipmentSlot.BOOTS.getSlot(), new Item(ItemId.CLIMBING_BOOTS));
            tribridEquipment.put(EquipmentSlot.RING.getSlot(), new Item(ItemId.RING_OF_RECOIL));
            tribridEquipment.put(EquipmentSlot.AMMUNITION.getSlot(), new Item(ItemId.DRAGONSTONE_BOLTS_E, 100));

            Map<Integer, Item> tribridInventory = new HashMap<>();
            tribridInventory.put(0, new Item(ItemId.SANFEW_SERUM4));
            tribridInventory.put(1, new Item(ItemId.SANFEW_SERUM4));
            tribridInventory.put(2, new Item(ItemId.SANFEW_SERUM4));
            tribridInventory.put(3, new Item(ItemId.SANFEW_SERUM4));
            tribridInventory.put(4, new Item(ItemId.SARADOMIN_BREW4));
            tribridInventory.put(5, new Item(ItemId.SARADOMIN_BREW4));
            tribridInventory.put(6, new Item(ItemId.SARADOMIN_BREW4));
            tribridInventory.put(7, new Item(ItemId.SARADOMIN_BREW4));
            tribridInventory.put(8, new Item(ItemId.RUNE_CROSSBOW));
            tribridInventory.put(9, new Item(ItemId.DRAGON_SCIMITAR));
            tribridInventory.put(10, new Item(ItemId.SARADOMIN_BREW4));
            tribridInventory.put(11, new Item(ItemId.SARADOMIN_BREW4));
            tribridInventory.put(12, new Item(ItemId.BLACK_DHIDE_BODY));
            tribridInventory.put(13, new Item(ItemId.RUNE_DEFENDER));
            tribridInventory.put(14, new Item(ItemId.SARADOMIN_BREW4));
            tribridInventory.put(15, new Item(ItemId.SARADOMIN_BREW4));
            tribridInventory.put(16, new Item(ItemId.RUNE_PLATELEGS));
            tribridInventory.put(17, new Item(ItemId.DRAGON_DAGGERP));
            tribridInventory.put(18, new Item(ItemId.SHARK));
            tribridInventory.put(19, new Item(ItemId.SARADOMIN_BREW4));
            tribridInventory.put(20, new Item(ItemId.MAGIC_POTION4));
            tribridInventory.put(21, new Item(ItemId.SUPER_STRENGTH4));
            tribridInventory.put(22, new Item(ItemId.SUPER_ATTACK4));
            tribridInventory.put(23, new Item(ItemId.SHARK));
            tribridInventory.put(24, new Item(ItemId.SHARK));
            tribridInventory.put(25, new Item(ItemId.BLOOD_RUNE, 10000));
            tribridInventory.put(26, new Item(ItemId.WATER_RUNE, 10000));
            tribridInventory.put(27, new Item(ItemId.DEATH_RUNE, 10000));

            Map<Integer, Integer> tribridStats = new HashMap<>();
            tribridStats.put(SkillConstants.ATTACK, 99);
            tribridStats.put(SkillConstants.STRENGTH, 99);
            tribridStats.put(SkillConstants.DEFENCE, 99);
            tribridStats.put(SkillConstants.HITPOINTS, 99);
            tribridStats.put(SkillConstants.RANGED, 99);
            tribridStats.put(SkillConstants.PRAYER, 99);
            tribridStats.put(SkillConstants.MAGIC, 99);

            Preset tribridPreset = new Preset("126 Tribrid", tribridEquipment, tribridInventory, Spellbook.ANCIENT, tribridStats);
            tribridPreset.setLocked(true);
            premades.put("TRIBRID", tribridPreset);

            Map<Integer, Item> pureMeleeEquipment = new HashMap<>();
            pureMeleeEquipment.put(EquipmentSlot.HELMET.getSlot(), new Item(ItemId.BEARHEAD));
            pureMeleeEquipment.put(EquipmentSlot.CAPE.getSlot(), new Item(ItemId.CAPE_OF_LEGENDS));
            pureMeleeEquipment.put(EquipmentSlot.AMULET.getSlot(), new Item(ItemId.AMULET_OF_GLORY));
            pureMeleeEquipment.put(EquipmentSlot.WEAPON.getSlot(), new Item(ItemId.DRAGON_SCIMITAR));
            pureMeleeEquipment.put(EquipmentSlot.PLATE.getSlot(), new Item(ItemId.MONKS_ROBE_TOP));
            pureMeleeEquipment.put(EquipmentSlot.SHIELD.getSlot(), new Item(ItemId.BOOK_OF_LAW));
            pureMeleeEquipment.put(EquipmentSlot.LEGS.getSlot(), new Item(ItemId.BLACK_DHIDE_CHAPS));
            pureMeleeEquipment.put(EquipmentSlot.HANDS.getSlot(), new Item(ItemId.MITHRIL_GLOVES));
            pureMeleeEquipment.put(EquipmentSlot.BOOTS.getSlot(), new Item(ItemId.CLIMBING_BOOTS));
            pureMeleeEquipment.put(EquipmentSlot.RING.getSlot(), new Item(ItemId.RING_OF_RECOIL));

            Map<Integer, Item> pureMeleeInventory = new HashMap<>();
            pureMeleeInventory.put(0, new Item(ItemId.SARADOMIN_BREW4));
            pureMeleeInventory.put(1, new Item(ItemId.SARADOMIN_BREW4));
            pureMeleeInventory.put(2, new Item(ItemId.SHARK));
            pureMeleeInventory.put(3, new Item(ItemId.SHARK));
            pureMeleeInventory.put(4, new Item(ItemId.SANFEW_SERUM4));
            pureMeleeInventory.put(5, new Item(ItemId.SANFEW_SERUM4));
            pureMeleeInventory.put(6, new Item(ItemId.SHARK));
            pureMeleeInventory.put(7, new Item(ItemId.SHARK));
            pureMeleeInventory.put(8, new Item(ItemId.SUPER_STRENGTH4));
            pureMeleeInventory.put(9, new Item(ItemId.SUPER_ATTACK4));
            pureMeleeInventory.put(10, new Item(ItemId.SHARK));
            pureMeleeInventory.put(11, new Item(ItemId.SHARK));
            pureMeleeInventory.put(12, new Item(ItemId.SHARK));
            pureMeleeInventory.put(13, new Item(ItemId.SHARK));
            pureMeleeInventory.put(14, new Item(ItemId.SHARK));
            pureMeleeInventory.put(15, new Item(ItemId.SHARK));
            pureMeleeInventory.put(16, new Item(ItemId.SHARK));
            pureMeleeInventory.put(17, new Item(ItemId.SHARK));
            pureMeleeInventory.put(18, new Item(ItemId.SHARK));
            pureMeleeInventory.put(19, new Item(ItemId.SHARK));
            pureMeleeInventory.put(20, new Item(ItemId.DRAGON_DAGGER));
            pureMeleeInventory.put(21, new Item(ItemId.SHARK));
            pureMeleeInventory.put(22, new Item(ItemId.SHARK));
            pureMeleeInventory.put(23, new Item(ItemId.SHARK));
            pureMeleeInventory.put(24, new Item(ItemId.DRAGON_2H_SWORD));
            pureMeleeInventory.put(25, new Item(ItemId.SHARK));
            pureMeleeInventory.put(26, new Item(ItemId.SHARK));
            pureMeleeInventory.put(27, new Item(ItemId.SHARK));

            Map<Integer, Integer> pureMeleeStats = new HashMap<>();
            pureMeleeStats.put(SkillConstants.ATTACK, 75);
            pureMeleeStats.put(SkillConstants.STRENGTH, 99);
            pureMeleeStats.put(SkillConstants.DEFENCE, 1);
            pureMeleeStats.put(SkillConstants.HITPOINTS, 99);
            pureMeleeStats.put(SkillConstants.RANGED, 99);
            pureMeleeStats.put(SkillConstants.PRAYER, 52);
            pureMeleeStats.put(SkillConstants.MAGIC, 99);

            Preset pureMeleePreset = new Preset("Pure Melee", pureMeleeEquipment, pureMeleeInventory, Spellbook.NORMAL, pureMeleeStats);
            pureMeleePreset.setLocked(true);
            premades.put("PURE_MELEE", pureMeleePreset);

            Map<Integer, Item> pureNhEquipment = new HashMap<>();
            pureNhEquipment.put(EquipmentSlot.HELMET.getSlot(), new Item(ItemId.GHOSTLY_HOOD));
            pureNhEquipment.put(EquipmentSlot.CAPE.getSlot(), new Item(ItemId.SARADOMIN_CAPE));
            pureNhEquipment.put(EquipmentSlot.AMULET.getSlot(), new Item(ItemId.AMULET_OF_GLORY));
            pureNhEquipment.put(EquipmentSlot.WEAPON.getSlot(), new Item(ItemId.ANCIENT_STAFF));
            pureNhEquipment.put(EquipmentSlot.PLATE.getSlot(), new Item(ItemId.GHOSTLY_ROBE));
            pureNhEquipment.put(EquipmentSlot.SHIELD.getSlot(), new Item(ItemId.BOOK_OF_LAW));
            pureNhEquipment.put(EquipmentSlot.LEGS.getSlot(), new Item(ItemId.GHOSTLY_ROBE_6108));
            pureNhEquipment.put(EquipmentSlot.HANDS.getSlot(), new Item(ItemId.MITHRIL_GLOVES));
            pureNhEquipment.put(EquipmentSlot.BOOTS.getSlot(), new Item(ItemId.CLIMBING_BOOTS));
            pureNhEquipment.put(EquipmentSlot.RING.getSlot(), new Item(ItemId.RING_OF_RECOIL));
            pureNhEquipment.put(EquipmentSlot.AMMUNITION.getSlot(), new Item(ItemId.DRAGONSTONE_BOLTS, 2000));

            Map<Integer, Item> pureNhInventory = new HashMap<>();
            pureNhInventory.put(0, new Item(ItemId.SANFEW_SERUM4));
            pureNhInventory.put(1, new Item(ItemId.SANFEW_SERUM4));
            pureNhInventory.put(2, new Item(ItemId.SHARK));
            pureNhInventory.put(3, new Item(ItemId.SHARK));
            pureNhInventory.put(4, new Item(ItemId.SANFEW_SERUM4));
            pureNhInventory.put(5, new Item(ItemId.SANFEW_SERUM4));
            pureNhInventory.put(6, new Item(ItemId.SHARK));
            pureNhInventory.put(7, new Item(ItemId.SHARK));
            pureNhInventory.put(8, new Item(ItemId.SUPER_STRENGTH4));
            pureNhInventory.put(9, new Item(ItemId.SUPER_ATTACK4));
            pureNhInventory.put(10, new Item(ItemId.RANGING_POTION4));
            pureNhInventory.put(11, new Item(ItemId.SARADOMIN_BREW4));
            pureNhInventory.put(12, new Item(ItemId.SARADOMIN_BREW4));
            pureNhInventory.put(13, new Item(ItemId.SARADOMIN_BREW4));
            pureNhInventory.put(14, new Item(ItemId.SARADOMIN_BREW4));
            pureNhInventory.put(15, new Item(ItemId.SARADOMIN_BREW4));
            pureNhInventory.put(16, new Item(ItemId.AVAS_ACCUMULATOR));
            pureNhInventory.put(17, new Item(ItemId.BLACK_DHIDE_CHAPS));
            pureNhInventory.put(18, new Item(ItemId.SARADOMIN_BREW4));
            pureNhInventory.put(19, new Item(ItemId.SARADOMIN_BREW4));
            pureNhInventory.put(20, new Item(ItemId.DRAGON_DAGGER));
            pureNhInventory.put(21, new Item(ItemId.RUNE_CROSSBOW));
            pureNhInventory.put(22, new Item(ItemId.SARADOMIN_BREW4));
            pureNhInventory.put(23, new Item(ItemId.SHARK));
            pureNhInventory.put(24, new Item(ItemId.DRAGON_SCIMITAR));
            pureNhInventory.put(25, new Item(ItemId.BLOOD_RUNE, 2000));
            pureNhInventory.put(26, new Item(ItemId.WATER_RUNE, 2000));
            pureNhInventory.put(27, new Item(ItemId.DEATH_RUNE, 2000));

            Map<Integer, Integer> pureNhStats = new HashMap<>();
            pureNhStats.put(SkillConstants.ATTACK, 75);
            pureNhStats.put(SkillConstants.STRENGTH, 99);
            pureNhStats.put(SkillConstants.DEFENCE, 1);
            pureNhStats.put(SkillConstants.HITPOINTS, 99);
            pureNhStats.put(SkillConstants.RANGED, 99);
            pureNhStats.put(SkillConstants.PRAYER, 52);
            pureNhStats.put(SkillConstants.MAGIC, 99);

            Preset pureNhPreset = new Preset("Pure NH", pureNhEquipment, pureNhInventory, Spellbook.ANCIENT, pureNhStats);
            pureNhPreset.setLocked(true);
            premades.put("PURE_NH", pureNhPreset);

            // ========== ZERK MELEE ==========
            Map<Integer, Item> zerkMeleeEquipment = new HashMap<>();
            zerkMeleeEquipment.put(EquipmentSlot.HELMET.getSlot(), new Item(ItemId.BERSERKER_HELM));
            zerkMeleeEquipment.put(EquipmentSlot.CAPE.getSlot(), new Item(ItemId.FIRE_CAPE));
            zerkMeleeEquipment.put(EquipmentSlot.AMULET.getSlot(), new Item(ItemId.AMULET_OF_GLORY));
            zerkMeleeEquipment.put(EquipmentSlot.WEAPON.getSlot(), new Item(ItemId.DRAGON_SCIMITAR));
            zerkMeleeEquipment.put(EquipmentSlot.PLATE.getSlot(), new Item(ItemId.FIGHTER_TORSO));
            zerkMeleeEquipment.put(EquipmentSlot.SHIELD.getSlot(), new Item(ItemId.RUNE_DEFENDER));
            zerkMeleeEquipment.put(EquipmentSlot.LEGS.getSlot(), new Item(ItemId.RUNE_PLATELEGS));
            zerkMeleeEquipment.put(EquipmentSlot.HANDS.getSlot(), new Item(ItemId.BARROWS_GLOVES));
            zerkMeleeEquipment.put(EquipmentSlot.BOOTS.getSlot(), new Item(ItemId.CLIMBING_BOOTS));
            zerkMeleeEquipment.put(EquipmentSlot.RING.getSlot(), new Item(ItemId.BERSERKER_RING));

            Map<Integer, Item> zerkMeleeInventory = new HashMap<>();
            zerkMeleeInventory.put(0, new Item(ItemId.SANFEW_SERUM4));
            zerkMeleeInventory.put(1, new Item(ItemId.SANFEW_SERUM4));
            zerkMeleeInventory.put(2, new Item(ItemId.SHARK));
            zerkMeleeInventory.put(3, new Item(ItemId.SHARK));
            zerkMeleeInventory.put(4, new Item(ItemId.SARADOMIN_BREW4));
            zerkMeleeInventory.put(5, new Item(ItemId.SARADOMIN_BREW4));
            zerkMeleeInventory.put(6, new Item(ItemId.SHARK));
            zerkMeleeInventory.put(7, new Item(ItemId.SHARK));
            zerkMeleeInventory.put(8, new Item(ItemId.SUPER_STRENGTH4));
            zerkMeleeInventory.put(9, new Item(ItemId.SUPER_ATTACK4));
            zerkMeleeInventory.put(10, new Item(ItemId.SHARK));
            zerkMeleeInventory.put(11, new Item(ItemId.SHARK));
            zerkMeleeInventory.put(12, new Item(ItemId.SHARK));
            zerkMeleeInventory.put(13, new Item(ItemId.SHARK));
            zerkMeleeInventory.put(14, new Item(ItemId.SHARK));
            zerkMeleeInventory.put(15, new Item(ItemId.SHARK));
            zerkMeleeInventory.put(16, new Item(ItemId.SHARK));
            zerkMeleeInventory.put(17, new Item(ItemId.SHARK));
            zerkMeleeInventory.put(18, new Item(ItemId.SHARK));
            zerkMeleeInventory.put(19, new Item(ItemId.SHARK));
            zerkMeleeInventory.put(20, new Item(ItemId.DRAGON_DAGGER));
            zerkMeleeInventory.put(21, new Item(ItemId.SHARK));
            zerkMeleeInventory.put(22, new Item(ItemId.SARADOMIN_BREW4));
            zerkMeleeInventory.put(23, new Item(ItemId.SHARK));
            zerkMeleeInventory.put(24, new Item(ItemId.SHARK));
            zerkMeleeInventory.put(25, new Item(ItemId.ASTRAL_RUNE, 2000));
            zerkMeleeInventory.put(26, new Item(ItemId.EARTH_RUNE, 2000));
            zerkMeleeInventory.put(27, new Item(ItemId.DEATH_RUNE, 2000));

            Map<Integer, Integer> zerkMeleeStats = new HashMap<>();
            zerkMeleeStats.put(SkillConstants.ATTACK, 75);
            zerkMeleeStats.put(SkillConstants.STRENGTH, 99);
            zerkMeleeStats.put(SkillConstants.DEFENCE, 45);
            zerkMeleeStats.put(SkillConstants.HITPOINTS, 99);
            zerkMeleeStats.put(SkillConstants.RANGED, 99);
            zerkMeleeStats.put(SkillConstants.PRAYER, 52);
            zerkMeleeStats.put(SkillConstants.MAGIC, 99);

            Preset zerkMeleePreset = new Preset("Zerk Melee", zerkMeleeEquipment, zerkMeleeInventory, Spellbook.LUNAR, zerkMeleeStats);
            zerkMeleePreset.setLocked(true);
            premades.put("ZERK_MELEE", zerkMeleePreset);


// ========== ZERK TRIBRID ==========
            Map<Integer, Item> zerkTribridEquipment = new HashMap<>();
            zerkTribridEquipment.put(EquipmentSlot.HELMET.getSlot(), new Item(ItemId.BERSERKER_HELM));
            zerkTribridEquipment.put(EquipmentSlot.CAPE.getSlot(), new Item(ItemId.SARADOMIN_CAPE));
            zerkTribridEquipment.put(EquipmentSlot.AMULET.getSlot(), new Item(ItemId.AMULET_OF_GLORY));
            zerkTribridEquipment.put(EquipmentSlot.WEAPON.getSlot(), new Item(ItemId.ANCIENT_STAFF));
            zerkTribridEquipment.put(EquipmentSlot.PLATE.getSlot(), new Item(ItemId.MYSTIC_ROBE_TOP));
            zerkTribridEquipment.put(EquipmentSlot.SHIELD.getSlot(), new Item(ItemId.SPIRIT_SHIELD));
            zerkTribridEquipment.put(EquipmentSlot.LEGS.getSlot(), new Item(ItemId.MYSTIC_ROBE_BOTTOM));
            zerkTribridEquipment.put(EquipmentSlot.HANDS.getSlot(), new Item(ItemId.BARROWS_GLOVES));
            zerkTribridEquipment.put(EquipmentSlot.BOOTS.getSlot(), new Item(ItemId.CLIMBING_BOOTS));
            zerkTribridEquipment.put(EquipmentSlot.RING.getSlot(), new Item(ItemId.BERSERKER_RING));
            zerkTribridEquipment.put(EquipmentSlot.AMMUNITION.getSlot(), new Item(ItemId.DRAGONSTONE_BOLTS, 1000));

            Map<Integer, Item> zerkTribridInventory = new HashMap<>();
            zerkTribridInventory.put(0, new Item(ItemId.SARADOMIN_BREW4));
            zerkTribridInventory.put(1, new Item(ItemId.SUPER_RESTORE4));
            zerkTribridInventory.put(2, new Item(ItemId.SHARK));
            zerkTribridInventory.put(3, new Item(ItemId.SHARK));
            zerkTribridInventory.put(4, new Item(ItemId.SUPER_STRENGTH4));
            zerkTribridInventory.put(5, new Item(ItemId.SUPER_ATTACK4));
            zerkTribridInventory.put(6, new Item(ItemId.BLACK_DHIDE_BODY));
            zerkTribridInventory.put(7, new Item(ItemId.BLACK_DHIDE_CHAPS));
            zerkTribridInventory.put(8, new Item(ItemId.RUNE_CROSSBOW));
            zerkTribridInventory.put(9, new Item(ItemId.SHARK));
            zerkTribridInventory.put(10, new Item(ItemId.FIGHTER_TORSO));
            zerkTribridInventory.put(11, new Item(ItemId.RUNE_PLATELEGS));
            zerkTribridInventory.put(12, new Item(ItemId.DRAGON_SCIMITAR));
            zerkTribridInventory.put(13, new Item(ItemId.DRAGON_DAGGER));
            zerkTribridInventory.put(14, new Item(ItemId.SHARK));
            zerkTribridInventory.put(15, new Item(ItemId.SHARK));
            zerkTribridInventory.put(16, new Item(ItemId.SHARK));
            zerkTribridInventory.put(17, new Item(ItemId.SHARK));
            zerkTribridInventory.put(18, new Item(ItemId.SHARK));
            zerkTribridInventory.put(19, new Item(ItemId.SHARK));
            zerkTribridInventory.put(20, new Item(ItemId.SHARK));
            zerkTribridInventory.put(21, new Item(ItemId.SHARK));
            zerkTribridInventory.put(22, new Item(ItemId.SHARK));
            zerkTribridInventory.put(23, new Item(ItemId.SHARK));
            zerkTribridInventory.put(24, new Item(ItemId.BLOOD_RUNE, 1000));
            zerkTribridInventory.put(25, new Item(ItemId.FIRE_RUNE, 2000));
            zerkTribridInventory.put(26, new Item(ItemId.DEATH_RUNE, 2000));
            zerkTribridInventory.put(27, new Item(ItemId.TELEPORT_TO_HOUSE));

            Map<Integer, Integer> zerkTribridStats = new HashMap<>();
            zerkTribridStats.put(SkillConstants.ATTACK, 75);
            zerkTribridStats.put(SkillConstants.STRENGTH, 99);
            zerkTribridStats.put(SkillConstants.DEFENCE, 45);
            zerkTribridStats.put(SkillConstants.HITPOINTS, 99);
            zerkTribridStats.put(SkillConstants.RANGED, 99);
            zerkTribridStats.put(SkillConstants.PRAYER, 52);
            zerkTribridStats.put(SkillConstants.MAGIC, 99);

            Preset zerkTribridPreset = new Preset("Zerk Tribrid", zerkTribridEquipment, zerkTribridInventory, Spellbook.ANCIENT, zerkTribridStats);
            zerkTribridPreset.setLocked(true);
            premades.put("ZERK_TRIBRID", zerkTribridPreset);

            return premades;
        }



        public void revalidatePresets() {
            final int max = getMaximumPresets();
            final int current = getTotalPresets();
            for (int i = 0; i < current; i++) {
                final Preset preset = presets.get(i);
                preset.setAvailable(i < max);
            }
            if (defaultPreset >= max) {
                defaultPreset = 0;
            }
        }

        public void addPreset(final int index, final String name) {
            final Player player = this.player.get();
            if (player == null) {
                return;
            }
            presets.add(index, new Preset(name, player));
            revalidatePresets();
        }

        public void setPreset(final int index, @NotNull final Preset preset) {
            presets.set(index, preset);
        }

        @Nullable
        public Preset getPreset(final int index) {
            if (index < 0 || index >= getTotalPresets()) {
                return null;
            }
            return presets.get(index);
        }

        public int getTotalPresets() {
            return presets.size();
        }

        public int getMaximumPresets() {
            int availablePresets = 8;
            final Player player = this.player.get();
            if (player == null) {
                throw new IllegalStateException();
            }
            //Donator rank slots
            availablePresets += getSlotsAmount(player);
            return availablePresets;
        }

        public static int getSlotsAmount(Player player) {
            switch (player.getMemberRank()) {
                case PREMIUM:
                    return 5;
                case EXPANSION:
                    return 10;
                case EXTREME:
                    return 15;
                case RESPECTED:
                    return 20;
                case LEGENDARY:
                    return 30;
                case MYTHICAL:
                    return 40;
                case UBER:
                case AMASCUT:
                    return 50;
            }
            return 0;
        }

        public void setDefaultPreset(final int slot) {
            if (slot < 0 || slot >= getTotalPresets()) {
                return;
            }
            this.defaultPreset = slot;
        }

        public List<Preset> getPresets() {
            return presets;
        }

        public int getDefaultPreset() {
            return defaultPreset;
        }

        public int getUnlockedSlots() {
            return unlockedSlots;
        }

        public void loadLastPreset() {
            final Player player = this.player.get();
            if (player == null) {
                return;
            }

            int index = player.getNumericAttributeOrDefault("last preset loaded", -1).intValue();
            if (index <= -1) {
                player.sendMessage("You haven't loaded a preset yet.");
                return;
            }

            if (player.getGameMode() != GameMode.REGULAR) {
                player.sendMessage("Only players in Regular PvP mode may use presets.");
                return;
            }
            PresetManagerInterface.load(player, index);
            player.getUpdateFlags().flag(UpdateFlag.APPEARANCE);
            player.getVarManager().sendBit(13027, player.getSkills().getCombatLevel());
            WorldTasksManager.schedule(() -> player.getCombatDefinitions().refresh(), 1);

        }

    }
