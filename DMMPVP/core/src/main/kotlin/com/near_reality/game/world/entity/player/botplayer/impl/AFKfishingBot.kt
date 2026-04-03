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

class AFKfishingBot(s: String) : FakePlayer("Band4bnd") { // Hardcoded Bot_3

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
            EquipmentSlot.WEAPON to 303, // ancient godsword
            EquipmentSlot.CAPE to 6570, // Fire Cape
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


        for (i in 0..20) {
            getSkills().setSkill(i, Utils.random(30, 99), 9537234.0)
        }


        combatDefinitions.refresh()

        System.out.println("[DEBUG] Bot $username heeft zijn skills gekregen. Combat level: " + getCombatLevel())

        getEquipment().refresh()
        updateFlags.flag(UpdateFlag.APPEARANCE)


        System.out.println("[DEBUG] Bot $username heeft Dragon Scimitar aangetrokken.")
        rebuildNormal.release()
        onLogin()

        // Debugging
        System.out.println("[DEBUG] Bot $username spawned ")

        setLocation(/* tile = */ Location(3101, 3498))
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

                ObjectHandler.handle(this, 50096, Location(3112, 3495), true, 1)
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