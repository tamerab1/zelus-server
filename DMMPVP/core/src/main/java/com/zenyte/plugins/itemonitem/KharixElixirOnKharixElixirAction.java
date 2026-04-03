package com.zenyte.plugins.itemonitem;

import com.zenyte.plugins.item.Kharix.CombineKharixElixir;
import com.zenyte.game.item.Item;
import com.zenyte.game.model.item.ItemOnItemAction;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.plugins.dialogue.SkillDialogue;

/**
 * Handles combining Kharix Elixir doses together.
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
public class KharixElixirOnKharixElixirAction implements ItemOnItemAction {

    private static final int DOSE_1 = 22670;
    private static final int DOSE_2 = 22669;
    private static final int DOSE_3 = 22668;
    private static final int DOSE_4 = 22667;

    @Override
    public void handleItemOnItemAction(final Player player, final Item from, final Item to, final int fromSlot, final int toSlot) {
final int fromId = from.getId();
        final int toId = to.getId();

        // 1+1 = 2/4
        if (bothMatch(fromId, toId, DOSE_1, DOSE_1)) {
            if (player.getInventory().getAmountOf(DOSE_1) < 2) {
                player.sendMessage("You need at least 2 doses to combine.");
                return;
            }
            player.getDialogueManager().start(new KharixCombineDialogue(player, DOSE_1, DOSE_1, DOSE_2, 0));
            return;
        }

        // 1+2 = 3/4
        if (bothMatch(fromId, toId, DOSE_1, DOSE_2)) {
            player.getDialogueManager().start(new KharixCombineDialogue(player, DOSE_1, DOSE_2, DOSE_3, 0));
            return;
        }

        // 2+2 = 4/4
        if (bothMatch(fromId, toId, DOSE_2, DOSE_2)) {
            if (player.getInventory().getAmountOf(DOSE_2) < 2) {
                player.sendMessage("You need at least 2 doses to combine.");
                return;
            }
            player.getDialogueManager().start(new KharixCombineDialogue(player, DOSE_2, DOSE_2, DOSE_4, 0));
            return;
        }

        // 1+3 = 4/4
        if (bothMatch(fromId, toId, DOSE_1, DOSE_3)) {
            player.getDialogueManager().start(new KharixCombineDialogue(player, DOSE_1, DOSE_3, DOSE_4, 0));
            return;
        }

        // 3+2 = 4/4 + leftover 1/4
        if (bothMatch(fromId, toId, DOSE_3, DOSE_2)) {
            player.getDialogueManager().start(new KharixCombineDialogue(player, DOSE_3, DOSE_2, DOSE_4, DOSE_1));
            return;
        }

        // 3+3 = 4/4 + leftover 2/4
        if (bothMatch(fromId, toId, DOSE_3, DOSE_3)) {
            if (player.getInventory().getAmountOf(DOSE_3) < 2) {
                player.sendMessage("You need at least 2 doses to combine.");
                return;
            }
            player.getDialogueManager().start(new KharixCombineDialogue(player, DOSE_3, DOSE_3, DOSE_4, DOSE_2));
            return;
        }

        player.sendMessage("You can't combine those doses together.");
    }

    private boolean bothMatch(int a, int b, int x, int y) {
        return (a == x && b == y) || (a == y && b == x);
    }

    @Override
    public ItemPair[] getMatchingPairs() {
        return new ItemPair[] {
                ItemPair.of(DOSE_1, DOSE_1),
                ItemPair.of(DOSE_1, DOSE_2),
                ItemPair.of(DOSE_2, DOSE_2),
                ItemPair.of(DOSE_1, DOSE_3),
                ItemPair.of(DOSE_3, DOSE_2),
                ItemPair.of(DOSE_3, DOSE_3),
        };
    }

    @Override
    public int[] getItems() {
        return null;
    }

    private static class KharixCombineDialogue extends SkillDialogue {
        private final int firstDose;
        private final int secondDose;
        private final int result;
        private final int leftover;

        public KharixCombineDialogue(Player player, int firstDose, int secondDose, int result, int leftover) {
            super(player, "How many would you like to make?", new Item(result));
            this.firstDose = firstDose;
            this.secondDose = secondDose;
            this.result = result;
            this.leftover = leftover;
        }

        @Override
        public void run(int slotId, int amount) {
            player.getActionManager().setAction(new CombineKharixElixir(amount, firstDose, secondDose, result, leftover));
        }
    }
}