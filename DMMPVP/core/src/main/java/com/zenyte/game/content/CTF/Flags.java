package com.zenyte.game.content.CTF;

import com.google.common.eventbus.Subscribe;
import com.zenyte.game.task.WorldTasksManager;
import com.zenyte.plugins.events.ServerLaunchEvent;

public class Flags {

    private static ScheduledFlagSpawn spawn;
    private static CaptureFlag current;

    @Subscribe
    public static void boot(ServerLaunchEvent event) {
        init();
    }

    public static void init() {
        ScheduledFlagSpawn scheduler = ScheduledFlagSpawn.atAnyRandomLocation(25);
        WorldTasksManager.schedule(scheduler, 0, 0);
        spawn = scheduler;
    }

    public static ScheduledFlagSpawn getSpawn() {
        return spawn;
    }

    public static void setSpawn(ScheduledFlagSpawn spawn) {
        Flags.spawn = spawn;
    }

    public static CaptureFlag getCurrent() {
        return current;
    }

    public static void setCurrent(CaptureFlag current) {
        Flags.current = current;
    }
}
