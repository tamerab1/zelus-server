package com.zenyte.game.model.ui.testinterfaces;

import com.zenyte.game.GameInterface;
import com.zenyte.game.model.ui.Interface;
import com.zenyte.game.world.entity.player.Player;

import java.util.List;

public class TeleInterface extends Interface {

    @Override
    protected void attach() {
        put(22, "Monster Teleports Category");
        put(26, "Dungeon Teleports Category");

        put(79, "Teleport slot 1 text");
        put(84, "Teleport slot 2 text");
        put(89, "Teleport slot 3 text");

        put(80, "Teleport slot 1 sprite");
        put(85, "Teleport slot 2 sprite");
        put(90, "Teleport slot 3 sprite");
    }

    @Override
    protected void build() {
        // Monsters
        bind("Monster Teleports Category", (player, slotId, itemId, option) -> {
            // texts
            // zet sprite 2001 op component 80 van interface 5108
            player.getPacketDispatcher().sendClientScript(7053, 5108, 80, 2001);

// zet sprite 2002 op component 85
            player.getPacketDispatcher().sendClientScript(7053, 5108, 85, 2002);

// zet sprite 2003 op component 90
            player.getPacketDispatcher().sendClientScript(7053, 5108, 90, 2003);

            player.getPacketDispatcher().sendComponentText(5108, 79, "Chickens");
            player.getPacketDispatcher().sendComponentText(5108, 84, "Cows");
            player.getPacketDispatcher().sendComponentText(5108, 89, "Yaks");

            // sprites (random IDs gekozen bv. 2001, 2002, 2003)
            player.getPacketDispatcher().sendComponentSprite(5108, 80, 2001);
            player.getPacketDispatcher().sendComponentSprite(5108, 85, 2002);
            player.getPacketDispatcher().sendComponentSprite(5108, 90, 2003);
        });

        // Dungeons
        bind("Dungeon Teleports Category", (player, slotId, itemId, option) -> {
            // texts
            player.getPacketDispatcher().sendComponentText(5108, 79, "Taverly Dungeon");
            player.getPacketDispatcher().sendComponentText(5108, 84, "Edgeville Dungeon");
            player.getPacketDispatcher().sendComponentText(5108, 89, "Brimhaven Dungeon");

            // sprites (random IDs gekozen bv. 2010, 2011, 2012)
            player.getPacketDispatcher().sendComponentSprite(5108, 80, 2010);
            player.getPacketDispatcher().sendComponentSprite(5108, 85, 2011);
            player.getPacketDispatcher().sendComponentSprite(5108, 90, 2012);
        });
    }


    // Een eenvoudige record voor teleport entries
    public record TeleportEntry(String name, int spriteId) {}

    /**
     * Rendert een category op de interface door tekst en sprite in de juiste componenten te plaatsen.
     */
    private void renderCategory(Player player, List<TeleportEntry> entries,
                                int interfaceId, int textStart, int spriteStart) {
        int textId = textStart;
        int spriteId = spriteStart;
        int index = 0;

        for (TeleportEntry entry : entries) {
            // tekst & sprite zetten
            player.getPacketDispatcher().sendComponentText(interfaceId, textId, entry.name());
            player.getPacketDispatcher().sendComponentSprite(interfaceId, spriteId, entry.spriteId());

            // binden op de *componentnaam* (die in attach() zit)
            int finalIndex = index;
            bind("Teleport slot " + (finalIndex + 1), (p, slot, item, opt) -> {
                p.sendMessage("Teleporting to " + entries.get(finalIndex).name());
                // teleport logic hier
            });

            // doorgaan naar volgende component
            textId += 5;
            spriteId += 5;
            index++;
        }
    }


    @Override
    public void open(Player player) {
        player.getInterfaceHandler().sendInterface(GameInterface.TELEINTERFACE);

        // Default categorie (Monster teleports) direct laden
        List<TeleportEntry> monsterTeleports = List.of(
                new TeleportEntry("Chickens", 200),
                new TeleportEntry("Cows", 201),
                new TeleportEntry("Yaks", 202)
        );
        renderCategory(player, monsterTeleports, 5108, 79, 80);
    }

    @Override
    public GameInterface getInterface() {
        return GameInterface.TELEINTERFACE;
    }
}
