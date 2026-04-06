package com.zenyte.plugins.renewednpc;

import com.zenyte.game.GameInterface;
import com.zenyte.game.content.globalshop.GlobalShopInterface;
import com.zenyte.game.world.entity.npc.actions.NPCPlugin;

public class HomeDonatorShop extends NPCPlugin {

    @Override
    public void handle() {
        bind("talk-to", (player, npc) -> {
            player.getTemporaryAttributes().put(GlobalShopInterface.DONATOR_MODE_KEY, Boolean.TRUE);
            player.getTemporaryAttributes().put("GlobalShopCategory", GlobalShopInterface.DONATOR_BOOSTERS);
            GameInterface.GLOBAL_SHOP.open(player);
        });
    }

    @Override
    public int[] getNPCs() {
        return new int[] { 5523 };
    }

}