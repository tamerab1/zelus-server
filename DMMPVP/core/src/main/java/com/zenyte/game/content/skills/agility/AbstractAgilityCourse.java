package com.zenyte.game.content.skills.agility;

import com.zenyte.plugins.PluginPriority;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Jire
 */
@PluginPriority(5_000)
public abstract class AbstractAgilityCourse implements AgilityCourse {

    protected final ObjectSet<AgilityCourseObstacle> obstacles = new ObjectOpenHashSet<>();
    protected final Int2ObjectMap<AgilityCourseObstacle> objectToObstacleMap = new Int2ObjectOpenHashMap<>();

    @Override
    public int getObstacleCount() {
        return obstacles.size();
    }

    @Nullable
    @Override
    public AgilityCourseObstacle getObstacle(int objectID) {
        return objectToObstacleMap.get(objectID);
    }

    @Override
    public void initObstacle(@Nonnull final AgilityCourseObstacle obstacle) {
        for (final int objectID : obstacle.getObjectIds()) {
            if (objectToObstacleMap.containsKey(objectID)) {
                throw new IllegalStateException("Course " + getClass()
                        + " already contains mapping for object ID " + objectID
                        + " (" + Obstacle.class.getSimpleName() + " is " + obstacle.getClass() + ")");
            }
            objectToObstacleMap.put(objectID, obstacle);
            obstacles.add(obstacle);
        }
    }

}
