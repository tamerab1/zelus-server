package com.zenyte.game.world.region.area;

import com.google.common.eventbus.Subscribe;
import com.zenyte.game.content.skills.construction.CombatDummyNPC;
import com.zenyte.game.world.World;
import com.zenyte.game.world.entity.Location;
import com.zenyte.game.world.entity.npc.NpcId;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.game.world.entity.player.SkillConstants;
import com.zenyte.game.world.object.ObjectId;
import com.zenyte.game.world.object.WorldObject;
import com.zenyte.game.world.region.PolygonRegionArea;
import com.zenyte.game.world.region.RSPolygon;
import com.zenyte.game.world.region.area.plugins.DeathPlugin;
import com.zenyte.game.world.region.area.plugins.RandomEventRestrictionPlugin;
import com.zenyte.plugins.events.ServerLaunchEvent;

/**
 * @author Kris | 02/05/2019 23:00
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
public class ClanWarsFFAArea extends PolygonRegionArea implements RandomEventRestrictionPlugin, DeathPlugin {

    @Subscribe
    public static void onServerLaunched(ServerLaunchEvent event) {
        World.spawnObject(new WorldObject(ObjectId.BANK_CHEST, 10, 0, new Location(3326, 4753)));
        World.spawnObject(new WorldObject(ObjectId.BANK_CHEST, 10, 0, new Location(3090, 3475)));
        World.spawnObject(new WorldObject(ObjectId.BANK_CHEST, 10, 0, new Location(3084, 3475)));
        World.spawnObject(new WorldObject(ObjectId.BANK_CHEST, 10, 0, new Location(3078, 3475)));
        World.spawnObject(new WorldObject(50081, 10, 0, new Location( 3083, 3511))); //pool
        World.spawnObject(new WorldObject(50081, 10, 0, new Location( 3104, 3514))); //pool
//        World.spawnObject(new WorldObject(50081, 10, 0, new Location( 3088, 3473))); //pool
//        World.spawnObject(new WorldObject(50081, 10, 0, new Location( 3079, 3473))); //pool
        World.spawnObject(new WorldObject(43486, 10, 2, new Location( 3093, 3513))); //Lootkey
        World.spawnObject(new WorldObject(41724, 10, 2, new Location( 3073, 3505))); //clan hall portal
        World.spawnObject(new WorldObject(24911, 10, 3, new Location( 3085, 3508))); //altar spellbook
        World.spawnObject(new WorldObject(50082, 10, 1, new Location( 3084, 3504))); //teleporter
        World.spawnObject(new WorldObject(42834, 10, 0, new Location( 3099, 3513))); //bank chest
        World.spawnObject(new WorldObject(55070, 10, 0, new Location( 3093, 3110))); //registerportal
        World.spawnObject(new WorldObject(50295, 10, 0, new Location( 3090, 3467))); //registerportal
        World.spawnObject(new WorldObject(10251, 10, 1, new Location( 3748, 3968)));
        World.spawnObject(new WorldObject(10251, 10, 0, new Location( 3098, 3496)));
        World.spawnObject(new WorldObject(50092, 10, 0, new Location( 3080, 3504))); //Well of Goodwill
        //World.spawnObject(new WorldObject(31617, 10, 0, new Location( 3100, 3499)));
        //World.spawnObject(new WorldObject(31617, 10, 0, new Location( 3100, 3498)));
        //world.spawnObject(new WorldObject(50084, 10, 0, new Location( 3101, 3501)));
        //world.spawnObject(new WorldObject(50085, 10, 0, new Location( 3101, 3495)));
        //world.spawnObject(new WorldObject(50087, 10, 0, new Location( 3106, 3495)));
        //world.spawnObject(new WorldObject(50089, 10, 0, new Location( 3113, 3498)));
        //world.spawnObject(new WorldObject(50061, 22, 1, new Location( 3106, 3498)));
        //world.spawnObject(new WorldObject(50061, 22, 3, new Location( 3109, 3498)));
       //world.spawnObject(new WorldObject(55071, 0, 0, new Location( 3100, 3499)));
        //world.spawnObject(new WorldObject(55071, 0, 0, new Location( 3100, 3498)));
        //world.spawnObject(new WorldObject(35003, 10, 1, new Location( 3097, 3480)));
        //world.spawnObject(new WorldObject(3553, 22, 3, new Location( 3107, 3498)));
        //world.spawnObject(new WorldObject(3553, 22, 3, new Location( 3108, 3498)));
        //world.spawnObject(new WorldObject(50090, 10, 0, new Location( 3111, 3501)));
        //world.spawnObject(new WorldObject(50093, 10, 0, new Location( 3105, 3503)));
        //world.spawnObject(new WorldObject(50094, 10, 0, new Location( 3104, 3494)));
        //world.spawnObject(new WorldObject(50095, 10, 0, new Location( 3108, 3501)));
        //world.spawnObject(new WorldObject(50096, 10, 0, new Location( 3112, 3495)));
        //world.spawnObject(new WorldObject(50092, 10, 0, new Location( 3093, 3509)));
        //world.spawnObject(new WorldObject(50055, 10, 0, new Location( 3101, 3504)));
        //world.spawnObject(new WorldObject(50056, 10, 0, new Location( 3104, 3504)));
        //world.spawnObject(new WorldObject(50057, 10, 0, new Location( 3107, 3504)));
        //world.spawnObject(new WorldObject(50058, 10, 0, new Location( 3110, 3504)));
        //world.spawnObject(new WorldObject(50059, 10, 0, new Location( 3108, 3494)));
        //world.spawnObject(new WorldObject(55000, 10, 0, new Location( 3102, 3485)));
        //world.spawnObject(new WorldObject(41728, 10, 0, new Location( 3078, 3495)));
        //world.spawnObject(new WorldObject(24911, 10, 0, new Location( 3113, 3513)));
        //world.spawnObject(new WorldObject(60000, 10, 2, new Location( 3105, 3508)));
        //world.spawnObject(new WorldObject(38426, 10, 0, new Location( 3074, 3480)));
        //world.spawnObject(new WorldObject(36582, 10, 1, new Location( 3099, 3502)));
        //world.spawnObject(new WorldObject(26645, 10, 0, new Location( 3098, 3486)));
        //world.spawnObject(new WorldObject(6802, 10, 0, new Location( 3075, 3514)));
        //world.spawnObject(new WorldObject(34662, 10, 0, new Location( 3084, 3507)));
        //world.spawnObject(new WorldObject(40448, 10, 0, new Location( 3089, 3488)));
        //World.spawnObject(new WorldObject(33020, 10, 3, new Location( 3085, 3509)));//Upgrade rack

    }

    @Override
    public RSPolygon[] polygons() {
        return new RSPolygon[] {
                new RSPolygon(new int[][]{
                        { 3264, 4864 },
                        { 3264, 4736 },
                        { 3392, 4736 },
                        { 3392, 4864 }
                })
        };
    }

    @Override
    public void enter(final Player player) {

    }

    @Override
    public void leave(final Player player, final boolean logout) {
        for (int i = 0; i < SkillConstants.SKILLS.length; i++) {
            if (player.getSkills().getLevel(i) < player.getSkills().getLevelForXp(i)) {
                player.getSkills().setLevel(i, player.getSkills().getLevelForXp(i));
            }
        }
        player.blockIncomingHits();
        player.getCombatDefinitions().setSpecialEnergy(100);
        player.getVariables().setRunEnergy(100);
        player.getToxins().reset();
        player.getVariables().resetScheduled();
        player.getPrayerManager().deactivateActivePrayers();
        player.resetFreeze();
    }

    @Override
    public String name() {
        return "Clan Wars: FFA";
    }

    @Override
    public boolean isSafe() {
        return true;
    }

    @Override
    public String getDeathInformation() {
        return "Deaths within the free-for-all zone are always safe.";
    }

    @Override
    public Location getRespawnLocation() {
        return new Location(3327, 4753, 0);
    }
}