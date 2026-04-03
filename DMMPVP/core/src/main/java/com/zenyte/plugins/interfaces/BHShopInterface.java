package com.zenyte.plugins.interfaces;

import com.zenyte.game.GameInterface;
import com.zenyte.game.model.ui.Interface;
import com.zenyte.game.world.entity.player.Player;

public class BHShopInterface extends Interface {

    @Override
    protected void attach() {
        // Je kunt hier extra knoppen/labels mappen zoals in BattlePassInterface,
        // maar voor nu alleen component 36 voor de item-naam.
        put(36, "Item Name Display");
    }

    @Override
    protected void build() {
        // Als je bv. ooit een "Buy" knop toevoegt, kun je die hier binden.
        // Voor nu alleen de tekst laten zien, dus niets om te binden.
    }

    @Override
    public void open(Player player) {
        int interfaceId = getInterface().getId();

        // Open de shop-interface
        player.getInterfaceHandler().sendInterface(getInterface());

        // Zet de naam van het item in component 36
        player.getPacketDispatcher().sendComponentText(interfaceId, 36, "Statius's full helm");
    }

    @Override
    public GameInterface getInterface() {
        return GameInterface.BHSHOP; // Voeg dit toe aan je GameInterface enum
    }
}
