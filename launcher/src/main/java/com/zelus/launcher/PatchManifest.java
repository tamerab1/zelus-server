package com.zelus.launcher;

import java.util.List;

/**
 * POJO that maps to the remote manifest.json served from the patch server.
 *
 * Example manifest.json:
 * {
 *   "launcher_version": "1.0.0",
 *   "client": {
 *     "version":    "1.0.0",
 *     "filename":   "zelus-client-1.0.0.jar",
 *     "url":        "https://patch.zelus.gg/releases/zelus-client-1.0.0.jar",
 *     "sha256":     "abc123...",
 *     "size_bytes": 45678901
 *   },
 *   "cache": {
 *     "version":    "1",
 *     "filename":   "cache-1.zip",
 *     "url":        "https://patch.zelus.gg/cache/cache-1.zip",
 *     "sha256":     "def456...",
 *     "size_bytes": 256000000
 *   },
 *   "jvm_args":    ["-Xmx512m", "-Xms256m", "-Dfile.encoding=UTF-8"],
 *   "main_class":  "client.Main",
 *   "game_server": "zelus.gg",
 *   "game_port":   43594
 * }
 */
public final class PatchManifest {

    public String     launcher_version;
    public FileEntry  client;
    public FileEntry  cache;
    public List<String> jvm_args;
    public String     main_class;
    public String     game_server;
    public int        game_port = 43594;

    /** Describes a single downloadable artifact (client JAR or cache zip). */
    public static final class FileEntry {
        public String version;
        public String filename;
        public String url;
        public String sha256;
        public long   size_bytes;

        @Override
        public String toString() {
            return filename + " v" + version + " (" + (size_bytes / 1_048_576) + " MB)";
        }
    }

    /** Human-readable total download size. */
    public String pendingDownloadSummary(boolean needsClient, boolean needsCache) {
        long bytes = 0;
        if (needsClient && client != null) bytes += client.size_bytes;
        if (needsCache  && cache  != null) bytes += cache.size_bytes;
        if (bytes == 0) return "No downloads required.";
        return String.format("Downloading %.1f MB...", bytes / 1_048_576.0);
    }
}
