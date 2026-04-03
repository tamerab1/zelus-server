package com.zenyte.game.model.ui.testinterfaces;

import com.zenyte.game.GameInterface;
import com.zenyte.game.model.ui.Interface;
import com.zenyte.game.util.AccessMask;
import com.zenyte.game.util.Colour;
import com.zenyte.game.world.entity.player.MessageType;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.game.world.entity.player.dialogue.Dialogue;

public class LotteryInterface extends Interface {

    @Override
    protected void attach() {
        // Hier kan je child components mappen als je interacties nodig hebt
    }

    @Override
    protected void build() {
        // Geen interacties voorlopig
    }

    @Override
    public void open(Player player) {
        // Eerst gewoon het interface openen
        super.open(player);

        // Daarna direct jouw clientscript 12588 aanroepen
        // Param is meestal het interface-id (1038)
      //  player.getPacketDispatcher().sendClientScript(12588, getInterface().getId());
    }

    @Override
    public GameInterface getInterface() {
        // Hier koppel je het enum object
        return GameInterface.LOTTERY;
    }
}