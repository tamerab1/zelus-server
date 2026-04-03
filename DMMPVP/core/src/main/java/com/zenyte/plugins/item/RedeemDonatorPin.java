package com.zenyte.plugins.item;

import com.zenyte.game.content.DonatorPin;
import com.zenyte.game.item.Item;
import com.zenyte.game.model.item.pluginextensions.ItemPlugin;
import com.zenyte.game.util.Colour;
import com.zenyte.game.world.entity.player.container.RequestResult;

import java.util.ArrayList;
import java.util.List;

public class RedeemDonatorPin extends ItemPlugin {

    @Override
    public void handle() {
        bind("Redeem", (player, item, slotId) -> {
            DonatorPin pin = DonatorPin.forId(item.getId());
            if (pin == null) {
                player.sendMessage("This is not a valid donator pin.");
                return;
            }
            if (player.getInventory().deleteItem(new Item(pin.getItemId())).getResult() != RequestResult.SUCCESS) {
                player.sendMessage("Failed to remove the Donator Pin from your inventory.");
                return;
            }
            player.addDonorPoints(pin.getCredits()); // Instead of setDonorPoints, we add the points.
            player.addTotalSpent(pin.getAmount());
            player.sendMessage(Colour.RS_GREEN.wrap("You have successfully redeemed your Donator Pin!"));
            player.sendMessage(Colour.RS_GREEN.wrap(pin.getCredits() + " Donor Points have been added to your account."));
            player.sendMessage(Colour.RS_GREEN.wrap("You now have " + player.getDonorPoints() + " Donor Points."));
            player.sendMessage(Colour.TURQOISE.wrap("Your total donated amount is now: $" + player.getTotalSpent()));
        });
    }

    /**
     * Get all item IDs for valid Donator Pins.
     *
     * @return An array of item IDs for donator pins.
     */
    @Override
    public int[] getItems() {
        List<Integer> donatorPinIds = new ArrayList<>();
        for (DonatorPin pin : DonatorPin.values()) {
            donatorPinIds.add(pin.getItemId());
        }
        return donatorPinIds.stream().mapToInt(Integer::intValue).toArray();
    }
}