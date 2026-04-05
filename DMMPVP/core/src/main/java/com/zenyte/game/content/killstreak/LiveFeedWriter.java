package com.zenyte.game.content.killstreak;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermissions;
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
     * Written to {@code data/characters/livefeed.json} inside the shared Docker volume
     * ({@code game_characters}) so both the game and the website API container can access it.
     */
    private static final Path FEED_FILE = Paths.get("data/characters/livefeed.json");
    private static final java.nio.file.attribute.FileAttribute<?> PERMS_644 =
            PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-r--r--"));

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

            // ── Atomic write: temp → rename (644 so API container can read) ────
            final Path parent = FEED_FILE.getParent();
            if (parent != null) Files.createDirectories(parent);
            final Path tmp = Files.createTempFile(
                    parent != null ? parent : FEED_FILE.toAbsolutePath().getParent(),
                    "livefeed", ".tmp", PERMS_644);
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
