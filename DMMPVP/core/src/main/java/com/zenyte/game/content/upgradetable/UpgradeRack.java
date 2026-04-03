package com.zenyte.game.content.upgradetable;

import com.zenyte.game.world.entity.player.Player;
import com.zenyte.game.world.object.ObjectAction;
import com.zenyte.game.world.object.ObjectId;
import com.zenyte.game.world.object.WorldObject;

import static com.zenyte.game.GameInterface.*;

public class UpgradeRack implements ObjectAction {

    @Override
    public void handleObjectAction(Player player, WorldObject object, String name, int optionId, String option) {
        UPGRADE_INTERFACE.open(player);
    }

    @Override
        public Object[] getObjects() {
            return new Object[]{ObjectId.WEAPON_RACK_33020};
        }
    }
