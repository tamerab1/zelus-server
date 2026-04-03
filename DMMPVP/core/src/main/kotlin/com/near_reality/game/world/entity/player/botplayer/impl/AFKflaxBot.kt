package com.near_reality.game.world.entity.player.botplayer.impl

import com.near_reality.game.world.entity.player.botplayer.FakePlayer
import com.zenyte.game.GameConstants
import com.zenyte.game.content.clans.ClanManager
import com.zenyte.game.packet.`in`.event.OpHeldEvent
import com.zenyte.game.packet.out.RebuildNormal
import com.zenyte.game.task.WorldTasksManager
import com.zenyte.game.util.Utils
import com.zenyte.game.world.World
import com.zenyte.game.world.entity.Location
import com.zenyte.game.world.entity.masks.UpdateFlag
import com.zenyte.game.world.entity.player.Player
import com.zenyte.game.world.entity.player.container.impl.equipment.EquipmentSlot
import com.zenyte.game.world.`object`.ObjectHandler

class AFKflaxBot(s: String) : FakePlayer("Hazy29") { // Hardcoded bot_2

    /**
     * Spawnt de bot op een specifieke locatie.
     */
    fun spawnAt(player: Player) {
        putBooleanAttribute("registered", true)
        ClanManager.join(this, GameConstants.SERVER_CHANNEL_NAME)
        unlock()
        isInitialized = true
        forceLocation(location)
        loadMapRegions(true)
        lastLoadedMapRegionTile = location.copy()
        World.addPlayer(this)
        val rebuildNormal = RebuildNormal(this, true).encode()

// Equipment IDs
        val equipmentItems = mapOf(
            EquipmentSlot.WEAPON to 4151, // Dragon Scimitar
            EquipmentSlot.HELMET to 11665, // Void helmet
            EquipmentSlot.PLATE to 13072, // Void
            EquipmentSlot.LEGS to 13073, // Void
            EquipmentSlot.HANDS to 8842, // Barrows Gloves
            EquipmentSlot.BOOTS to 11840, // Dragon Boots
            EquipmentSlot.CAPE to 6570, // Fire Cape
            EquipmentSlot.AMULET to 6585, // Amulet of Fury
            EquipmentSlot.SHIELD to 12954 // D defender
        )

        for ((slot, itemId) in equipmentItems) {
            val currentItem = getEquipment().getItem(slot.getSlot())
            if (currentItem != null) {
                getInventory().addItem(currentItem.id, 1)
                getEquipment().set(slot.getSlot(), null)
            }
            getInventory().addItem(itemId, 1)
            val newItem = getInventory().getItemById(itemId)
            if (newItem != null) {
                getEquipment().set(slot.getSlot(), newItem)
                getInventory().deleteItem(itemId, 1)
            }
        }


        getEquipment().refresh()
        updateFlags.flag(UpdateFlag.APPEARANCE)


        System.out.println("[DEBUG] Bot $username heeft volledige Bandos gear en Dragon Scimitar aangetrokken.")


        for (i in 0..12) {
            getSkills().setSkill(i, Utils.random(30, 99), 8537234.0)

        }


        combatDefinitions.refresh()

        System.out.println("[DEBUG] Bot $username heeft zijn skills gekregen. Combat level: " + getCombatLevel())

        getEquipment().refresh()
        updateFlags.flag(UpdateFlag.APPEARANCE)


        System.out.println("[DEBUG] Bot $username heeft Dragon Scimitar aangetrokken.")


        setLocation(Location(3101, 3498)) // Zorg dat de bot zich verplaatst
        System.out.println("[DEBUG] Bot $username verplaatst naar startpositie.")

        rebuildNormal.release()
        onLogin()
        startBot()
    }
    /**
     * Start het gedrag van de bot, waarbij NPC's worden gezocht en aangevallen.
     */
    fun startBot() {
        System.out.println("[DEBUG] Starting bot behavior for $username")

        WorldTasksManager.schedule(Utils.random(30)) { // Scheduler met ticks
            try {
                getUpdateFlags().flag(UpdateFlag.CHAT)
                getChatMessage().set("Hi my name is " + this.username + "", 2 , false)

                ObjectHandler.handle(this, 50093, Location(3105, 3503), true, 1)
            } catch (e: Exception) {
                System.out.println("[ERROR] Exception in bot behavior for $username: ${e.message}")
            }
            if (isDead) {
                System.out.println("[DEBUG] Bot $username is either dead or not active. Stopping behavior.")
                setForceTalk("I am no longer active.")
                return@schedule
            }


            // try {
            System.out.println("[DEBUG] Bot $username is running behavior loop.")
            // } catch (e: Exception) {
            //       System.out.println("[ERROR] Exception in bot behavior for $username: ${e.message}")
        }
    }}