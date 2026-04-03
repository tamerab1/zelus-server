package com.zenyte.game.world.region.area.wilderness;

import com.zenyte.game.world.Position;
import com.zenyte.game.world.entity.Entity;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.game.world.entity.player.action.combat.PlayerCombat;
import com.zenyte.game.world.entity.player.action.combat.magic.CombatSpell;
import com.zenyte.game.world.region.RSPolygon;

public class EdgePvpSafeArea extends EdgePvpArea {

    private static final RSPolygon EDGE_BANK_POLYS = new RSPolygon(new int[][]{
            {3539, 8224},
            {3539, 8235},
            {3546, 8235},
            {3546, 8224},
    });

    @Override
    protected RSPolygon[] polygons() {
        return new RSPolygon[]{EDGE_BANK_POLYS};
    }

    @Override
    public boolean attack(Player player, Entity entity, PlayerCombat combat) {
        return false;
    }

    @Override
    public void enter(Player player) {
        player.getVarManager().sendBit(8121, 1);
    }

    @Override
    public void leave(Player player, boolean logout) {
        super.leave(player, logout);
    }

    @Override
    public boolean isSafe() {
        return super.isSafe();
    }

    @Override
    public boolean processCombat(final Player player, final Entity entity, final String style) {
        return false;
    }

    @Override
    public void onAttack(Player player, Entity entity, String style, CombatSpell spell, boolean splash) {
    }

    @Override
    public boolean isWildernessArea(Position position) {
        return false;
    }

    @Override
    public String name() {
        return "Edgeville PvP Bank";
    }
}
