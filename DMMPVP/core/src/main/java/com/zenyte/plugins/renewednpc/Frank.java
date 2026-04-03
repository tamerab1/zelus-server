package com.zenyte.plugins.renewednpc;

import com.zenyte.game.GameInterface;
import com.zenyte.game.world.entity.npc.actions.NPCPlugin;

public class Frank extends NPCPlugin {

    @Override
    public void handle() {
        bind("Shops", (player, npc) -> {
            player.getTemporaryAttributes().put("GlobalShopCategory", "Vote shop");
            GameInterface.GLOBAL_SHOP.open(player);
        });
    }

    @Override
    public int[] getNPCs() {
        return new int[] { 4425 };
    }

}