package com.zenyte.game.content.lootkeys;

import com.zenyte.game.world.entity.npc.NPC;
import com.zenyte.game.world.entity.npc.actions.NPCPlugin;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.game.world.entity.player.dialogue.Dialogue;

/**
 * author: touring
 * date: 7/14/2025
 * <<rune-server.ee>>
 */
public class Refuge extends NPCPlugin {

    @Override
    public void handle() {
        bind("Talk-to", (player, npc) -> {
            LootkeySettings settings = player.getLootkeySettings();
            player.getDialogueManager().start(new Dialogue(player, npc) {
                @Override
                public void buildDialogue() {
                    if (settings == null) {
                        npc("Would you like to enable loot keys for 10.000 Blood Money?");
                        options("Enable loot keys for 10.000 Blood Money?",
                                "Yes, enable them.",
                                "No, thanks.")
                                .onOptionOne(() -> {
                                    int coins = player.getInventory().getAmountOf(13307);
                                    if (coins >= 10_000) {
                                        player.getInventory().deleteItem(13307, 10_000);
                                        player.setLootkeySettings(new LootkeySettings(true, false, false, 10_000, 0, 0));
                                        npc("All set. Loot keys are now enabled.");
                                    } else {
                                        npc("You don't have enough coins.");
                                    }
                                    finish();
                                })
                                .onOptionTwo(this::finish);
                    } else {
                        boolean enabled = settings.isEnabled();
                        npc("Loot keys are currently " + (enabled ? "enabled." : "disabled."));
                        options(enabled ? "Disable loot keys?" : "Enable loot keys?",
                                "Yes.",
                                "No, thanks.")
                                .onOptionOne(() -> {
                                    if (!enabled) {
                                        npc("Would you like to enable loot keys for free now?");
                                    }
                                    settings.setEnabled(!enabled);
                                    npc("Loot keys are now " + (settings.isEnabled() ? "enabled." : "disabled."));
                                    finish();
                                })
                                .onOptionTwo(this::finish);
                    }
                }
            });
        });
    }

    @Override
    public int[] getNPCs() {
        return new int[]{10417};
    }
}
