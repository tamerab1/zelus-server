package com.zenyte.game.content.lootchest;

import java.util.EnumSet;
import java.util.Set;
import com.zenyte.game.content.lootchest.LootChestLocation;


public enum LootChestLocation {
    DARK_WARRIORS_FORTRESS("near Dark Warriors' Fortress", 3036, 3651, 0),
    CHAOS_TEMPLE("near Chaos Temple", 3235, 3622, 0),
    DEMONIC_RUINS("at Demonic Ruins", 3282, 3886, 0),
    GRAVEYARD("at the Graveyard of Shadows", 3167, 3671, 0),
    LAVA_MAZE("near Lava Maze", 3032, 3852, 0);

    public static final Set<LootChestLocation> LOCATIONS = EnumSet.allOf(LootChestLocation.class);

    private final String location;
    private final int x, y, z;

    LootChestLocation(String location, int x, int y, int z) {
        this.location = location;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public String getLocation() {
        return location;
    }

    public int getX() { return x; }

    public int getY() { return y; }

    public int getZ() { return z; }
}
