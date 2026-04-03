package com.zenyte.game.content.lootchest;

import com.zenyte.game.world.World;
import com.zenyte.game.world.object.WorldObject;
import com.zenyte.game.content.lootchest.LootChestLocation;

public class LootChest extends WorldObject {

    private final LootChestLocation location;

    public LootChest(LootChestLocation location) {
        super(29069, 10, 0, location.getX(), location.getY(), location.getZ()); // 3193 = example chest ID
        this.location = location;
    }

    public LootChestLocation getLootChestLocation() {
        return location;
    }

    public void remove() {
        World.removeObject(this);
    }
}
