package mgi.custom.bh;

import com.zenyte.game.ui.testinterfaces.BountyHunterRewardType;
import mgi.types.config.enums.EnumDefinitions;

import java.util.HashMap;

public class BountyHunterRewardsEnum {

    public static void pack() {
        packItemEnum();
        packCostEnum();
        packAmountEnum();
    }

    private static void packItemEnum() {
        EnumDefinitions def = new EnumDefinitions();
        def.setId(5454);
        def.setDefaultInt(-1);
        def.setKeyType("int");
        def.setValueType("obj"); // waardeType = obj

        HashMap<Integer, Object> values = new HashMap<>();
        int slot = 0;
        for (BountyHunterRewardType reward : BountyHunterRewardType.values()) {
            // Gebruik de ID als Integer, geen string!
            values.put(slot++, reward.getId());
        }

        def.setValues(values);
        def.pack();
    }





    private static void packCostEnum() {
        EnumDefinitions def = new EnumDefinitions();
        def.setId(5455); // cost per itemId
        def.setDefaultInt(-1);
        def.setKeyType("int");
        def.setValueType("int");

        HashMap<Integer, Object> values = new HashMap<>();
        for (BountyHunterRewardType reward : BountyHunterRewardType.values()) {
            values.put(reward.getId(), reward.getCost()); // itemId → cost
        }

        def.setValues(values);
        def.pack();
    }

    private static void packAmountEnum() {
        EnumDefinitions def = new EnumDefinitions();
        def.setId(5456); // amount per itemId
        def.setDefaultInt(-1);
        def.setKeyType("int");
        def.setValueType("int");

        HashMap<Integer, Object> values = new HashMap<>();
        for (BountyHunterRewardType reward : BountyHunterRewardType.values()) {
            values.put(reward.getId(), 1); // itemId → standaard 1 (aantal per aankoop)
        }

        def.setValues(values);
        def.pack();
    }
}
