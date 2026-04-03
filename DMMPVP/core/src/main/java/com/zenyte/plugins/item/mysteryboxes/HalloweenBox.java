package com.zenyte.plugins.item.mysteryboxes;

import com.zenyte.game.item.ItemId;
import com.zenyte.game.model.item.pluginextensions.ItemPlugin;
import com.zenyte.plugins.PluginPriority;
import com.zenyte.plugins.interfaces.MysteryBoxInterface;

@PluginPriority(9990)
public class HalloweenBox extends ItemPlugin {

    public static int totalWeight;
    public static MysteryItem[] rewards;
    public static MysterySupplyItem[] supplies;

    @Override
    public void handle() {
        bind("Open", (player, item, container, slotId) ->
                MysteryBoxInterface.openBox(player, item.getId(), rewards, totalWeight, supplies)
        );
        bind("Quick-Open", (player, item, container, slotId) ->
                MysteryBoxInterface.openBoxQuick(player, item.getId(), rewards, totalWeight, supplies)
        );

        // 🎃 Definieer jouw eigen Halloween rewards
        rewards = new MysteryItem[] {
                new MysteryItem(8149, 1, 1, 500), // Bronze casket
                new MysteryItem(8150, 1, 1, 300), // Silver casket
                new MysteryItem(8151, 1, 1, 100).announce(), // Gold casket
                new MysteryItem(30116, 100, 500, 1000), // Halloween tokens
                new MysteryItem(1419, 1, 1, 20).announce(), // Scythe
                new MysteryItem(1053, 1, 1, 10).announce(), // Halloween mask (red)
                new MysteryItem(1055, 1, 1, 10).announce(), // Blue
                new MysteryItem(1057, 1, 1, 10).announce(), // Green
        };

        totalWeight = MysteryItem.calculateTotalWeight(rewards);

        supplies = new MysterySupplyItem[] {
                new MysterySupplyItem(30116, 200, 2500)
        };
    }

    @Override
    public int[] getItems() {
        return new int[] {32162};
    }
}
