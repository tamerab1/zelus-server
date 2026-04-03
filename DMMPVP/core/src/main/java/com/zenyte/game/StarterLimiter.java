package com.zenyte.game;

import com.zenyte.game.model.ui.Interface;
import com.zenyte.game.world.World;
import com.zenyte.game.world.entity.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Kris | 14/04/2019 13:54
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */

public class StarterLimiter {
    private static final Map<String, Integer> ipStarterCount = new HashMap<>();
    private static final Map<String, Integer> macStarterCount = new HashMap<>();

    public static boolean canReceiveStarter(String ip, String mac) {
        int ipCount = ipStarterCount.getOrDefault(ip, 0);
        int macCount = macStarterCount.getOrDefault(mac, 0);
        return ipCount < 2 && macCount < 2;
    }

    public static void registerStarter(String ip, String mac) {
        ipStarterCount.put(ip, ipStarterCount.getOrDefault(ip, 0) + 1);
        macStarterCount.put(mac, macStarterCount.getOrDefault(mac, 0) + 1);
    }
}

