package com.zenyte.plugins.renewednpc;

import com.zenyte.game.world.entity.npc.NpcId;
import com.zenyte.game.world.entity.npc.actions.NPCPlugin;
import com.zenyte.game.world.entity.player.dialogue.Dialogue;
import com.zenyte.game.item.Item;

public class HalloweenVIR extends NPCPlugin {

    private static final int HALLOWEEN_TOKEN = 30116;
    private static final int HALLOWEEN_BOX = 32162;
    private static final int COST = 50_000;

    @Override
    public void handle() {
        bind("Talk-to", (player, npc) -> {
            player.getDialogueManager().start(new Dialogue(player, npc) {
                @Override
                public void buildDialogue() {
                    // Introductie / uitleg
                    npc("Greetings, wanderer! I have recently discovered a group of <col=FF0000>Demonic Gorillas</col> " +
                            "lurking on my island.");
                    npc("They are fierce, but if you manage to kill them, you will be rewarded with <col=FF6600>Halloween tokens</col>.");
                    npc("Bring those tokens back to me, and I can offer you something special...");

                    // Shop-gedeelte
                    npc("One of my prized rewards is a <col=6600FF>Halloween Box</col>. " +
                            "It may contain mysterious and scary treasures inside!");

                    options("Buy Halloween Box:",
                            "Yes, buy for 50,000 tokens.",
                            "No, maybe later.")
                            .onOptionOne(() -> buyBox(player));
                }
            });
        });
    }

    @Override
    public int[] getNPCs() {
        return new int[]{NpcId.GENERAL_VIR};
    }

    private void buyBox(com.zenyte.game.world.entity.player.Player player) {
        if (!player.getInventory().containsItem(HALLOWEEN_TOKEN, COST)) {
            player.sendMessage("You need " + COST + " Halloween tokens to buy a Halloween Box.");
            return;
        }

        player.getInventory().deleteItem(new Item(HALLOWEEN_TOKEN, COST));
        player.getInventory().addItem(HALLOWEEN_BOX, 1);

        player.sendMessage("You buy a Halloween Box for " + COST + " Halloween tokens.");
    }
}
