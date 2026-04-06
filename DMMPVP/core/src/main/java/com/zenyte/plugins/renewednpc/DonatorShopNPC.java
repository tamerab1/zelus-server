package com.zenyte.plugins.renewednpc;

import com.zenyte.game.util.Colour;
import com.zenyte.game.world.entity.npc.NpcId;
import com.zenyte.game.world.entity.npc.actions.NPCPlugin;
import com.zenyte.game.world.entity.player.dialogue.Dialogue;

/**
 * Handles the Donator Store for Jackie (16008) and Robin Hood (16003).
 *
 * Opens a category-selection dialogue so players can navigate between
 * Boosters, Boxes, and Other — each backed by its own independent shop
 * definition and opened through the standard shop interface.
 *
 * This is entirely separate from GlobalShopInterface / GLOBAL_SHOP.
 */
public class DonatorShopNPC extends NPCPlugin {

    @Override
    public void handle() {
        bind("Trade", (player, npc) -> {
            player.stopAll();
            player.faceEntity(npc);
            player.sendMessage("You have " + Colour.RED.wrap(player.getDonorPoints()) + " Donor Points.");
            player.getDialogueManager().start(new Dialogue(player, npc) {
                @Override
                public void buildDialogue() {
                    options("Donator Store — choose a category:",
                        new DialogueOption("Boosters", () -> player.openShop("Donator Boosters")),
                        new DialogueOption("Boxes",    () -> player.openShop("Donator Boxes")),
                        new DialogueOption("Other",    () -> player.openShop("Donator Other")),
                        new DialogueOption("Close", () -> player.getDialogueManager().finish())
                    );
                }
            });
        });
    }

    @Override
    public int[] getNPCs() {
        // Jackie (16008) and Robin Hood (16003) are the designated donator shop NPCs.
        return new int[] { NpcId.JACKIE, NpcId.ROBIN_HOOD };
    }
}
