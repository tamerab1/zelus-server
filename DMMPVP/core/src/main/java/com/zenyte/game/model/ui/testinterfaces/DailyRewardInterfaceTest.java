package com.zenyte.game.model.ui.testinterfaces;

import com.zenyte.game.GameInterface;
import com.zenyte.game.model.ui.Interface;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.game.world.entity.player.dialogue.Dialogue;
import com.zenyte.plugins.dialogue.PlainChat;
import com.zenyte.game.world.World;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class DailyRewardInterfaceTest extends Interface {

    private static final String LAST_REWARD_TIMESTAMP_ATTR = "lastDailyRewardTime";
    private static final String GLOBAL_IP_REWARD_TRACKER = "ipDailyRewardMap";


    @Override
    protected void attach() {
        put(14, "Claim Random Reward");
        put(15, "Claim Money Reward");
    }

    @Override
    protected void build() {

        bind("Claim Random Reward", (player, slotId, itemId, option) -> {
            if (hasClaimedWithin24Hours(player) || hasClaimedIPRecently(player)) {
                showCooldownMessage(player);
                return;
            }

            claimReward(player);
            player.getInventory().addItem(6199, 1); // mystery box
            player.getDialogueManager().start(new PlainChat(player, "You have received a Mystery Box! Come back in 24 hours."));
        });

        bind("Claim Money Reward", (player, slotId, itemId, option) -> {
            if (hasClaimedWithin24Hours(player) || hasClaimedIPRecently(player)) {
                showCooldownMessage(player);
                return;
            }


            claimReward(player);
            player.getInventory().addItem(13307, 2000); // blood money
            player.getDialogueManager().start(new PlainChat(player, "You have received 2000 Blood Money! Come back in 24 hours."));
        });
    }

    private boolean hasClaimedWithin24Hours(Player player) {
        Object timestampObj = player.getAttributes().get(LAST_REWARD_TIMESTAMP_ATTR);
        long lastClaimTime = (timestampObj instanceof Long) ? (Long) timestampObj : 0L;
        long currentTime = Instant.now().getEpochSecond();
        return (currentTime - lastClaimTime) < TimeUnit.HOURS.toSeconds(24);
    }
    @SuppressWarnings("unchecked")
    private boolean hasClaimedIPRecently(Player player) {
        String ip = player.getIP();
        if (ip == null) return false;

        long now = Instant.now().getEpochSecond();

        Object obj = World.getTemporaryAttributes().get(GLOBAL_IP_REWARD_TRACKER);

        if (!(obj instanceof java.util.Map)) {
            obj = new java.util.HashMap<String, Long>();
            World.getTemporaryAttributes().put(GLOBAL_IP_REWARD_TRACKER, obj);
        }

        java.util.Map<String, Long> ipMap = (java.util.Map<String, Long>) obj;
        Long lastTime = ipMap.get(ip);

        if (lastTime != null && (now - lastTime) < TimeUnit.HOURS.toSeconds(24)) {
            return true;
        }

        ipMap.put(ip, now); // Update de tijd
        return false;
    }


    private void claimReward(Player player) {
        player.getAttributes().put(LAST_REWARD_TIMESTAMP_ATTR, Instant.now().getEpochSecond());
    }

    private void showCooldownMessage(Player player) {
        player.getDialogueManager().start(new PlainChat(player, "You have already claimed your daily reward. Please come back in 24 hours."));
    }

    @Override
    public GameInterface getInterface() {
        return GameInterface.DAILY_REWARD; // Zorg dat je deze enumwaarde aanmaakt als die nog niet bestaat
    }
}
