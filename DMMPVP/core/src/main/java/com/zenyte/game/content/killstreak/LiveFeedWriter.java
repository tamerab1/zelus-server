package com.zenyte.game.content.killstreak;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Writes real-time kill and killstreak events to {@code data/livefeed.json}.
 *
 * <p>The website API's {@code /livefeed} endpoint reads this file as its
 * primary source.  Without it the site falls back to synthetic events built
 * from character save files, which are only written on logout — so active
 * players are invisible to the live feed widget.</p>
 *
 * <p>Writes are atomic: the JSON is first written to a sibling {@code .tmp}
 * file and then renamed over the real file so the website never reads a
 * partially-written payload.</p>
 */
public final class LiveFeedWriter {

    private static final Logger log = LoggerFactory.getLogger(LiveFeedWriter.class);

    /**
     * Written inside {@code data/characters/} so that the shared Docker volume
     * ({@code game_characters}) exposes it to the API container automatically.
     * The website API looks for the file in the same characters directory it
     * already uses for player saves.
     */
    private static final Path FEED_FILE = Paths.get("data/characters/livefeed.json");

    /** Maximum number of events kept in the file; older ones are dropped. */
    private static final int MAX_EVENTS = 100;

    private static final Gson GSON = new Gson();

    private LiveFeedWriter() {}

    /**
     * Prepends a new event to the live feed file.
     *
     * @param type    one of {@code "pvp_kill"}, {@code "killstreak"}
     * @param message human-readable description shown on the website
     */
    public static void appendEvent(final String type, final String message) {
        try {
            // ── Read existing events ───────────────────────────────────────────
            JsonArray existing = new JsonArray();
            if (Files.exists(FEED_FILE)) {
                try {
                    final String raw = Files.readString(FEED_FILE);
                    final JsonArray parsed = GSON.fromJson(raw, JsonArray.class);
                    if (parsed != null) existing = parsed;
                } catch (final Exception ignored) {
                    // Corrupt or empty file — start fresh
                }
            }

            // ── Build new event ────────────────────────────────────────────────
            final JsonObject event = new JsonObject();
            event.addProperty("id",        type + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8));
            event.addProperty("type",      type);
            event.addProperty("timestamp", Instant.now().toString());
            event.addProperty("message",   message);

            // ── Prepend and cap ────────────────────────────────────────────────
            final JsonArray updated = new JsonArray();
            updated.add(event);
            for (int i = 0; i < Math.min(existing.size(), MAX_EVENTS - 1); i++) {
                updated.add(existing.get(i));
            }

            // ── Atomic write: temp → rename ────────────────────────────────────
            final Path tmp = FEED_FILE.resolveSibling("livefeed.json.tmp");
            Files.writeString(tmp, GSON.toJson(updated),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            try {
                Files.move(tmp, FEED_FILE,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (final AtomicMoveNotSupportedException e) {
                // Non-atomic fallback (should not happen on ext4/NTFS)
                Files.move(tmp, FEED_FILE, StandardCopyOption.REPLACE_EXISTING);
            }

        } catch (final IOException e) {
            log.error("Failed to write livefeed.json", e);
        }
    }
}
