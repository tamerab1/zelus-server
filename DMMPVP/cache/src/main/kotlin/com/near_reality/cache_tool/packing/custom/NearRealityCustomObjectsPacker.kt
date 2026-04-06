package com.near_reality.cache_tool.packing.custom

import com.near_reality.game.item.CustomObjectId
import com.zenyte.game.world.`object`.ObjectId
import mgi.tools.parser.TypeParser
import mgi.types.config.ObjectDefinitions
import java.nio.file.Files
import java.nio.file.Paths

object NearRealityCustomObjectsPacker {

    @JvmStatic
    fun pack(){
        TypeParser.cloneObject(ObjectId.ZAMORAK_PORTAL, 35015).apply {
            name = "Private portal"
            sizeX /= 2
            sizeY /= 2
            modelSizeX /= 2
            modelSizeY /= 2
            modelSizeHeight /= 2
            mapSceneId = 64
            setOption(0, "Use")
            pack()
        }

        ObjectDefinitions.get(47567).apply {
            setOption(1, "Take")
            pack()
        }

        ObjectDefinitions.get(47568).apply {
            setOption(1, "Take")
            pack()
        }

        TypeParser.cloneObject(ObjectId.ZAMORAK_PORTAL, 55001).apply {
            name = "Panda"
            models = intArrayOf(60348)
            pack()
        }

        TypeParser.cloneObject(ObjectId.ZAMORAK_PORTAL, 55002).apply {
            name = "Timmy Chest"
            models = intArrayOf(60353)
            pack()
        }
        

        // ── BH Crate object ───────────────────────────────────────────────
        // Model ID 60362 — packed via NearRealityCustomItemPacker from BH Crate Object.dat
        TypeParser.cloneObject(ObjectId.CLOSED_CHEST, CustomObjectId.BH_CRATE).apply {
            name = "Bounty Hunter Crate"
            models = intArrayOf(60362)
            setOption(0, "Open")
            pack()
        }

        // ── Boss Statues (model IDs 60248–60259) ──────────────────────────
        TypeParser.cloneObject(ObjectId.STATUE, CustomObjectId.ABYSSAL_STATUE).apply {
            name = "Abyssal Sire statue"
            models = intArrayOf(60248)
            setOption(0, "Admire")
            pack()
        }

        TypeParser.cloneObject(ObjectId.STATUE, CustomObjectId.HYDRA_STATUE).apply {
            name = "Alchemical Hydra statue"
            models = intArrayOf(60249)
            setOption(0, "Admire")
            pack()
        }

        TypeParser.cloneObject(ObjectId.STATUE, CustomObjectId.JAD_STATUE).apply {
            name = "TzTok-Jad statue"
            models = intArrayOf(60250)
            setOption(0, "Admire")
            pack()
        }

        TypeParser.cloneObject(ObjectId.STATUE, CustomObjectId.KALAPHITE_STATUE).apply {
            name = "Kalaphite Queen statue"
            models = intArrayOf(60251)
            setOption(0, "Admire")
            pack()
        }

        TypeParser.cloneObject(ObjectId.STATUE, CustomObjectId.KBD_STATUE).apply {
            name = "King Black Dragon statue"
            models = intArrayOf(60252)
            setOption(0, "Admire")
            pack()
        }

        TypeParser.cloneObject(ObjectId.STATUE, CustomObjectId.OLM_STATUE).apply {
            name = "Great Olm statue"
            models = intArrayOf(60253)
            setOption(0, "Admire")
            pack()
        }

        TypeParser.cloneObject(ObjectId.STATUE, CustomObjectId.REGULAR_SLAYER_STATUE).apply {
            name = "Slayer statue"
            models = intArrayOf(60254)
            setOption(0, "Admire")
            pack()
        }

        TypeParser.cloneObject(ObjectId.STATUE, CustomObjectId.SKOTIZO_STATUE).apply {
            name = "Skotizo statue"
            models = intArrayOf(60255)
            setOption(0, "Admire")
            pack()
        }

        TypeParser.cloneObject(ObjectId.STATUE, CustomObjectId.VORKATH_STATUE).apply {
            name = "Vorkath statue"
            models = intArrayOf(60256)
            setOption(0, "Admire")
            pack()
        }

        TypeParser.cloneObject(ObjectId.STATUE, CustomObjectId.SLAYER_STATUE_EMPTY).apply {
            name = "Empty slayer pedestal"
            models = intArrayOf(60257)
            setOption(0, "Examine")
            pack()
        }

        TypeParser.cloneObject(ObjectId.STATUE, CustomObjectId.VERZIK_STATUE).apply {
            name = "Verzik Vitur statue"
            models = intArrayOf(60258)
            setOption(0, "Admire")
            pack()
        }

        TypeParser.cloneObject(ObjectId.STATUE, CustomObjectId.ZUK_STATUE).apply {
            name = "TzKal-Zuk statue"
            models = intArrayOf(60259)
            setOption(0, "Admire")
            pack()
        }

        // ── Wood Score Board ──────────────────────────────────────────────
        TypeParser.cloneObject(ObjectId.ZAMORAK_PORTAL, CustomObjectId.WOOD_SCORE_BOARD).apply {
            name = "Scoreboard"
            models = intArrayOf(60197)
            setOption(0, "Check")
            pack()
        }

    }
}
