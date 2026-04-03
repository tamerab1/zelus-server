package com.zenyte.game.content.portal;

import com.zenyte.game.world.entity.Location;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.game.world.object.ObjectAction;
import com.zenyte.game.world.object.WorldObject;

public class PortalAction implements ObjectAction {

    private static final int PORTAL_ID = 10251;

    private static final Location HOME_PORTAL = new Location(3099, 3497, 0);
    private static final Location WILDY_PORTAL = new Location(3747, 3969, 0);

    @Override
    public void handleObjectAction(Player player, WorldObject object, String name, int optionId, String option) {
        if (!option.equalsIgnoreCase("Enter")) {
            return; // Alleen reageren op "Enter"
        }

        int x = object.getX();
        int y = object.getY();
        int z = object.getPlane();

        if (x == 3098 && y == 3496 && z == 0) {
            player.useStairs(-1, WILDY_PORTAL, 1, 2);
        } else if (x == 3748 && y == 3968 && z == 0) {
            player.useStairs(-1, HOME_PORTAL, 1, 2);
        } else {
            player.sendMessage("This portal doesn't seem to lead anywhere...");
        }
    }


    @Override
    public Object[] getObjects() {
        return new Object[]{PORTAL_ID};
    }
}
