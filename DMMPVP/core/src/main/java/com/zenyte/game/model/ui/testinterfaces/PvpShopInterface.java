//package com.zenyte.game.model.ui.testinterfaces;
//
//import com.zenyte.game.GameInterface;
//import com.zenyte.game.model.ui.Interface;
//import com.zenyte.game.world.entity.player.Player;
//
//public class PvpShopInterface extends Interface {
//
//    @Override
//    protected void attach() {
//        put(20, "PvP Shop Item Slot"); // Component waar het item komt
//    }
//
//    @Override
//    protected void build() {
//        bind("PvP Shop Item Slot", (player, slotId, itemId, option) -> {
//            // Optioneel gedrag als speler op het item klikt
//            player.sendMessage("Je hebt op het PvP-item geklikt.");
//        });
//    }
//
//    @Override
//    public void open(Player player) {
//        player.getInterfaceHandler().sendInterface(getInterface());
//        player.getPacketDispatcher().sendClientScript(31502, 954, 20);
//
//
//
//        // Plaats item ID 11802 (Armadyl Godsword) op component 20
//       // player.getPacketDispatcher().sendItemOnInterface(954, 20, 11802, 1);
//
//        // Alternatief met clientscript indien nodig voor inventory-type component:
//        // player.getPacketDispatcher().sendClientScript(149, 954, 20, 11802, 1);
//    }
//
//    @Override
//    public GameInterface getInterface() {
//        return GameInterface.BHREWARDS; // Voeg deze entry toe in je GameInterface enum
//    }
//}
