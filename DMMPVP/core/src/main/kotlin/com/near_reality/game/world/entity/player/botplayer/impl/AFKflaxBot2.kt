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

class AFKflaxBot2(s: String) : FakePlayer("PVMderk") { // Hardcoded bot_2

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
            EquipmentSlot.WEAPON to 4587, // Dragon Scimitar
            EquipmentSlot.HELMET to 10828, // Helm of Neitiznot
            EquipmentSlot.PLATE to 11832, // Bandos Chestplate
            EquipmentSlot.LEGS to 11834, // Bandos Tassets
            EquipmentSlot.HANDS to 7462, // Barrows Gloves
            EquipmentSlot.BOOTS to 11840, // Dragon Boots
            EquipmentSlot.CAPE to 6570, // Fire Cape
            EquipmentSlot.AMULET to 6585, // Amulet of Fury
            EquipmentSlot.SHIELD to 12954 // D defender
        )

// Loop door de items en equip ze
        for ((slot, itemId) in equipmentItems) {
            val currentItem = getEquipment().getItem(slot.getSlot())
            if (currentItem != null) {
                getInventory().addItem(currentItem.id, 1) // Voeg huidig item terug toe aan de inventory
                getEquipment().set(slot.getSlot(), null) // Verwijder huidig item uit equipment
            }
            getInventory().addItem(itemId, 1) // Voeg nieuwe item toe
            val newItem = getInventory().getItemById(itemId)
            if (newItem != null) {
                getEquipment().set(slot.getSlot(), newItem) // Zet item direct in equipment
                getInventory().deleteItem(itemId, 1) // Verwijder uit inventory
            }
        }

// Refresh de appearance en equipment
        getEquipment().refresh()
        updateFlags.flag(UpdateFlag.APPEARANCE)

// Debug bericht
        System.out.println("[DEBUG] Bot $username heeft volledige Bandos gear en Dragon Scimitar aangetrokken.")

        // Skills instellen tussen 30 en 99, zodat combat level niet 3 blijft
        for (i in 0..12) { // Combat skills: Attack, Strength, Defence, HP, Ranged, Magic, Prayer
            getSkills().setSkill(i, Utils.random(30, 99), 4537234.0)
        }

// Verplicht een refresh van de combat-definities
        combatDefinitions.refresh()

// Debug bericht om te checken of skills correct worden ingesteld
        System.out.println("[DEBUG] Bot $username heeft zijn skills gekregen. Combat level: " + getCombatLevel())

        getEquipment().refresh()
        updateFlags.flag(UpdateFlag.APPEARANCE)

        // Debug message
        System.out.println("[DEBUG] Bot $username heeft Dragon Scimitar aangetrokken.")

        // Beweging starten
        setLocation(Location(3101, 3498)) // Zorg dat de bot zich verplaatst
        System.out.println("[DEBUG] Bot $username verplaatst naar startpositie.")

        rebuildNormal.release()
        onLogin()
        startBot() // Zorg dat de bot start
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