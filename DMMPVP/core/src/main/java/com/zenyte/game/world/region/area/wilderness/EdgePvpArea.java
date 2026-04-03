package com.zenyte.game.world.region.area.wilderness;

import com.zenyte.game.world.entity.player.Player;
import com.zenyte.game.world.region.RSPolygon;

public class EdgePvpArea extends WildernessArea {

    private static final RSPolygon EDGE_PVP_POLYS = new RSPolygon(new int[][]{
            {3456, 8128},
            {3647, 8128},
            {3647, 8319},
            {3456, 8319}
    });

    @Override
    protected RSPolygon[] polygons() {
        return new RSPolygon[]{EDGE_PVP_POLYS};
    }

    @Override
    public void leave(Player player, boolean logout) {
        super.leave(player, logout);
    }

    @Override
    public void enter(Player player) {
        super.enter(player);
    }

    @Override
    public String name() {
        return "Edgeville PvP Area";
    }
}
