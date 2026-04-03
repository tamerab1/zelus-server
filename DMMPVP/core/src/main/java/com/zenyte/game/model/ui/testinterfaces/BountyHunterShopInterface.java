package com.zenyte.game.model.ui.testinterfaces;

import com.zenyte.game.GameInterface;
import com.zenyte.game.item.Item;
import com.zenyte.game.model.ui.Interface;
import com.zenyte.game.task.WorldTasksManager;
import com.zenyte.game.util.AccessMask;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.game.world.entity.player.dialogue.Dialogue;
import com.zenyte.game.world.entity.player.MessageType;
import com.zenyte.game.util.Colour;

public class BountyHunterShopInterface extends Interface {

    @Override
    protected void attach() {
        put(56, "Buy Vesta Legs");
        put(57, "Buy Vesta Body");
        put(58, "Buy Statius Helm");

        put(70, "Bounty Hunter Points Value");
    }



    @Override
    protected void build() {
        bind("Buy Statius Helm", (player, slotId, itemId, option) -> {
            handlePurchase(player, "Statius helm", 22625, 400);
        });

        bind("Buy Vesta Body", (player, slotId, itemId, option) -> {
            handlePurchase(player, "Vesta's body", 22613, 600);
        });

        bind("Buy Vesta Legs", (player, slotId, itemId, option) -> {
            handlePurchase(player, "Vesta's legs", 22615, 550);
        });
    }


    @Override
    public void open(Player player) {
        int bhPoints = player.getBountyHunterPoints();
        int interfaceId = getInterface().getId();
        player.getInterfaceHandler().sendInterface(getInterface());

        // maak componenten klikbaar
        player.getPacketDispatcher().sendComponentSettings(interfaceId, 56, 0, 0, AccessMask.CLICK_OP1);
        player.getPacketDispatcher().sendComponentSettings(interfaceId, 57, 0, 0, AccessMask.CLICK_OP1);
        player.getPacketDispatcher().sendComponentSettings(interfaceId, 58, 0, 0, AccessMask.CLICK_OP1);

        // toon Armadyl godsword (itemId = 12802) op component 76
        player.getPacketDispatcher().sendItems(1939, 76, 0, new Item(11802, 1));


        // duidelijk chatbericht, altijd zichtbaar
        // Optie 1 (als overload bestaat):
        player.sendMessage("You currently have " + Colour.RED.wrap(bhPoints) + " Bounty Hunter points.", MessageType.UNFILTERABLE);
        player.sendDeveloperMessage("BH Shop opened, points = " + bhPoints);
        player.sendMessage("You currently have " + Colour.RED.wrap(bhPoints) + " Bounty Hunter points.", MessageType.UNFILTERABLE);

    }




    private void handlePurchase(Player player, String name, int itemId, int cost) {
        int bhPoints = player.getBountyHunterPoints();

        player.getDialogueManager().start(new Dialogue(player) {
            @Override
            public void buildDialogue() {
                plain("Are you sure you want to buy <col=FF0000>" + name +
                        "</col> for <col=FF0000>" + cost + "</col> Bounty Hunter Points?");
                options("Confirm purchase?",
                        "Yes, buy " + name + ".",
                        "No, cancel.")
                        .onOptionOne(() -> {
                            if (bhPoints >= cost) {
                                player.setBountyHunterPoints(bhPoints - cost);
                                player.getInventory().addItem(itemId, 1);
                                player.sendMessage("You have purchased a " + name + " for " + cost + " BH points.");
                                //updatePoints(player);
                            } else {
                                player.sendMessage("You do not have enough Bounty Hunter Points.");
                            }
                        })
                        .onOptionTwo(() -> player.sendMessage("You decide not to buy the " + name + "."));
            }
        });
    }

//    private void updatePoints(Player player) {
//        int bhPoints = player.getBountyHunterPoints();
//        int interfaceId = getInterface().getId();

    // TEST: battlepass level op compId 70 zetten i.p.v. bhPoints
//        int level = player.getBattlePassLevel();
//        player.getPacketDispatcher().sendComponentText(interfaceId, 70,
//                "<col=00ff00>TEST Level: " + level + "</col>");

    // Normaal: BH points
    // player.getPacketDispatcher().sendComponentText(interfaceId, 70,
    //        "<col=ffffff>" + bhPoints + "</col>");
//}


    @Override
    public GameInterface getInterface() {
        return GameInterface.SACRIFICEINTERFACE;
    }
}
