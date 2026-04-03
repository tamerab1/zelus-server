package com.zenyte.game.world.entity.player.upgrades;

import com.zenyte.game.item.Item;
enum UpgradableItems {


        // Weapon
        W0(UpgradeCategory.WEAPON, 12788, 100, new Item[]{new Item(861,1), new Item(995, 5000000)}),
        W1(UpgradeCategory.WEAPON, 27853, 100, new Item[]{ new Item(11235,1), new Item(13307, 10000)}),
        W2(UpgradeCategory.WEAPON, 22547, 100, new Item[]{ new Item(22550,1), new Item(21820, 2500)}),
        W3(UpgradeCategory.WEAPON, 25867, 100, new Item[]{ new Item(25865,1), new Item(13307, 15000), new Item(23962, 1)}),
        W4(UpgradeCategory.WEAPON, 28540, 100, new Item[]{ new Item(32387,1), new Item(13307, 25000)}),
        W5(UpgradeCategory.WEAPON, 28039, 100, new Item[]{ new Item(13652,1), new Item(13307, 25000)}),
        W6(UpgradeCategory.WEAPON, 25734, 100, new Item[]{ new Item(22324,1), new Item(25742, 1), new Item(13307, 35000)}),
        W7(UpgradeCategory.WEAPON, 24551, 100, new Item[]{ new Item(23995,1), new Item(13307, 15000), new Item(23962, 1)}),
        W8(UpgradeCategory.WEAPON, 27246, 100, new Item[]{ new Item(26219,1), new Item(27248, 1), new Item(13307, 25000)}),
        W9(UpgradeCategory.WEAPON, 25736, 100, new Item[]{ new Item(13307,50000), new Item(25742, 1), new Item(22325, 1)}),
        W10(UpgradeCategory.WEAPON, 25739, 100, new Item[]{ new Item(13307,50000), new Item(25744, 1), new Item(22325, 1)}),
        W11(UpgradeCategory.WEAPON, 27184, 100, new Item[]{ new Item(20368,1), new Item(19677, 3), new Item(13307, 20000)}),



        // Armour
        A1(UpgradeCategory.ARMOUR, 24271, 100, new Item[]{new Item(10828,1), new Item(11864, 1)}),
        A2(UpgradeCategory.ARMOUR, 28254, 100, new Item[]{new Item(26382,1), new Item(25744,1), new Item(13307, 10000)}),
        A3(UpgradeCategory.ARMOUR, 27235, 100, new Item[]{new Item(27226,1), new Item(27372,1), new Item(13307, 10000)}),
        A4(UpgradeCategory.ARMOUR, 24664, 100, new Item[]{new Item(24670,1), new Item(21018,1), new Item(13307, 10000)}),
        A5(UpgradeCategory.ARMOUR, 27705, 100, new Item[]{new Item(23971,1), new Item(13307,15000)}),
        A7(UpgradeCategory.ARMOUR, 26718, 100, new Item[]{new Item(11832,1), new Item(13307,10000)}),
        A8(UpgradeCategory.ARMOUR, 28256, 100, new Item[]{new Item(26384,1), new Item(25744,1), new Item(13307, 10000)}),
        A9(UpgradeCategory.ARMOUR, 27238, 100, new Item[]{new Item(27229,1), new Item(27372,1), new Item(13307, 10000)}),
        A11(UpgradeCategory.ARMOUR, 24666, 100, new Item[]{new Item(24670,1), new Item(21021,1), new Item(13307, 10000)}),
        A10(UpgradeCategory.ARMOUR, 27697, 100, new Item[]{new Item(23975,1), new Item(13307,10000)}),
        A12(UpgradeCategory.ARMOUR, 26719, 100, new Item[]{new Item(11834,1), new Item(13307,10000)}),
        A13(UpgradeCategory.ARMOUR, 28258, 100, new Item[]{new Item(26386,1), new Item(25744,1), new Item(13307, 10000)}),
        A14(UpgradeCategory.ARMOUR, 27241, 100, new Item[]{new Item(27232,1), new Item(27372,1), new Item(13307, 10000)}),
        A15(UpgradeCategory.ARMOUR, 24668, 100, new Item[]{new Item(24670,1), new Item(21024,1), new Item(13307, 10000)}),
        A16(UpgradeCategory.ARMOUR, 27701, 100, new Item[]{new Item(23979,1), new Item(13307,10000)}),




        // Jewellery
        J1(UpgradeCategory.JEWELLERY, 19710, 100, new Item[]{new Item(19550), new Item(995, 15000000),}),

        J2(UpgradeCategory.JEWELLERY, 23444, 75, new Item[]{new Item(19544, 2), new Item(995, 25000000)}),

        J3(UpgradeCategory.JEWELLERY, 22249, 75, new Item[]{new Item(19547, 2), new Item(995, 5000000)}),

        J4(UpgradeCategory.JEWELLERY, 12785, 50, new Item[]{new Item(2572), new Item(995, 5000000)}),

        J5(UpgradeCategory.JEWELLERY, 12017, 100, new Item[]{new Item(4081), new Item(995, 5000000)}),

        J6(UpgradeCategory.JEWELLERY, 12018, 100, new Item[]{new Item(12017), new Item(995, 10000000)}),

        J7(UpgradeCategory.JEWELLERY, 13202, 100, new Item[]{new Item(12601), new Item(995, 5000000)}),

        J8(UpgradeCategory.JEWELLERY, 12692, 100, new Item[]{new Item(12605), new Item(995, 5000000)}),

        J9(UpgradeCategory.JEWELLERY, 12691, 100, new Item[]{new Item(12603), new Item(995, 5000000)}),

        J10(UpgradeCategory.JEWELLERY, 11773, 100, new Item[]{new Item(6737), new Item(995, 5000000)}),

        J11(UpgradeCategory.JEWELLERY, 11771, 100, new Item[]{new Item(6733), new Item(995, 5000000)}),

        J12(UpgradeCategory.JEWELLERY, 11770, 100, new Item[]{new Item(6731), new Item(995, 5000000)}),

        J13(UpgradeCategory.JEWELLERY, 11772, 100, new Item[]{new Item(6735), new Item(995, 5000000)}),
        J14(UpgradeCategory.JEWELLERY, 29804, 100, new Item[]{new Item(29801), new Item(25742, 1)}),


        // Pets



        // OTHER

        M3(UpgradeCategory.MISC, 25185, 75, new Item[]{new Item(25191), new Item(25183), new Item(25908)}),
        //M4(UpgradeCategory.MISC, 25191, 100, new Item[]{new Item(11865), new Item(20724), new Item(995, 10000000)}),
        //M5(UpgradeCategory.MISC, 25183, 100, new Item[]{new Item(11865), new Item(20724), new Item(995, 10000000)}),
        //M6(UpgradeCategory.MISC, 25908, 100, new Item[]{new Item(11865), new Item(20724), new Item(995, 10000000)}),
        M20(UpgradeCategory.MISC, 28070, 100, new Item[]{new Item(10828), new Item(13307, 10000)}),
        M7(UpgradeCategory.MISC, 21793, 100, new Item[]{new Item(2413), new Item(13307, 5000)}),
        M8(UpgradeCategory.MISC, 21795, 100, new Item[]{new Item(2414), new Item(13307, 5000)}),
        M9(UpgradeCategory.MISC, 21791, 100, new Item[]{new Item(2412), new Item(13307, 5000)}),
        M17(UpgradeCategory.MISC, 32058, 100, new Item[]{new Item(23351, 1), new Item(5547, 1), new Item(13307, 20000)}),
        M10(UpgradeCategory.MISC, 28682, 100, new Item[]{new Item(21015), new Item(13307, 15000)}),
        M11(UpgradeCategory.MISC, 28067, 50, new Item[]{new Item(10551), new Item(13307, 3000)}),
        M12(UpgradeCategory.MISC, 6199, 100, new Item[]{new Item(13307, 10000)}),
        M13(UpgradeCategory.MISC, 32164, 100, new Item[]{new Item(6199, 2), new Item(13307, 5000)}),
        M14(UpgradeCategory.MISC, 32231, 90, new Item[]{new Item(32164, 3), new Item(13307, 10000)}),
        M15(UpgradeCategory.MISC, 32165, 85, new Item[]{new Item(32231, 3), new Item(13307, 15000)}),
        M16(UpgradeCategory.MISC, 32206, 70, new Item[]{new Item(32165, 2), new Item(13307, 20000)}),





        ;

        public transient final UpgradeCategory category;
        public final int id;
        public final int chance;

        public final Item[] required;

        UpgradableItems(UpgradeCategory category, int id, int chance, Item[] required) {
            this.category = category;
            this.id = id;
            this.chance = chance;
            this.required = required;
        }
    }