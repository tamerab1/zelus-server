package com.zenyte.plugins.item.Kharix;

import com.zenyte.game.world.entity.player.Action;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

/**
 * Handles combining Kharix Elixir doses.
 *
 * Rules:
 *   1+1 = 2/4
 *   1+2 = 3/4
 *   2+2 = 4/4
 *   1+3 = 4/4
 *   3+2 = 4/4 + leftover 1/4
 *   3+3 = 4/4 + leftover 2/4
 *
 * @author Zelus Dev
 */
public class CombineKharixElixir extends Action {

    private final int amount;
    private final int firstDose;
    private final int secondDose;
    private final int result;
    private final int leftover; // 0 = no leftover
    private int completed;

    public CombineKharixElixir(int amount, int firstDose, int secondDose, int result, int leftover) {
        this.amount = amount;
        this.firstDose = firstDose;
        this.secondDose = secondDose;
        this.result = result;
        this.leftover = leftover;
    }

    @Override
    public boolean start() {
        return check();
    }

    @Override
    public boolean process() {
        return check();
    }

    @Override
    public int processWithDelay() {
        // Delete both doses
        player.getInventory().deleteItem(firstDose, 1);
        player.getInventory().deleteItem(secondDose, 1);

        // Give result
        player.getInventory().addItem(result, 1);

        // Give leftover if any
        if (leftover != 0) {
            player.getInventory().addItem(leftover, 1);
        }

        player.sendMessage("You combine the Kharix Elixir doses.");
        completed++;
        return 1;
    }

    private boolean check() {
        if (completed >= amount) return false;
        if (firstDose == secondDose) {
            return player.getInventory().getAmountOf(firstDose) >= 2;
        }
        return player.carryingItem(firstDose) && player.carryingItem(secondDose);
    }
}