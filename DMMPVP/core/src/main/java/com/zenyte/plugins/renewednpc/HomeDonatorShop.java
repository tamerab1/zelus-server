package com.zenyte.plugins.renewednpc;

import com.zenyte.game.world.entity.npc.actions.NPCPlugin;

public class HomeDonatorShop extends NPCPlugin {

    @Override
    public void handle() {
        bind("talk-to", (player, npc) -> player.openShop("Donator store"));
    }

    @Override
    public int[] getNPCs() {
        return new int[] { 5523 };
    }

}