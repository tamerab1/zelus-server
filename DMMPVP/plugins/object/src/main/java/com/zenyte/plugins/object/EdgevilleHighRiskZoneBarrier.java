//package com.zenyte.plugins.object;
//
//import com.zenyte.game.world.entity.player.Player;
//import com.zenyte.game.world.object.ObjectAction;
//import com.zenyte.game.world.object.WorldObject;
//import com.zenyte.game.world.region.area.wilderness.EdgevilleHighRiskArea;
//
//
//public class EdgevilleHighRiskZoneBarrier implements ObjectAction {
//    @Override
//    public void handleObjectAction(Player player, WorldObject object, String name, int optionId, String option) {
//
//        player.resetWalkSteps();
//
//        int currentX = player.getX();
//        int currentY = player.getY();
//        int destinationY = currentY;
//
//        if (player.getArea() instanceof EdgevilleHighRiskArea) {
//            destinationY = currentY + 1;
//        } else {
//            destinationY = currentY - 1;
//        }
//
//        player.addWalkSteps(currentX, destinationY, 1, false);
//    }
//
//    @Override
//    public Object[] getObjects() {
//        return new Object[] {41200};
//    }
//}
