package com.zenyte.game.world.entity.player.cutscene.impl;

import com.zenyte.game.GameConstants;
import com.zenyte.game.model.HintArrow;
import com.zenyte.game.model.HintArrowPosition;
import com.zenyte.game.world.entity.Location;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.game.world.entity.player.cutscene.Cutscene;
import com.zenyte.game.world.entity.player.cutscene.actions.CameraLookAction;
import com.zenyte.game.world.entity.player.cutscene.actions.CameraPositionAction;
import com.zenyte.game.world.entity.player.cutscene.actions.CameraResetAction;
import com.zenyte.game.world.entity.player.dialogue.impl.NPCChat;
import com.zenyte.plugins.renewednpc.ZenyteGuide;

/**
 * @author Kris | 26/01/2019 01:49
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
public class EdgevilleCutscene extends Cutscene {

    @Override
    public void build() {
        int time = 0;
        addActions(time+=3, () -> player.lock(), () -> player.setViewDistance(30), () -> player.getAppearance().setInvisible(true),
                () -> player.setLocation(ZenyteGuide.SPAWN_LOCATION), () -> chat(player, "Very well! Let's begin then.."));

        addActions(time+=12, () -> action(player, "Here you can find the slayer hub where you can start with your slayer tasks. There is also an upgrade rack which can be used to upgrade weapons to slay monsters faster!", 3081, 3507),
                new CameraPositionAction(player, new Location(3083, 3506), 800, 5, 10),
                new CameraLookAction(player, new Location(3078, 3510), 0, 5, 10));//

        addActions(time+=12, () -> action(player, "Over here you will find the wilderness emblem trader where you can start your PVP journey.", 3095, 3514),
                new CameraPositionAction(player, new Location(3095, 3518), 800, 5, 10),
                new CameraLookAction(player, new Location(3095, 3514, 0), 0, 5, 10));


        addActions(time+=12, () -> action(player, "In this hub you can find the vote and loyalty store and also the donator shop.", 3096, 3503),
                new CameraPositionAction(player, new Location(3096, 3499), 500, 5, 10),
                new CameraLookAction(player, new Location(3096, 3503, 0), 0, 5, 10));

        addActions(time+=12, () -> action(player, "In here you will find the <col=00080>Iron Man Tutor</col> for all your ironman related inquiries like reverting your ironman mode and claiming armour" +
                        " In here you will also find the wizard to re-design your character.", 3112, 3509),
                new CameraPositionAction(player, new Location(3104, 3508), 1000, 5, 10),
                new CameraLookAction(player, new Location(3113, 3509, 0), 0, 5, 10));//

        addActions(time+=12, () -> action(player, "And over here we have the Flower poker area of " + GameConstants.SERVER_NAME + ".", 3084, 3473),
                new CameraPositionAction(player, new Location(3087, 3466), 1000, 5, 10),
                new CameraLookAction(player, new Location(3085, 3477, 0), 0, 5, 10));

        addActions(time+=12, () -> action(player, "This is the <col=00080>" + GameConstants.SERVER_NAME + " Portal</col> which you can use to teleport all around " + GameConstants.SERVER_NAME + "!", 3084, 3504),
                new CameraPositionAction(player, new Location(3084, 3501), 300, 5, 10),
                new CameraLookAction(player, new Location(3084, 3504, 0), 0, 5, 10));

        addActions(time+=12, () -> action(player, "Oh right... we also have tournaments. There are some epic rewards available at the tournament guard", 3074, 3506),
                new CameraPositionAction(player, new Location(3074, 3510), 800, 5, 10),
                new CameraLookAction(player, new Location(3074, 3506, 0), 0, 5, 10));

        addActions(time+=12, () -> action(player, "And finally over here we have the main hub of the home area where you can find <col=00080>Bankers & Grand Exchange Clerks</col>.", 3087, 3495, HintArrowPosition.EAST),
                new CameraPositionAction(player, new Location(3083, 3500), 1000, 5, 10),//
                new CameraLookAction(player, new Location(3087, 3495, 0), 0, 5, 10), () -> player.setLocation(ZenyteGuide.HOME_ZENYTE_GUIDE));

        addActions(time+=12, () -> player.setViewDistance(15), () -> player.getPacketDispatcher().resetHintArrow(), () -> ZenyteGuide.finishTutorial(player),
                new CameraResetAction(player));
    }

    private void action(final Player player, final String message, final int x, final int y) {
        action(player, message, x, y, HintArrowPosition.CENTER);
    }

    private void action(final Player player, final String message, final int x, final int y, final HintArrowPosition position) {
        player.getPacketDispatcher().sendHintArrow(new HintArrow(x, y, (byte) 50, position));
        chat(player, message);
    }

    private void chat(final Player player, final String message) {
        player.getDialogueManager().start(new NPCChat(player, ZenyteGuide.NPC_ID, message, false));
    }
}
