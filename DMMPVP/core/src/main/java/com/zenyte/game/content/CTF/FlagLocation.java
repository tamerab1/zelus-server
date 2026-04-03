package com.zenyte.game.content.CTF;

import java.util.EnumSet;
import java.util.Set;


public enum FlagLocation {
    DARK_WARRIORS_FORTRESS("inside the forgotten cemetry", 2977, 3759, 0),
    CHAOS_TEMPLE("near the hot anvil!", 3359, 3935, 0),
    DEMONIC_RUINS("near the larran's chest", 3277, 3661, 0),
    GRAVEYARD("in the middle of the dark warrior's fortress", 3029, 3632, 0),
    LAVA_MAZE("near the chinchompa hunting spot", 3136, 3787, 0);

    public static final Set<FlagLocation> LOCATIONS = EnumSet.allOf(FlagLocation.class);

    private final String location;
    private final int x, y, z;

    FlagLocation(String location, int x, int y, int z) {
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
