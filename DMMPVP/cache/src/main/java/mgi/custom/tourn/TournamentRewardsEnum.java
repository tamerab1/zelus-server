package mgi.custom.tourn;

import mgi.types.config.enums.EnumDefinitions;

import java.util.HashMap;

public class TournamentRewardsEnum {


    public static void pack() {
        packItemEnum();
        packPriceEnum();
        packAmountEnum();
    }

    private static void packAmountEnum() {
        var itemEnum = new EnumDefinitions();
        itemEnum.setId(10054);
        itemEnum.setDefaultInt(-1);
        itemEnum.setKeyType("int");
        itemEnum.setValueType("int");

        var hashMap = new HashMap<Integer, Object>();
        for (var values : TournamentRewards.values()) {
            hashMap.put(values.getItemId(), values.getItemAmount());
        }

        itemEnum.setValues(hashMap);
        itemEnum.pack();
    }

    private static void packPriceEnum() {
        var itemEnum = new EnumDefinitions();
        itemEnum.setId(10055);
        itemEnum.setDefaultInt(-1);
        itemEnum.setKeyType("int");
        itemEnum.setValueType("int");

        var hashMap = new HashMap<Integer, Object>();
        for (var values : TournamentRewards.values()) {
            hashMap.put(values.getItemId(), values.getCost());
        }

        itemEnum.setValues(hashMap);
        itemEnum.pack();
    }


    private static void packItemEnum() {
        var itemEnum = new EnumDefinitions();
        itemEnum.setId(10053);
        itemEnum.setDefaultInt(-1);
        itemEnum.setKeyType("int");
        itemEnum.setValueType("int");

        var hashMap = new HashMap<Integer, Object>();
        for (var values : TournamentRewards.values()) {
            hashMap.put(values.ordinal(), values.getItemId());
        }

        itemEnum.setValues(hashMap);
        itemEnum.pack();
    }


}
