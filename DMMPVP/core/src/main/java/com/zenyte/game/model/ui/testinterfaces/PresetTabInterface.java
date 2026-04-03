package com.zenyte.game.model.ui.testinterfaces;

import com.zenyte.game.GameInterface;
import com.zenyte.game.content.preset.Preset;
import com.zenyte.game.content.preset.PresetManager;
import com.zenyte.game.item.Item;
import com.zenyte.game.model.ui.Interface;
import com.zenyte.game.model.ui.InterfacePosition;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.game.world.entity.player.privilege.GameMode;
import com.zenyte.game.world.region.area.wilderness.WildernessArea;
import com.zenyte.game.content.grandexchange.GrandExchange;

/**
 * Preset tab interface for premade loadouts.
 */
@SuppressWarnings("unused")
public class PresetTabInterface extends Interface {

    @Override
    protected void attach() {

        put(16, "Max Melee");
        put(23, "Tribrid");
        put(30, "Pure Melee");
        put(37, "Pure NH");

        put(44, "Zerk Melee");
        put(51, "Zerk Tribrid");
    }

    @Override
    public void open(final Player player) {
        final InterfacePosition pos = getInterface().getPosition();
        final int component = pos.getComponent(player.getInterfaceHandler().getPane());

        player.getInterfaceHandler().sendInterface(
                getInterface().getId(),
                component,
                player.getInterfaceHandler().getPane(),
                pos.isWalkable()
        );
    }

    private boolean canUsePresets(Player player) {
        if (!player.getBooleanAttribute("registered")) {
            player.sendMessage("You must first speak with the Zenyte Guide to choose your game mode.");
            return false;
        }
        if (player.getGameMode() != GameMode.REGULAR) {
            player.sendMessage("Only players in Regular PvP mode may use presets.");
            return false;
        }
        if (WildernessArea.isWithinWilderness(player.getLocation())) {
            player.sendMessage("You cannot use presets while in the Wilderness.");
            return false;
        }
        return true;
    }

    @Override
    protected void build() {
        bind("Max Melee", player -> {
            if (!canUsePresets(player)) return;
            Preset preset = PresetManager.getPremadePresets().get("MAX_MELEE");
            if (preset != null) {
                preset.load(player);
                blockPresetItems(preset);
            }
        });

        bind("Tribrid", player -> {
            if (!canUsePresets(player)) return;
            Preset preset = PresetManager.getPremadePresets().get("TRIBRID");
            if (preset != null) {
                preset.load(player);
                blockPresetItems(preset);
            }
        });

        bind("Pure Melee", player -> {
            if (!canUsePresets(player)) return;
            Preset preset = PresetManager.getPremadePresets().get("PURE_MELEE");
            if (preset != null) {
                preset.load(player);
                blockPresetItems(preset);
            }
        });

        bind("Pure NH", player -> {
            if (!canUsePresets(player)) return;
            Preset preset = PresetManager.getPremadePresets().get("PURE_NH");
            if (preset != null) {
                preset.load(player);
                blockPresetItems(preset);
            }
        });

        bind("Zerk Melee", player -> {
            if (!canUsePresets(player)) return;
            Preset preset = PresetManager.getPremadePresets().get("ZERK_MELEE");
            if (preset != null) {
                preset.load(player);
                blockPresetItems(preset);
            }
        });

        bind("Zerk Tribrid", player -> {
            if (!canUsePresets(player)) return;
            Preset preset = PresetManager.getPremadePresets().get("ZERK_TRIBRID");
            if (preset != null) {
                preset.load(player);
                blockPresetItems(preset);
            }
        });
    }

    private void blockPresetItems(Preset preset) {
        if (preset == null) return;

        for (Item item : preset.getInventory().values()) {
            if (item != null) {
                GrandExchange.addBlockedItemWithNotes(item);
            }
        }

        for (Item item : preset.getEquipment().values()) {
            if (item != null) {
                GrandExchange.addBlockedItemWithNotes(item);
            }
        }
    }

    @Override
    public GameInterface getInterface() {
        return GameInterface.LOTTERY;
    }
}
