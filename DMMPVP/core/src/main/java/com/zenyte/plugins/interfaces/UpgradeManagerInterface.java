package com.zenyte.plugins.interfaces;

import com.zenyte.game.model.ui.Interface;
import com.zenyte.game.GameInterface;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.game.world.entity.player.upgrades.UpgradeCategory;

/**
 * Created by Advo on 11/18/2023
 */
public class UpgradeManagerInterface extends Interface {

    private static final int UPGRADE_BUTTON = 107;

    @Override
    protected void attach() {
        for (int i = 0; i < UpgradeCategory.values().length; i++) {
            put(23 + (i * 5), UpgradeCategory.values()[i].name());
        }
        put(UPGRADE_BUTTON, "UPGRADE");
    }

    @Override
    protected void build() {
        for (int i = 0; i < UpgradeCategory.values().length; i++) {
            UpgradeCategory category = UpgradeCategory.values()[i];
            bind(category.name(), (player, slotId, itemId, option) -> player.getUpgradeManager().selectCategory(category, player));
        }
        bind("UPGRADE", (player, slotId, itemId, option) -> player.getUpgradeManager().upgrade(player, option));
    }

    @Override
    public void open(Player player) {
        player.getInterfaceHandler().sendInterface(getInterface());
        player.getUpgradeManager().open();
    }

    @Override
    protected DefaultClickHandler getDefaultHandler() {
        return (player, componentId, slotId, itemId, optionId) -> {
            if(optionId == 6) {
                player.getUpgradeManager().checkInterface(player,itemId);
            }
        };
    }

    @Override
    public GameInterface getInterface() {
        return GameInterface.UPGRADE_INTERFACE;
    }
}