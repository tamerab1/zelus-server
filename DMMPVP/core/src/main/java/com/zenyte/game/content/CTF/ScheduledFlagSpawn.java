package com.zenyte.game.content.CTF;

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

public class ScheduledFlagSpawn extends TickTask {

    private final FlagLocation location;
    private boolean notified = false;

    public ScheduledFlagSpawn(int ticks, FlagLocation location) {
        this.ticks = ticks;
        this.location = location;
    }

    @Override
    public void run() {
        if (ticks == 0) {
            if (Flags.getCurrent() != null) {
                Flags.getCurrent().remove();
                Flags.setCurrent(null);
            }

            CaptureFlag flag = new CaptureFlag(location);
            World.spawnObject(flag);
            Flags.setCurrent(flag);

// ✅ Voeg dit toe:
            World.addBoost(new WorldBoost(XamphurBoost.WILDERNESS_FLAG, 1));


            World.getPlayers().forEach(player -> {
                player.getPacketDispatcher().sendHintArrow(
                        new HintArrow(location.getX(), location.getY(), (byte) location.getZ(), HintArrowPosition.CENTER)
                );
            });

            WorldBroadcasts.sendMessage(
                    "<img=70><col=4ea3f1><shad=000000>A mysterious flag has appeared " + location.getLocation() + "!",
                    LOTTERY,
                    true
            );


            int nextTicks = 10000 + Utils.random(2000); // ongeveer 60-80 minuten
            ScheduledFlagSpawn nextSpawn = atRandomLocationExcept(location, nextTicks);
            WorldTasksManager.schedule(nextSpawn, 0, 0);
            Flags.setSpawn(nextSpawn);

            stop();
            return;
        }

        // 1 minuut voor spawn
        if (ticks == 100 && !notified) {
            notified = true;
            WorldBroadcasts.sendMessage(
                    "<img=70><col=4ea3f1><shad=000000>A mysterious flag will appear in 1 minute!",
                    LOTTERY,
                    true
            );
        }

        ticks--;
    }

    public static ScheduledFlagSpawn atRandomLocationExcept(FlagLocation except, int ticks) {
        Set<FlagLocation> locations = EnumSet.copyOf(FlagLocation.LOCATIONS);
        if (except != null) {
            locations.remove(except);
        }
        FlagLocation location = Utils.getRandomCollectionElement(locations);
        return new ScheduledFlagSpawn(ticks, location);
    }

    public static ScheduledFlagSpawn atAnyRandomLocation(int ticks) {
        return atRandomLocationExcept(null, ticks);
    }

    public static ScheduledFlagSpawn atLocation(FlagLocation location, int ticks) {
        return new ScheduledFlagSpawn(ticks, location);
    }

    public FlagLocation getLocation() {
        return location;
    }
}
