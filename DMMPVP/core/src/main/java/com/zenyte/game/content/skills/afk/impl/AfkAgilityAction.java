package com.zenyte.game.content.skills.afk.impl;

import com.zenyte.game.content.skills.afk.BasicAfkAction;
import com.zenyte.game.world.entity.masks.Animation;
import com.zenyte.game.world.entity.masks.RenderAnimation;
import com.zenyte.game.world.entity.player.SkillConstants;

public class AfkAgilityAction extends BasicAfkAction {


    //			player.lock();
    //			player.addFreezeImmunity(getDelay());
    //			player.getTemporaryAttributes().put("courseRun", player.isRun());
    //			player.setRunSilent(true);


    private static final RenderAnimation RENDER = new RenderAnimation(RenderAnimation.STAND, 762, 762);

    @Override
    public boolean start() {
        if(!super.check())
            return false;

        player.getTemporaryAttributes().put("courseRun", player.isRun());
        player.setRunSilent(true);
        return true;
    }


    public boolean getiDirection() {
        int x = player.getLocation().getX();
        return x == 3106; // Als de speler op 3106 staat, moet hij naar 3108 lopen
    }


    @Override
    public int processWithDelay() {
        super.processWithDelay();
        player.getAppearance().setRenderAnimation(RENDER);

        if (getiDirection()) {
            player.addWalkSteps(3109, 3498, -1, false); // Loop naar 3108
        } else {
            player.addWalkSteps(3106, 3498, -1, false); // Loop naar 3106
        }

        return 3; // Herhaal elke 3 ticks
    }

    @Override
    public void stop() {
        super.stop();
        player.setRunSilent(false);
        player.getAppearance().resetRenderAnimation();
    }

    @Override
    public Animation actionAnimation() {
        return null;
    }

    @Override
    public int getSkill() {
        return SkillConstants.AGILITY;
    }

    @Override
    public String getMessage() {
        return "You cross the log";
    }

    @Override
    public boolean hasRequiredItem() {
        return true;
    }
}
