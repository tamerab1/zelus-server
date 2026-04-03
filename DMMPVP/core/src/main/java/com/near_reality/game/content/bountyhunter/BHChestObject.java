package com.near_reality.game.content.bountyhunter;

import com.zenyte.game.GameInterface;
import com.zenyte.game.task.WorldTasksManager;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.game.world.entity.player.dialogue.Dialogue;
import com.zenyte.game.world.object.ObjectAction;
import com.zenyte.game.world.object.WorldObject;
import com.zenyte.plugins.dialogue.CountDialogue;
import com.zenyte.plugins.dialogue.NameDialogue;

/**
 * Handles clicking the BH Chest world object (ID 56001).
 *
 * Option 1 → Open BH Store   (GameInterface.BOUNTY_HUNTER_STORE)
 * Option 2 → Place a Bounty  (multi-step input flow)
 */
public class BHChestObject implements ObjectAction {

    // Object ID of the BH Chest, matches CustomObjectId.BH_CRATE
    private static final int BH_CHEST_ID = 56001;

    @Override
    public void handleObjectAction(Player player, WorldObject object,
                                   String name, int optionId, String option) {
        openMainMenu(player);
    }

    private void openMainMenu(Player player) {
        player.getDialogueManager().start(new Dialogue(player) {
            @Override
            public void buildDialogue() {
                options("BH Chest",
                        "Open BH Store",
                        "Place a Bounty",
                        "View Bounties")
                        .onOptionOne(()   -> openBHStore(player))
                        .onOptionTwo(()   -> startBountyFlow(player))
                        .onOptionThree(() -> BountyManager.sendBountyList(player));
            }
        });
    }

    // ── Option 1: BH Store ────────────────────────────────────────────────────
    private void openBHStore(Player player) {
        player.getTemporaryAttributes().put("GlobalShopCategory", "Bounty Hunter Shop");
        GameInterface.GLOBAL_SHOP.open(player);
    }

    // ── Option 2: Place a Bounty ──────────────────────────────────────────────
    private void startBountyFlow(Player player) {
        // Delay by 1 tick so the dialogue close doesn't wipe interfaceInput
        WorldTasksManager.schedule(() -> player.sendInputName("Enter the player's name:", (NameDialogue) name -> {
            final String trimmedName = name.trim();
            if (trimmedName.isEmpty()) {
                player.sendMessage("Invalid name entered.");
                return;
            }
            // Step 2 — ask for the blood money amount
            player.sendInputInt("How much Blood Money do you want to offer?", (CountDialogue) amount -> {
                if (amount <= 0) {
                    player.sendMessage("The amount must be greater than zero.");
                    return;
                }
                // Step 3 — confirm before deducting
                player.getDialogueManager().start(new Dialogue(player) {
                    @Override
                    public void buildDialogue() {
                        plain("Place a bounty of <col=ff0000>" + amount
                                + " Blood Money</col> on <col=ff0000>"
                                + trimmedName + "</col>?");
                        options("Confirm bounty?",
                                "Yes, place the bounty.",
                                "No, cancel.")
                                .onOptionOne(() -> BountyManager.placeBounty(player, trimmedName, amount))
                                .onOptionTwo(() -> player.sendMessage("Bounty placement cancelled."));
                    }
                });
            });
        }), 1);
    }

    @Override
    public Object[] getObjects() {
        return new Object[]{ BH_CHEST_ID };
    }
}
