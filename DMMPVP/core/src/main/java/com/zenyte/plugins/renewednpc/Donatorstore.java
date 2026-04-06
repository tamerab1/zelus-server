package com.zenyte.plugins.renewednpc;

import com.zenyte.game.GameInterface;
import com.zenyte.game.content.globalshop.GlobalShopInterface;
import com.zenyte.game.world.entity.npc.actions.NPCPlugin;

public class Donatorstore extends NPCPlugin {

    @Override
    public void handle() {
        bind("talk-to", (player, npc) -> {
            // Mark this session as donator-shop mode so GlobalShopInterface
            // routes the first three tabs to Boosters / Boxes / Other and
            // shows the correct Donor Points balance on each switch.
            player.getTemporaryAttributes().put(GlobalShopInterface.DONATOR_MODE_KEY, Boolean.TRUE);
            // Default landing tab is Boosters.
            player.getTemporaryAttributes().put("GlobalShopCategory", GlobalShopInterface.DONATOR_BOOSTERS);
            GameInterface.GLOBAL_SHOP.open(player);
        });
    }

    @Override
    public int[] getNPCs() {
        return new int[] { 5523 };
    }

}
