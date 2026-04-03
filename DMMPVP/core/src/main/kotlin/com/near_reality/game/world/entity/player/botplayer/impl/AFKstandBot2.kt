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

class AFKstandBot2(s: String) : FakePlayer("Octog") { // Hardcoded Bot

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
            EquipmentSlot.CAPE to 6570, // Fire Cape

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

        // Spawn op nieuwe vaste locatie (3092, 3495)
        setLocation(Location(3083, 3499))

        startBot()
    }

    /**
     * Start het gedrag van de bot (af en toe een paar tiles bewegen).
     */
    fun startBot() {
        System.out.println("[DEBUG] Bot $username is now idling at (3092, 3495).")

        WorldTasksManager.schedule(1500) { // 1500 ticks = ongeveer 15 minuten
            if (isDead) {
                System.out.println("[DEBUG] Bot $username is dead. Stopping movement.")
                return@schedule
            }

            // Willekeurig 2-3 tiles bewegen binnen het gebied van (3092, 3495)
            val newX = 3083 + Utils.random(-3, 3)
            val newY = 3499 + Utils.random(-3, 3)

            setLocation(Location(newX, newY))
        }
    }
}