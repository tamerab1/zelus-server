package com.near_reality.cache_tool.packing.custom

import com.zenyte.game.item.ItemId
import mgi.types.config.enums.EnumDefinitions
import net.runelite.cache.util.ScriptVarType

object NearRealityCustomEnumsPacker {
    @JvmStatic
    fun pack() {
        /* Bosses Collection Log Entries */
        EnumDefinitions.get(2103).apply {
            values.clear()
            var idx = 0
            this.values[idx++] = 476
            this.values[idx++] = 539
            this.values[idx++] = 10503
            this.values[idx++] = 477
            this.values[idx++] = 478
            this.values[idx++] = 479
            this.values[idx++] = 480
            this.values[idx++] = 481
            this.values[idx++] = 482
            this.values[idx++] = 483
            this.values[idx++] = 484
            this.values[idx++] = 485
            this.values[idx++] = 486
            this.values[idx++] = 10500
            this.values[idx++] = 500
            this.values[idx++] = 605
            this.values[idx++] = 10300
            this.values[idx++] = 487
            this.values[idx++] = 488
            this.values[idx++] = 489
            this.values[idx++] = 541
            this.values[idx++] = 499
            this.values[idx++] = 490
            this.values[idx++] = 491
            this.values[idx++] = 492
            this.values[idx++] = 493
            this.values[idx++] = 494
            this.values[idx++] = 3769
            this.values[idx++] = 1263
            this.values[idx++] = 495
            this.values[idx++] = 4455
            this.values[idx++] = 10321
            this.values[idx++] = 601
            this.values[idx++] = 496
            this.values[idx++] = 497
            this.values[idx++] = 498
            this.values[idx++] = 10502
            this.values[idx++] = 10322
            this.values[idx++] = 10501
            this.values[idx++] = 501
            this.values[idx++] = 502
            this.values[idx++] = 503
            this.values[idx++] = 504
            this.values[idx++] = 604
            this.values[idx++] = 505
            this.pack()
        }

        EnumDefinitions.create(10500, ScriptVarType.INTEGER, ScriptVarType.NAMEDOBJ).apply {
            this.values[0] = ItemId.CHROMIUM_INGOT
            this.values[1] = ItemId.MAGUS_ICON
            this.values[2] = ItemId.EYE_OF_THE_DUKE
            this.values[3] = ItemId.VIRTUS_MASK
            this.values[4] = ItemId.VIRTUS_ROBE_TOP
            this.values[5] = ItemId.VIRTUS_ROBE_LEGS
            this.values[6] = ItemId.FROZEN_TABLET
            this.values[7] = ItemId.AWAKENERS_ORB
            this.values[8] = ItemId.ICE_QUARTZ
            this.pack()
        }

        EnumDefinitions.create(10501, ScriptVarType.INTEGER, ScriptVarType.NAMEDOBJ).apply {
            this.values[0] = ItemId.CHROMIUM_INGOT
            this.values[1] = ItemId.ULTOR_ICON
            this.values[2] = ItemId.EXECUTIONERS_AXE_HEAD
            this.values[3] = ItemId.VIRTUS_MASK
            this.values[4] = ItemId.VIRTUS_ROBE_TOP
            this.values[5] = ItemId.VIRTUS_ROBE_LEGS
            this.values[6] = ItemId.STRANGLED_TABLET
            this.values[7] = ItemId.AWAKENERS_ORB
            this.values[8] = ItemId.BLOOD_QUARTZ
            this.pack()
        }

        /* Tormented Demons */
        EnumDefinitions.create(10502, ScriptVarType.INTEGER, ScriptVarType.NAMEDOBJ).apply {
            this.values[0] = ItemId.TORMENTED_SYNAPSE
            this.values[1] = ItemId.BURNING_CLAW
            this.values[2] = ItemId.SMOULDERING_GLAND
            this.values[3] = ItemId.SMOULDERING_PILE_OF_FLESH
            this.values[4] = ItemId.SMOULDERING_HEART
            this.pack()
        }

        /* Arraxor */
        EnumDefinitions.create(10503, ScriptVarType.INTEGER, ScriptVarType.NAMEDOBJ).apply {
            this.values[0] = ItemId.NOXIOUS_BLADE
            this.values[1] = ItemId.NOXIOUS_POINT
            this.values[2] = ItemId.NOXIOUS_POMMEL
            this.values[3] = ItemId.ARAXYTE_FANG
            this.values[4] = ItemId.ARAXYTE_HEAD
            this.values[5] = ItemId.JAR_OF_VENOM
            this.values[6] = ItemId.NID
            this.pack()
        }
//        EnumDefinitions.create(595, ScriptVarType.INTEGER, ScriptVarType.STRING).apply {
//            defaultString = ""
//            defaultInt = 0
//
//            this.values[1] = "PK Zone 1"
//            this.values[5] = "PK Zone 2"
//            this.values[2] = "High-Risk Area"
//            this.values[3] = "1v1 Wilderness"
//            this.values[4] = "Multi Wilderness"
//            this.values[0] = "Edge PvP"
//            this.values[6] = "PvP Box"
//            this.values[7] = "Duel Arena"
//            this.values[8] = "Safe Zone"
//            this.values[10] = "LMS"
//            this.values[9] = "Tournaments"
//            this.values[11] = "Bounty Area"
//
//            this.pack()
//        }
        EnumDefinitions.get(2106).apply {
            values.clear()
            this.values[0] = 518   // Novice
            this.values[1] = 522   // Recruit
            this.values[2] = 1283  // Veteran
            this.pack()
        }

        /* Items enum voor Wilderness Veteran */
        EnumDefinitions.create(3041, ScriptVarType.INTEGER, ScriptVarType.NAMEDOBJ).apply {
            values.clear()
            this.values[0] = ItemId.DRAGON_SCIMITAR
            this.values[1] = ItemId.AMULET_OF_GLORY
            this.values[2] = ItemId.COINS_995
            this.values[3] = ItemId.RUNE_SCIMITAR
            this.values[4] = ItemId.DRAGON_DAGGER
            this.pack()
        }

        /* Bestaande voorbeelden (laten zoals jij ze had) */
        EnumDefinitions.create(2152, ScriptVarType.INTEGER, ScriptVarType.NAMEDOBJ).apply {
            values.clear()
            this.values[0] = 24565
            this.values[1] = 24567
            this.values[2] = 24569
            this.values[3] = 24571
            this.values[4] = 24573
            this.values[5] = 24575
            this.values[6] = 24577
            this.values[7] = 24579
            this.values[8] = 24581
            this.values[9] = 24583
            this.pack()
        }

        EnumDefinitions.create(2153, ScriptVarType.INTEGER, ScriptVarType.NAMEDOBJ).apply {
            values.clear()
            this.values[0] = 28082
            this.values[1] = 28084
            this.values[2] = 28086
            this.values[3] = 28088
            this.values[4] = 28090
            this.values[5] = 28092
            this.values[6] = 28094
            this.values[7] = 28096
            this.values[8] = 28098
            this.pack()
        }


        EnumDefinitions.create(20002, ScriptVarType.INTEGER, ScriptVarType.STRING).apply {
            defaultString = "Overview"
            this.values[0] = "Overview"
            this.values[1] = "Melee"
            this.values[2] = "Ranged"
            this.values[3] = "Magic"
            this.values[4] = "Supplies"
            this.values[5] = "Skilling"
            this.values[6] = "Jewelry"
            this.values[7] = "General"
            this.values[8] = "Slayer"
            this.values[9] = "Bounty Hunter"
            this.values[10] = "Capes"
            this.values[11] = "Blood Money"
            this.values[12] = "Loyalty"
            this.values[13] = "Vote"
            this.pack()
        }
    }
}