package com.near_reality.game.content.bountyhunter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.zenyte.game.item.Item;
import com.zenyte.game.world.World;
import com.zenyte.game.world.entity.player.MessageType;
import com.zenyte.game.world.entity.player.Player;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Manages custom player-placed bounties on other players.
 * Bounties are persisted to data/bounties.json on every change.
 *
 * Key concepts:
 *   targetName (lowercase) → ActiveBounty
 *   Blood Money item ID = 13307
 */
public class BountyManager {

    // ── Constants ─────────────────────────────────────────────────────────────
    public static final int BLOOD_MONEY_ID = 13307;
    private static final Path SAVE_FILE      = Paths.get("data", "bounties.json");
    private static final Path LIVEFEED_FILE  = Paths.get("data", "livefeed.json");
    private static final int  LIVEFEED_MAX   = 50; // max events kept in the file
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final AtomicLong FEED_SEQ = new AtomicLong(System.currentTimeMillis());

    // ── State ─────────────────────────────────────────────────────────────────
    /** targetName (lowercase) → bounty */
    private static final Map<String, ActiveBounty> activeBounties = new HashMap<>();

    // ── Data class ────────────────────────────────────────────────────────────
    public static class ActiveBounty {
        public final String placerName;
        public final String placerIp;
        public final String placerMac;
        public final String targetName;
        public final int amount;

        public ActiveBounty(String placerName, String placerIp, String placerMac,
                            String targetName, int amount) {
            this.placerName  = placerName;
            this.placerIp    = placerIp;
            this.placerMac   = placerMac;
            this.targetName  = targetName;
            this.amount      = amount;
        }
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Called when a player confirms the "Place a Bounty" flow from the BH Chest.
     * Validates input, deducts blood money, registers the bounty, broadcasts.
     *
     * @return true if the bounty was successfully placed
     */
    public static boolean placeBounty(Player placer, String rawTargetName, int amount) {
        if (amount <= 0) {
            placer.sendMessage("The bounty amount must be greater than zero.");
            return false;
        }

        final String targetKey = rawTargetName.trim().toLowerCase();

        // Cannot bounty yourself
        if (targetKey.equals(placer.getUsername().toLowerCase())) {
            placer.sendMessage("You cannot place a bounty on yourself.");
            return false;
        }

        // Target must be online
        final Player target = World.getPlayerByDisplayname(rawTargetName.trim());
        if (target == null) {
            placer.sendMessage("This player is not online or does not exist.");
            return false;
        }

        // IP / MAC anti-farming check
        final String placerIp  = placer.getIP();
        final String placerMac = placer.getMACAddress();
        final String targetIp  = target.getIP();
        final String targetMac = target.getMACAddress();

        if (placerIp != null && placerIp.equals(targetIp)) {
            placer.sendMessage("You cannot place a bounty on a player sharing your connection.");
            return false;
        }
        if (placerMac != null && placerMac.equals(targetMac) && !placerMac.isEmpty()) {
            placer.sendMessage("You cannot place a bounty on a player sharing your device.");
            return false;
        }

        // Already has an active bounty — do not stack
        if (activeBounties.containsKey(targetKey)) {
            final ActiveBounty existing = activeBounties.get(targetKey);
            placer.sendMessage(target.getName() + " already has an active bounty of "
                    + existing.amount + " Blood Money.");
            return false;
        }

        // Check placer has enough blood money
        final int inInventory = placer.getInventory().getAmountOf(BLOOD_MONEY_ID);
        if (inInventory < amount) {
            placer.sendMessage("You do not have enough Blood Money. You need " + amount
                    + " but only have " + inInventory + ".");
            return false;
        }

        // Deduct blood money
        placer.getInventory().deleteItem(BLOOD_MONEY_ID, amount);

        // Register bounty
        final ActiveBounty bounty = new ActiveBounty(
                placer.getName(), placerIp, placerMac,
                target.getName(), amount);
        activeBounties.put(targetKey, bounty);
        save();
        writeLiveFeedEvent(placer.getName(), target.getName(), amount);

        // Broadcast
        final String announcement = "<img=13><shad=000000>[Bounty Hunter] "
                + placer.getName() + " has placed a bounty of <col=ff0000>"
                + amount + " Blood Money</col> on <col=ff0000>"
                + target.getName() + "</col>'s head!</shad>";
        World.sendMessage(MessageType.UNFILTERABLE, announcement);

        return true;
    }

    /**
     * Called from the PvP death hook when a player with an active bounty is killed.
     * Awards the blood money to the killer and removes the bounty.
     *
     * @param killer  the player who landed the killing blow
     * @param victim  the player who died
     */
    public static void onBountyKilled(Player killer, Player victim) {
        final String targetKey = victim.getUsername().toLowerCase();
        final ActiveBounty bounty = activeBounties.get(targetKey);
        if (bounty == null) return;

        // Anti-farming: killer must not share IP or MAC with the bounty placer
        final String killerIp  = killer.getIP();
        final String killerMac = killer.getMACAddress();

        if (killerIp != null && killerIp.equals(bounty.placerIp)) {
            // Silently ignore — don't announce or award
            activeBounties.remove(targetKey);
            save();
            killer.sendMessage("The bounty was voided due to a suspicious connection.");
            return;
        }
        if (killerMac != null && !killerMac.isEmpty() && killerMac.equals(bounty.placerMac)) {
            activeBounties.remove(targetKey);
            save();
            killer.sendMessage("The bounty was voided due to a suspicious connection.");
            return;
        }

        // Award blood money
        killer.getInventory().addOrDrop(new Item(BLOOD_MONEY_ID, bounty.amount));
        activeBounties.remove(targetKey);
        save();

        // Broadcast
        final String announcement = "<img=13><shad=000000>[Bounty Hunter] "
                + killer.getName() + " has executed <col=ff0000>"
                + victim.getName() + "</col> and claimed the <col=ff0000>"
                + bounty.amount + " Blood Money</col> reward!</shad>";
        World.sendMessage(MessageType.UNFILTERABLE, announcement);
    }

    /** Returns true if the given player (by username) has an active bounty on their head. */
    public static boolean hasBounty(Player player) {
        return activeBounties.containsKey(player.getUsername().toLowerCase());
    }

    /** Returns the active bounty for a player, or null if none exists. */
    public static ActiveBounty getBounty(Player player) {
        return activeBounties.get(player.getUsername().toLowerCase());
    }

    /** Sends the full active bounty list to a player via chat messages. */
    public static void sendBountyList(Player player) {
        if (activeBounties.isEmpty()) {
            player.sendMessage("<img=13><col=ff0000>[Bounty Hunter]</col> There are no active bounties.");
            return;
        }
        player.sendMessage("<img=13><col=ff0000>[Bounty Hunter]</col> Active bounties:");
        int index = 1;
        for (ActiveBounty bounty : activeBounties.values()) {
            player.sendMessage("  " + index + ". <col=ff0000>" + bounty.targetName
                    + "</col> — <col=ffffff>" + bounty.amount + " Blood Money</col>"
                    + " (placed by " + bounty.placerName + ")");
            index++;
        }
    }

    // ── Live feed ─────────────────────────────────────────────────────────────

    /** Appends a bounty_placed event to data/livefeed.json for the website feed. */
    private static void writeLiveFeedEvent(String placerName, String targetName, int amount) {
        try {
            // Read existing events
            List<Map<String, Object>> events = new ArrayList<>();
            if (Files.exists(LIVEFEED_FILE)) {
                try (Reader r = Files.newBufferedReader(LIVEFEED_FILE)) {
                    Type listType = new com.google.gson.reflect.TypeToken<List<Map<String, Object>>>(){}.getType();
                    List<Map<String, Object>> existing = GSON.fromJson(r, listType);
                    if (existing != null) events.addAll(existing);
                } catch (Exception ignored) {}
            }

            // Build the new event
            Map<String, Object> event = new LinkedHashMap<>();
            event.put("id",        "bounty_" + targetName.toLowerCase() + "_" + FEED_SEQ.getAndIncrement());
            event.put("type",      "bounty_placed");
            event.put("timestamp", Instant.now().toString());
            event.put("message",   placerName + " placed a " + amount
                    + " Blood Money bounty on " + targetName + "'s head!");
            events.add(0, event); // newest first

            // Cap to LIVEFEED_MAX
            if (events.size() > LIVEFEED_MAX) events = events.subList(0, LIVEFEED_MAX);

            Files.createDirectories(LIVEFEED_FILE.getParent());
            try (Writer w = Files.newBufferedWriter(LIVEFEED_FILE)) {
                GSON.toJson(events, w);
            }
        } catch (Exception e) {
            System.err.println("[BountyManager] Failed to write live feed: " + e.getMessage());
        }
    }

    // ── Persistence ───────────────────────────────────────────────────────────

    /** Load bounties from disk on server startup. */
    public static void load() {
        if (!Files.exists(SAVE_FILE)) return;
        try (Reader reader = Files.newBufferedReader(SAVE_FILE)) {
            Type type = new TypeToken<Map<String, ActiveBounty>>(){}.getType();
            Map<String, ActiveBounty> loaded = GSON.fromJson(reader, type);
            if (loaded != null) {
                activeBounties.clear();
                activeBounties.putAll(loaded);
            }
        } catch (IOException e) {
            System.err.println("[BountyManager] Failed to load bounties: " + e.getMessage());
        }
    }

    /** Save bounties to disk. Called on every change. */
    private static void save() {
        try {
            Files.createDirectories(SAVE_FILE.getParent());
            try (Writer writer = Files.newBufferedWriter(SAVE_FILE)) {
                GSON.toJson(activeBounties, writer);
            }
        } catch (IOException e) {
            System.err.println("[BountyManager] Failed to save bounties: " + e.getMessage());
        }
    }
}
