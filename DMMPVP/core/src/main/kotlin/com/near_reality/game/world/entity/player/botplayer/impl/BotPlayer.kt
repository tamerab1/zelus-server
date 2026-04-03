package com.near_reality.game.world.entity.player.botplayer.impl

import com.near_reality.game.world.entity.player.botplayer.FakePlayer
import com.zenyte.game.content.consumables.drinks.Potion
import com.zenyte.game.packet.out.RebuildNormal
import com.zenyte.game.task.WorldTask
import com.zenyte.game.task.WorldTasksManager
import com.zenyte.game.util.Utils
import com.zenyte.game.world.World
import com.zenyte.game.world.entity.Location
import com.zenyte.game.world.entity.masks.UpdateFlag
import com.zenyte.game.world.entity.player.Player
import com.zenyte.game.world.region.GlobalAreaManager




val SPAWNLOCATION = Location(3360, 7206, 1)
fun spawnFakePlayers(
    player: Player,
) {
        spawn(player, +1, SPAWNLOCATION)
        spawn(player, +1, SPAWNLOCATION)
    }



 fun spawn(player: Player, index: Int, location: Location) {
     val fake = FakePlayer("Bot_$index") // Dit geeft elke bot een unieke naam zoals "Bot_1", "Bot_2", enz.
    fake.setDefaultSettings()
    fake.forceLocation(location.copy())
    copy(player, fake)
    World.addPlayer(fake)
    fake.loadMapRegions(true)
    fake.lastLoadedMapRegionTile = location.copy()
    fake.afterLoadMapRegions()
    fake.putBooleanAttribute("registered", true)
    val rebuildNormal = RebuildNormal(fake, true).encode()
    rebuildNormal.release()
    WorldTasksManager.schedule(1) {
        fake.isInitialized = true
        fake.combatDefinitions.isAutoRetaliate = false
        fake.onLogin()
        WorldTasksManager.schedule(1) {
            fake.forceLocation(location.copy())
            scheduleAutoPvmArenaTask(fake)
        }
    }
}

private fun copy(player: Player, target: Player) {
    if (target.getNumericAttribute("first_99_skill").toInt() == -1) {
        target.addAttribute("first_99_skill", 0)
    }
    //sets skills to random level 0-100
    for (i in 0..22) {
        target.getSkills().setSkill(i, Utils.random(100), 13034431.0)
    }
    target.combatDefinitions.initialize(player.combatDefinitions)
    target.getInventory().setInventory(player.inventory)
    target.getEquipment().setEquipment(player.equipment)
    target.combatDefinitions.refresh()
    target.bonuses.update()
    target.updateFlags.flag(UpdateFlag.APPEARANCE)
}

fun scheduleAutoPvmArenaTask(fake: Player) {
    listOf(
        Potion.SUPER_ATTACK_POTION,
        Potion.SUPER_DEFENCE_POTION,
        Potion.SUPER_STRENGTH_POTION
    ).forEach {
        it.onConsumption(fake)
        fake.chatMessage.set("ah some good potions", 2 , false)
    }
    scheduleStateHandlerTask(0, 1) {
        if (fake is FakePlayer)
            World.removePlayer(fake)
        return@scheduleStateHandlerTask false
    }

    if (fake.area == null) {
        GlobalAreaManager.getArea(fake)?.add(fake)
    }
    if (fake.getVariables().runEnergy > 50 && !fake.isRun) {
        fake.setForceTalk("Switching to run mode")
        true.also { fake.isRun = it }
    }
    fake.setForceTalk(fake.area.name())
    fake.chatMessage.set("Testing and talking talking and testing", 1, false)
    var cycle = 0
    cycle++
    if ((cycle % 10) == 0) {
        if (Utils.random(50) <= 15)
            fake.setForceTalk("I like rock crabs")
    }
}
fun scheduleStateHandlerTask(
    ticksTillStart: Int = WorldTasksManager.DEFAULT_INITIAL_DELAY,
    tickCyclePeriod: Int = WorldTasksManager.NO_PERIOD_DELAY,
    taskWithCycle: (Int) -> Boolean,
) {
    var cycle = 0
    val worldTask = object : WorldTask {
        override fun run() {
            if (!taskWithCycle(cycle++))
                stop()
        }
    }
    WorldTasksManager.schedule(worldTask, ticksTillStart, tickCyclePeriod)
}

