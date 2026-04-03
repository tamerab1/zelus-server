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
import com.zenyte.game.world.`object`.ObjectHandler

class AFKthievingbot(s: String) : FakePlayer("Pikachuu") { // Hardcoded Bot_4

    /**
     * Spawnt de bot op een specifieke locatie.
     */
    fun spawnAt(player: Player,) {
        putBooleanAttribute("registered", true)
        ClanManager.join(this, GameConstants.SERVER_CHANNEL_NAME)
        unlock()
        isInitialized = true
        forceLocation(location)
        loadMapRegions(true)
        lastLoadedMapRegionTile = location.copy()
        World.addPlayer(this)
        val rebuildNormal = RebuildNormal(this, true).encode()

        getInventory().addItem(4587, 1)
        for (i in 0..22) {
            getSkills().setSkill(i, Utils.random(30,99), 13034431.0)
        }
        if (weapon == null) {
            val wieldableWeaponWithSlot = inventory.container.items.filterValues { it.definitions.containsOption("Wield") }.entries.randomOrNull()
            if (wieldableWeaponWithSlot != null) {
                val (slot, wieldableWeapon) = wieldableWeaponWithSlot
                val option = wieldableWeapon.definitions.getSlotForOption("Wield")
                OpHeldEvent(slot, wieldableWeapon.id, option).handle(this)
            }
        }
        getInventory().setInventory(inventory)
        getEquipment().setEquipment(equipment)
        combatDefinitions.refresh()
        bonuses.update()
        updateFlags.flag(UpdateFlag.APPEARANCE)
        combatDefinitions.isAutoRetaliate = true
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

                ObjectHandler.handle(this, 50089, Location(3113, 3498), true, 1)
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