package com.zenyte.game.content.lootchest;

import com.zenyte.game.content.serverevent.WorldBoost;
import com.zenyte.game.content.xamphur.XamphurBoost;
import com.zenyte.game.model.HintArrow;
import com.zenyte.game.model.HintArrowPosition;
import com.zenyte.game.task.TickTask;
import com.zenyte.game.task.WorldTasksManager;
import com.zenyte.game.util.Utils;
import com.zenyte.game.world.World;
import com.zenyte.game.world.broadcasts.WorldBroadcasts;

import java.util.EnumSet;
import java.util.Set;

import static com.zenyte.game.world.broadcasts.BroadcastType.LOTTERY;

public class ScheduledLootChestSpawn extends TickTask {

    private final LootChestLocation location;
    private boolean notified = false;

    public ScheduledLootChestSpawn(int ticks, LootChestLocation location) {
        this.ticks = ticks;
        this.location = location;
    }

    @Override
    public void run() {
        if (ticks == 0) {
            if (LootChests.getCurrent() != null) {
                LootChests.getCurrent().remove();
                LootChests.setCurrent(null);
            }

            LootChest chest = new LootChest(location);
            World.spawnObject(chest);
            LootChests.setCurrent(chest);
            new WorldBoost(XamphurBoost.LOOT_CHEST, 1).activate(false);

            World.getPlayers().forEach(player -> {
                player.getPacketDispatcher().sendHintArrow(
                        new HintArrow(location.getX(), location.getY(), (byte) location.getZ(), HintArrowPosition.CENTER)
                );
            });


            WorldBroadcasts.sendMessage(
                    "<img=68><col=ff6600><shad=000000>A mysterious loot chest has appeared " + location.getLocation() + "!",
                    LOTTERY,
                    true
            );

            int nextTicks = 10000 + Utils.random(2000); // ongeveer 60-80 minuten
            ScheduledLootChestSpawn nextSpawn = atRandomLocationExcept(location, nextTicks);
            WorldTasksManager.schedule(nextSpawn, 0, 0);
            LootChests.setSpawn(nextSpawn);

            stop();
            return;
        }

        // 1 minuut voor spawn
        if (ticks == 100 && !notified) {
            notified = true;
            WorldBroadcasts.sendMessage(
                    "<img=68><col=ff6600><shad=000000>A mysterious loot chest will appear in 1 minute!",
                    LOTTERY,
                    true
            );
        }

        ticks--;
    }


    public static ScheduledLootChestSpawn atRandomLocationExcept(LootChestLocation except, int ticks) {
        Set<LootChestLocation> locations = EnumSet.copyOf(LootChestLocation.LOCATIONS);
        if (except != null) {
            locations.remove(except);
        }
        LootChestLocation location = Utils.getRandomCollectionElement(locations);
        return new ScheduledLootChestSpawn(ticks, location);
    }

    public static ScheduledLootChestSpawn atAnyRandomLocation(int ticks) {
        return atRandomLocationExcept(null, ticks);
    }

    public static ScheduledLootChestSpawn atLocation(LootChestLocation location, int ticks) {
        return new ScheduledLootChestSpawn(ticks, location);
    }

    public LootChestLocation getLocation() {
        return location;
    }
}
