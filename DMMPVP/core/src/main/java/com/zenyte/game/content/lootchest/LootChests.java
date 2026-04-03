package com.zenyte.game.content.lootchest;

import com.google.common.eventbus.Subscribe;
import com.zenyte.plugins.events.ServerLaunchEvent;
import com.zenyte.game.task.WorldTasksManager;

public class LootChests {

    private static ScheduledLootChestSpawn spawn;
    private static LootChest current;

    @Subscribe
    public static void boot(ServerLaunchEvent event) {
        init();
    }

    public static void init() {
        ScheduledLootChestSpawn scheduler = ScheduledLootChestSpawn.atAnyRandomLocation(25);
        WorldTasksManager.schedule(scheduler, 0, 0);
        spawn = scheduler;
    }

    public static ScheduledLootChestSpawn getSpawn() {
        return spawn;
    }

    public static void setSpawn(ScheduledLootChestSpawn spawn) {
        LootChests.spawn = spawn;
    }

    public static LootChest getCurrent() {
        return current;
    }

    public static void setCurrent(LootChest current) {
        LootChests.current = current;
    }
}