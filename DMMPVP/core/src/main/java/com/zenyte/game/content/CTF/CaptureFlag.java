package com.zenyte.game.content.CTF;

import com.zenyte.game.world.World;
import com.zenyte.game.world.object.WorldObject;

public class CaptureFlag extends WorldObject {
    private final FlagLocation location;

    public CaptureFlag(FlagLocation location) {
        super(15998, 10, 0, location.getX(), location.getY(), location.getZ());
        this.location = location;
    }

    public FlagLocation getFlagLocation() {
        return location;
    }

    public void remove() {
        World.removeObject(this);
    }
}
