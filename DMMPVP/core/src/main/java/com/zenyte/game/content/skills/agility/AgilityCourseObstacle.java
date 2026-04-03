package com.zenyte.game.content.skills.agility;

import com.zenyte.game.world.entity.player.Player;
import com.zenyte.game.world.object.WorldObject;

import javax.annotation.Nonnull;

import static com.zenyte.game.content.skills.agility.AgilityCourseManager.COURSE_STAGE_ATTRIBUTE;

/**
 * @author Kris | 21. dets 2017 : 18:54.18
 * @author Jire
 */
public abstract class AgilityCourseObstacle implements Obstacle {

    public static final int STARTER_INDEX = 1;

    private final Class<? extends AgilityCourse> course;

    private final int index;
    private final boolean starter;

    public AgilityCourseObstacle(@Nonnull final Class<? extends AgilityCourse> course,
                                 final int index) {
        this.course = course;

        this.index = index;
        this.starter = index == STARTER_INDEX;
    }

    @Nonnull
    public Class<? extends AgilityCourse> getCourse() {
        return course;
    }

    public int updateStage(@Nonnull final Player player) {
        int stage = player.getNumericAttribute(COURSE_STAGE_ATTRIBUTE).intValue();

        int tempStage;
        if (index == 1) tempStage = 1;
        else if (stage == index - 1) tempStage = index;
        else tempStage = 0;

        player.getTemporaryAttributes().put(COURSE_STAGE_ATTRIBUTE, tempStage);

        return stage;
    }

    public int getIndex() {
        return index;
    }

    public boolean isStarter() {
        return starter;
    }

    @Override
    public void handleObjectAction(final Player player, final WorldObject object, final String name,
                                   final int optionId, final String option) {
        final AgilityCourse course = AgilityCourseManager.getCourse(this);

        if (isStarter()) {
            player.addAttribute(COURSE_STAGE_ATTRIBUTE, 0);
        }
        course.runCourseObstacle(player, object, this);
    }

    @Override
    public void init() {
        final AgilityCourse course = AgilityCourseManager.getCourse(this);
        course.initObstacle(this);
    }

}
