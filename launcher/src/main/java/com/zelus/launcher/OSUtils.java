package com.zelus.launcher;

import java.io.*;
import java.nio.file.*;
import java.util.logging.Logger;
import java.util.zip.*;

/**
 * OS detection and platform-specific path resolution.
 *
 * Data directory layout:
 *   Windows  → %USERPROFILE%\.zelus\
 *   macOS    → ~/Library/Application Support/Zelus/
 *   Linux    → ~/.zelus/
 *
 * Inside the data directory:
 *   cache/        ← extracted game cache (unzipped by UpdateManager)
 *   client/       ← downloaded client JAR(s)
 *   version.json  ← locally-cached last-known version info
 */
public final class OSUtils {

    private static final Logger LOG = Logger.getLogger(OSUtils.class.getName());

    /** Detected operating system. */
    public enum OS { WINDOWS, MAC, LINUX, UNKNOWN }

    private static final OS CURRENT_OS = detect();

    private OSUtils() {}

    // ── OS detection ──────────────────────────────────────────────────────────

    public static OS getCurrentOS() {
        return CURRENT_OS;
    }

    private static OS detect() {
        String name = System.getProperty("os.name", "").toLowerCase();
        if (name.contains("win"))    return OS.WINDOWS;
        if (name.contains("mac"))    return OS.MAC;
        if (name.contains("nux") || name.contains("nix") || name.contains("aix"))
                                     return OS.LINUX;
        return OS.UNKNOWN;
    }

    // ── Data directory ────────────────────────────────────────────────────────

    /**
     * Returns the OS-appropriate base data directory for Zelus.
     * The directory is NOT created here — caller must call Files.createDirectories().
     */
    public static Path getDataDirectory() {
        return switch (CURRENT_OS) {
            case WINDOWS -> {
                String profile = System.getenv("USERPROFILE");
                if (profile == null) profile = System.getProperty("user.home");
                yield Path.of(profile, ".zelus");
            }
            case MAC -> Path.of(System.getProperty("user.home"),
                    "Library", "Application Support", "Zelus");
            default  -> Path.of(System.getProperty("user.home"), ".zelus");
        };
    }

    /** Returns the cache sub-directory inside the data directory. */
    public static Path getCacheDirectory(Path dataDir) {
        return dataDir.resolve("cache");
    }

    /** Returns the client JAR directory inside the data directory. */
    public static Path getClientDirectory(Path dataDir) {
        return dataDir.resolve("client");
    }

    // ── Zip extraction ────────────────────────────────────────────────────────

    /**
     * Extracts a zip archive into {@code destDir}.
     * Includes Zip Slip protection — entries that resolve outside destDir are skipped.
     *
     * @param zipFile   path to the .zip file to extract
     * @param destDir   destination directory (will be created if absent)
     * @param listener  called with (bytesExtracted, totalBytes) for progress reporting
     */
    public static void extractZip(Path zipFile, Path destDir, ProgressListener listener)
            throws IOException {
        Files.createDirectories(destDir);
        Path canonicalDest = destDir.toRealPath();

        try (ZipInputStream zis = new ZipInputStream(
                new BufferedInputStream(Files.newInputStream(zipFile), 64 * 1024))) {

            long totalBytes   = Files.size(zipFile);  // rough progress denominator
            long bytesWritten = 0L;
            byte[] buf = new byte[64 * 1024];
            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                // Zip Slip protection
                Path target = destDir.resolve(entry.getName()).normalize();
                if (!target.startsWith(canonicalDest)) {
                    LOG.warning("Zip Slip attempt blocked: " + entry.getName());
                    zis.closeEntry();
                    continue;
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(target);
                } else {
                    Files.createDirectories(target.getParent());
                    try (OutputStream out = new BufferedOutputStream(
                            Files.newOutputStream(target), 64 * 1024)) {
                        int len;
                        while ((len = zis.read(buf)) > 0) {
                            out.write(buf, 0, len);
                            bytesWritten += len;
                            if (listener != null) {
                                listener.onProgress(bytesWritten, totalBytes);
                            }
                        }
                    }
                }
                zis.closeEntry();
            }
        }
    }

    /** Progress callback used during extraction and downloads. */
    @FunctionalInterface
    public interface ProgressListener {
        void onProgress(long bytesProcessed, long totalBytes);
    }
}
