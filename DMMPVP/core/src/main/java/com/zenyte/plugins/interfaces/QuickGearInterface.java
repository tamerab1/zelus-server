package com.zenyte.plugins.interfaces;

import com.zenyte.game.GameInterface;
import com.zenyte.game.item.Item;
import com.zenyte.game.model.ui.Interface;
import com.zenyte.game.util.AccessMask;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.game.world.entity.player.container.Container;
import com.zenyte.game.world.entity.player.container.ContainerPolicy;
import com.zenyte.game.world.entity.player.container.impl.ContainerType;

import java.util.Optional;

public class QuickGearInterface extends Interface {


    private static final int LOAD_BUTTON_COMPONENT = 71; // ← i.p.v. 72
    private static final int HELM_COMPONENT = 34; // Slot voor rune helm
    private static final int INVENTORY_COMPONENT = 55; // Container voor shark

    @Override
    protected void attach() {
        put(LOAD_BUTTON_COMPONENT, "Load Gear");

        bind("Load Gear", player -> loadGear(player));

    }

    @Override
    protected void build() {
        put(LOAD_BUTTON_COMPONENT, "Load Gear");

        bind("Load Gear", player -> loadGear(player));

    }


    private void loadGear(final Player player) {
        // Zet rune helm (ID 10286) in component 34
        player.getPacketDispatcher().sendComponentItem(getInterface(), HELM_COMPONENT, 10286, 1);

        // Maak een nieuwe container voor QUICK_GEAR
        Container container = new Container(
                ContainerPolicy.NORMAL,
                ContainerType.QUICK_GEAR,
                Optional.of(player)
        );

        // Zet 1 shark (ID 383) op slot 0 van de container
        container.set(0, new Item(383, 1));

        // Verstuur containerupdate naar de client
        player.getPacketDispatcher().sendUpdateItemContainer(container);


    }

    @Override
    public void open(Player player) {
        player.getInterfaceHandler().sendInterface(this);

        // 🔑 Zorg dat component 71 (of jouw juiste button component) klikbaar is
        player.getPacketDispatcher().sendComponentSettings(getInterface(), 72, 0, 0, AccessMask.CLICK_OP1);
    }


    @Override
    public GameInterface getInterface() {
        return GameInterface.QUICK_GEAR_INTERFACE;
    }
}
