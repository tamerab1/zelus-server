/*package com.near_reality.game.world.entity.player.botplayer.impl

import com.near_reality.game.content.araxxor.attacks.Attack
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
import com.zenyte.game.world.entity.npc.NPC
import com.zenyte.game.world.entity.pathfinding.events.RouteEvent
import com.zenyte.game.world.entity.player.Player
import com.zenyte.game.world.`object`.ObjectHandler


class AFKCOMBATbot(s: String) : FakePlayer("Bot_5") { // Hardcoded Bot_3

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

        getInventory().addItem(4587, 1) // Wapen
        for (i in 0..22) {
            getSkills().setSkill(i, Utils.random(30, 99), 13034431.0)
        }

        // Als de bot geen wapen heeft, geef dan een willekeurig wapen
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
        System.out.println("[DEBUG] Bot $username spawned")

        setLocation(Location(3257, 3263))  // Startlocatie nabij Lumbridge (bijvoorbeeld)
        startBot()  // Start de bot's gedrag
    }

    /**
     * Start het gedrag van de bot, waarbij koeien in de buurt worden aangevallen.
     */
    fun startBot() {
        println("[DEBUG] Starting bot behavior for $username")

        WorldTasksManager.schedule({
            try {
                // Zoek de dichtstbijzijnde koe en begin te vechten
                val cowLocation = Location(3257, 3263) // De locatie van de koeien in Lumbridge
                val npc = findNearestCow(cowLocation)
                if (npc != null) {
                    // Beweeg naar de koe en begin met aanvallen
                    moveToNpcAndAttack(npc)
                } else {
                    println("[DEBUG] No cow found nearby.")
                }
            } catch (e: Exception) {
                println("[ERROR] Exception in bot behavior for $username: ${e.message}")
            }
        }, 0, 5) // Voer de taak om de 5 ticks uit
    }

    // Zoek naar de dichtstbijzijnde koe in de buurt
    fun findNearestCow(location: Location): NPC? {
        // Koe ID is 2790
        val cowNpcId = 2790
        val nearbyNpcs = World.getNPCs().filter {
            it.id == cowNpcId && it.getPosition().withinDistance(location, 10) // Controleer op afstand van 10 blokken
        }

        // Debugging: toon hoeveel koeien er in de buurt zijn
        println("[DEBUG] Found ${nearbyNpcs.size} cows nearby.")

        // Kies willekeurig een koe als er een in de buurt is
        return nearbyNpcs.randomOrNull()
    }

    // Beweeg naar de koe en begin met aanvallen
    fun moveToNpcAndAttack(npc: NPC) {
        this.faceEntity(npc)

        // Beweeg eerst naar de koe
        this.addWalkSteps(npc.getX(), npc.getY(), -1, true)

        // Zorg ervoor dat de aanval pas start als de bot dichtbij genoeg is
        WorldTasksManager.schedule({
            if (this.getPosition().withinDistance(npc.getPosition(), 1)) {
                performAttack(npc)
                println("[DEBUG] $username reached and is attacking cow at ${npc.getPosition()}")
            } else {
                println("[DEBUG] Still walking towards cow...")
            }
        }, 1, 1)  // Controleer elke tick of de bot dichtbij genoeg is
    }

    private fun walkTo(position: Location?, function: () -> Unit) {
        TODO("Not yet implemented")
    }


    fun performAttack(npc: NPC) {
        if (npc.isDead || npc.isFinished) {
            println("[DEBUG] Cannot attack cow: NPC is dead or removed.")
            return
        }

        this.faceEntity(npc)  // Zorg ervoor dat de bot naar de koe kijkt

        // Probeer een aanval te starten
        this.attack(npc)  // Meest waarschijnlijke correcte methode
        // this.setInteractingEntity(npc)  // Alternatief
        // this.getActionManager().setAction(Attack(npc))  // Nog een alternatief

        println("[DEBUG] $username is attacking cow at ${npc.getPosition()}")
    }


    fun attack(npc: NPC) {
        if (npc.isDead || npc.isFinished) {
            println("[DEBUG] Cannot attack NPC ${npc.id}: NPC is dead or removed.")
            return
        }

        this.faceEntity(npc) // Kijk naar de vijand

        // Start de aanval met een geldige combat methode
        this.getCombat().setTarget(npc)  // Forceer de aanval op de NPC

        println("[DEBUG] $username started attacking NPC ${npc.id}")
    }

}*/