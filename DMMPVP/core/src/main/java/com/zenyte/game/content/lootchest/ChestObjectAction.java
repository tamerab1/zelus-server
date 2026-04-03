package com.zenyte.game.content.lootchest;

import com.zenyte.game.item.Item;
import com.zenyte.game.world.World;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.game.world.entity.player.variables.TickVariable;
import com.zenyte.game.world.object.ObjectAction;
import com.zenyte.game.world.object.WorldObject;

import java.util.List;


public class ChestObjectAction implements ObjectAction {

    @Override
    public void handleObjectAction(Player player, WorldObject object, String name, int optionId, String option) {
        if (object.getId() == 29069 && option.equalsIgnoreCase("open")) {
            World.getPlayers().forEach(p -> p.getPacketDispatcher().resetHintArrow());
            player.getVariables().schedule(300, TickVariable.TELEBLOCK);
            player.sendMessage("A mysterious force prevents you from teleporting for 3 minutes.");
            World.removeObject(object);

            List<Item> loot = ChestLootTable.generateLoot();
            int bloodMoney = 0;

            for (Item item : loot) {
                player.getInventory().addItem(item);
                if (item.getId() == 13307) { // Blood money ID
                    bloodMoney += item.getAmount();
                }
            }

            if (bloodMoney > 0) {
                player.sendMessage("You loot the mysterious chest and receive " + bloodMoney + " blood money.");
            } else {
                player.sendMessage("You loot the mysterious chest...");
            }
        }
    }


    @Override
    public Object[] getObjects() {
        return new Object[] { 29069 };
    }
}
