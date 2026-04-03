//package com.zenyte.game.world.region.area.wilderness;
//
//import com.near_reality.game.world.entity.player.PlayerAttributesKt;
//import com.zenyte.game.content.skills.prayer.Prayer;
//import com.zenyte.game.model.ui.testinterfaces.advancedsettings.SettingVariables;
//import com.zenyte.game.world.Position;
//import com.zenyte.game.world.entity.Entity;
//import com.zenyte.game.world.entity.Location;
//import com.zenyte.game.world.entity.player.Player;
//import com.zenyte.game.world.entity.player.action.combat.PlayerCombat;
//import com.zenyte.game.world.entity.player.action.combat.magic.CombatSpell;
//import com.zenyte.game.world.entity.player.variables.TickVariable;
//import com.zenyte.game.world.region.RSPolygon;
//import com.zenyte.game.world.region.area.EdgevilleArea;
//import com.zenyte.game.world.region.area.plugins.*;
//import org.jetbrains.annotations.Nullable;
//
//public class EdgevilleHighRiskArea extends EdgevilleArea implements
//        DeathPlugin, EntityAttackPlugin, PlayerCombatPlugin,
//        RandomEventRestrictionPlugin, LootBroadcastPlugin {
//
////    private static final RSPolygon HIGH_RISK_POLYS = new RSPolygon(new int[][]{
////            {3073, 3463},
////            {3073, 3472},
////            {3096, 3472},
////            {3096, 3463}
////    });
//
//    @Override
//    public RSPolygon[] polygons() {
//        return new RSPolygon[]{HIGH_RISK_POLYS};
//    }
//
//    @Override
//    public void enter(Player player) {
//        PlayerAttributesKt.setBlackSkulled(player,true);
//        player.getVariables().setSkull(true);
//        player.getPrayerManager().deactivatePrayer(Prayer.PROTECT_ITEM);
//
//        WildernessAreaUtils.enter(player);
//    }
//
//    @Override
//    public void leave(Player player, boolean logout) {
//        PlayerAttributesKt.setBlackSkulled(player,false);
//        player.getVariables().setSkull(false);
//        WildernessAreaUtils.leave(player, logout);
//    }
//
//    @Override
//    public boolean sendDeath(Player player, Entity source) {
//        return WildernessAreaUtils.sendDeath(player, source);
//    }
//
//    @Override
//    public String name() {
//        return "Edgeville High Risk Area";
//    }
//
//    @Override
//    public boolean isSafe() {
//        return false;
//    }
//
//    @Override
//    public String getDeathInformation() {
//        return null;
//    }
//
//    @Override
//    public Location getRespawnLocation() {
//        return null;
//    }
//
//    @Override
//    public boolean attack(Player player, Entity entity, PlayerCombat combat) {
//        return WildernessAreaUtils.attack(player, entity, combat);
//    }
//
//    @Override
//    public boolean processCombat(Player player, Entity entity, String style) {
//        return true;
//    }
//
//    @Override
//    public void onAttack(final Player player, final Entity entity, final String style,
//                         final CombatSpell spell, final boolean splash) {
//        WildernessAreaUtils.onAttack(player, entity, style, spell, splash);
//    }
//}
