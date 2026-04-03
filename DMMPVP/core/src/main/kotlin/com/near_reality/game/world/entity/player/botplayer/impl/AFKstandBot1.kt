package com.near_reality.game.world.entity.player.botplayer.impl

import com.near_reality.game.world.entity.combatController
import com.near_reality.game.world.entity.player.botplayer.FakePlayer
import com.zenyte.game.GameConstants
import com.zenyte.game.content.clans.ClanManager
import com.zenyte.game.packet.out.RebuildNormal
import com.zenyte.game.task.WorldTasksManager
import com.zenyte.game.util.Utils
import com.zenyte.game.world.World
import com.zenyte.game.world.entity.Location
import com.zenyte.game.world.entity.masks.UpdateFlag
import com.zenyte.game.world.entity.player.Player
import com.zenyte.game.world.entity.player.container.impl.equipment.EquipmentSlot

class AFKstandBot1(s: String) : FakePlayer("PVMElijahh") {

    fun spawnAt(player: Player) {
        println("[DEBUG] >>> spawnAt() aangeroepen")
        putBooleanAttribute("registered", true)
        ClanManager.join(this, GameConstants.SERVER_CHANNEL_NAME)
        unlock()
        isInitialized = true
        forceLocation(location)
        loadMapRegions(true)
        lastLoadedMapRegionTile = location.copy()
        World.addPlayer(this)
        val rebuildNormal = RebuildNormal(this, true).encode()

        val equipmentItems = mapOf(
            EquipmentSlot.WEAPON to 12006,
            EquipmentSlot.HELMET to 11865,
            EquipmentSlot.PLATE to 11832,
            EquipmentSlot.LEGS to 11834,
            EquipmentSlot.HANDS to 7462,
            EquipmentSlot.BOOTS to 11840,
            EquipmentSlot.CAPE to 6570,
            EquipmentSlot.AMULET to 6585,
            EquipmentSlot.SHIELD to 12954
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
        println("[DEBUG] Bot $username heeft volledige Bandos gear en Dragon Scimitar aangetrokken.")

        for (i in 0..12) {
            getSkills().setSkill(i, Utils.random(30, 99), 4537234.0)
        }

        combatDefinitions.refresh()
        println("[DEBUG] Bot $username heeft zijn skills gekregen. Combat level: " + getCombatLevel())

        setLocation(Location(3093, 3532))
        startBot()
    }

    fun startBot() {
        println("[DEBUG] >>> startBot() aangeroepen voor bot $name")

        println("[DEBUG] Bot $name is now idling at (3093, 3532).")

        // Combat-loop: check elke tick of de bot is aangevallen
        WorldTasksManager.schedule(1, 1) {
            val bot = this@AFKstandBot1
            if (bot.isDead) return@schedule

            val attacker = bot.combatController.lastAttackedBySince(10)?.attacker?.get()
            if (attacker is Player && !attacker.isDead) {
                println("[DEBUG] Bot ${bot.name} detected attack from ${attacker.name}, initiating counter-attack.")

                bot.combatController.markTarget(attacker)
                bot.combatController.markAttack(attacker)

                // Hier voeren we daadwerkelijk de aanval uit via PlayerCombat
                com.zenyte.game.world.entity.player.action.combat.PlayerCombat.attackEntity(bot, attacker, null)
                println("[DEBUG] Bot is attempting to attack ${attacker.name} at ${attacker.location}, canHit=${bot.canHit(attacker)}")

            }
        }

        // Idle behavior (willekeurig bewegen)
        WorldTasksManager.schedule(1500) {
            val bot = this@AFKstandBot1
            if (bot.isDead) return@schedule

            val newX = 3093 + Utils.random(-3, 3)
            val newY = 3532 + Utils.random(-3, 3)
            bot.setLocation(Location(newX, newY))
        }
    }

}
