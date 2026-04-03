package com.zenyte.game.content.CTF;

import com.zenyte.game.item.Item;
import com.zenyte.game.world.World;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.game.world.entity.player.variables.TickVariable;
import com.zenyte.game.world.object.ObjectAction;
import com.zenyte.game.world.object.WorldObject;

public class FlagObjectAction implements ObjectAction {

    private static final int FLAG_OBJECT_ID = 15998;
    private static final int FLAG_ITEM_ID = 8970;

    @Override
    public void handleObjectAction(Player player, WorldObject object, String name, int optionId, String option) {
        if (object.getId() == FLAG_OBJECT_ID && option.equalsIgnoreCase("take")) {
            World.getPlayers().forEach(p -> p.getPacketDispatcher().resetHintArrow());
            player.getVariables().schedule(300, TickVariable.TELEBLOCK);
            player.sendMessage("You have taken the flag! You cannot teleport for 3 minutes.");
            World.removeObject(object);

            // Geef vlag als item
            player.getInventory().addItem(new Item(FLAG_ITEM_ID));
        }
    }

    @Override
    public Object[] getObjects() {
        return new Object[] { FLAG_OBJECT_ID };
    }
}
