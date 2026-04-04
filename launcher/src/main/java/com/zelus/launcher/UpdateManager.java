package com.zelus.launcher;

import com.google.gson.Gson;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.nio.file.*;
import java.security.*;
import java.time.Duration;
import java.util.HexFormat;
import java.util.logging.Logger;

/**
 * Core auto-updater logic.
 *
 * Responsibilities:
 *   1. Fetch manifest.json from the patch server.
 *   2. Compare local SHA-256 hashes against the manifest.
 *   3. Download missing or outdated files with live progress reporting.
 *   4. Verify SHA-256 after download (discard corrupted files).
 *   5. Extract cache zip to the OS-native cache directory.
 *   6. Persist a local version file so the next launch is fast.
 */
public final class UpdateManager {

    private static final Logger LOG = Logger.getLogger(UpdateManager.class.getName());
    private static final Gson   GSON = new Gson();

    /** Cloudflare R2 / S3 read timeout. Large caches can be slow on first download. */
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(15);
    private static final Duration READ_TIMEOUT    = Duration.ofMinutes(30);

    private final String        manifestUrl;
    private final Path          dataDir;
    private final LauncherFrame frame;          // for progress callbacks
    private final HttpClient    http;

    public UpdateManager(String manifestUrl, Path dataDir, LauncherFrame frame) {
        this.manifestUrl = manifestUrl;
        this.dataDir     = dataDir;
        this.frame       = frame;
        this.http        = HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    // ── Manifest ──────────────────────────────────────────────────────────────

    /**
     * Fetches and parses the remote manifest.json.
     * Throws {@link IOException} if the server is unreachable.
     */
    public PatchManifest fetchManifest() throws IOException, InterruptedException {
        LOG.info("Fetching manifest: " + manifestUrl);
        frame.setStatus("Connecting to patch server...");

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(manifestUrl))
                .timeout(Duration.ofSeconds(20))
                .header("User-Agent", "ZelusLauncher/" + getClass().getPackage().getImplementationVersion())
                .GET()
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            throw new IOException("Manifest server returned HTTP " + resp.statusCode());
        }

        PatchManifest manifest = GSON.fromJson(resp.body(), PatchManifest.class);
        if (manifest == null || manifest.client == null) {
            throw new IOException("Manifest is malformed or incomplete.");
        }
        LOG.info("Manifest OK — client=" + manifest.client + "  cache=" + manifest.cache);
        return manifest;
    }

    // ── Version comparison ────────────────────────────────────────────────────

    public boolean needsClientUpdate(PatchManifest manifest) throws IOException {
        Path jar = OSUtils.getClientDirectory(dataDir).resolve(manifest.client.filename);
        return !fileMatchesHash(jar, manifest.client.sha256);
    }

    public boolean needsCacheUpdate(PatchManifest manifest) throws IOException {
        // We compare the cache version stored in a small sentinel file,
        // not the zip itself (the zip is deleted after extraction).
        Path sentinel = OSUtils.getCacheDirectory(dataDir).resolve(".cache_version");
        if (!Files.exists(sentinel)) return true;
        String localVersion = Files.readString(sentinel).trim();
        return !localVersion.equals(manifest.cache.version);
    }

    // ── Downloads ─────────────────────────────────────────────────────────────

    public void downloadClient(PatchManifest manifest) throws IOException, InterruptedException {
        Path destDir = OSUtils.getClientDirectory(dataDir);
        Files.createDirectories(destDir);
        Path dest = destDir.resolve(manifest.client.filename);

        LOG.info("Downloading client: " + manifest.client.url);
        downloadWithProgress(manifest.client.url, dest, manifest.client.size_bytes, "client JAR");
        verifyHash(dest, manifest.client.sha256, "client JAR");
    }

    public void downloadCache(PatchManifest manifest) throws IOException, InterruptedException {
        Path tmpZip = dataDir.resolve("cache-download.zip");

        LOG.info("Downloading cache: " + manifest.cache.url);
        downloadWithProgress(manifest.cache.url, tmpZip, manifest.cache.size_bytes, "game cache");
        verifyHash(tmpZip, manifest.cache.sha256, "game cache");

        Path cacheDir = OSUtils.getCacheDirectory(dataDir);
        LOG.info("Extracting cache to: " + cacheDir);
        frame.setStatus("Extracting game cache...");

        OSUtils.extractZip(tmpZip, cacheDir, (done, total) -> {
            int pct = total > 0 ? (int) (done * 100L / total) : 0;
            SwingUtilsThunk.runOnEDT(() -> frame.setProgress(50 + pct / 2));
        });

        // Write sentinel so we don't re-download the same cache version next launch
        Files.writeString(cacheDir.resolve(".cache_version"), manifest.cache.version);

        // Delete the zip to free disk space
        Files.deleteIfExists(tmpZip);
        LOG.info("Cache extraction complete.");
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    /**
     * Streams a URL to a local file, updating the progress bar while downloading.
     * Uses Java 11+ HttpClient with BodyHandlers for efficient streaming.
     */
    private void downloadWithProgress(String url, Path dest,
                                      long expectedBytes, String label)
            throws IOException, InterruptedException {

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(READ_TIMEOUT)
                .header("User-Agent", "ZelusLauncher")
                .GET()
                .build();

        // Stream directly to file; read body as InputStream for progress tracking
        HttpResponse<InputStream> resp =
                http.send(req, HttpResponse.BodyHandlers.ofInputStream());

        if (resp.statusCode() != 200) {
            throw new IOException("Download of " + label + " failed: HTTP " + resp.statusCode());
        }

        long contentLength = resp.headers().firstValueAsLong("Content-Length")
                                 .orElse(expectedBytes);

        try (InputStream in  = new BufferedInputStream(resp.body(), 128 * 1024);
             OutputStream out = new BufferedOutputStream(
                     Files.newOutputStream(dest,
                             StandardOpenOption.CREATE,
                             StandardOpenOption.TRUNCATE_EXISTING), 128 * 1024)) {

            byte[] buf  = new byte[128 * 1024];
            long   done = 0L;
            int    len;
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
                done += len;
                final long downloaded = done;
                final long total      = contentLength;
                SwingUtilsThunk.runOnEDT(() -> {
                    int pct = total > 0 ? (int) (downloaded * 50L / total) : 0;
                    frame.setProgress(pct);
                    frame.setStatus(String.format("Downloading %s… %.1f / %.1f MB",
                            label,
                            downloaded / 1_048_576.0,
                            total      / 1_048_576.0));
                });
            }
        }
        LOG.info("Download complete: " + dest);
    }

    /**
     * Computes the SHA-256 of {@code file} and compares it to {@code expected}.
     * Deletes the file and throws if the hash does not match.
     */
    private static void verifyHash(Path file, String expected, String label) throws IOException {
        if (expected == null || expected.isBlank()) {
            LOG.warning("No SHA-256 in manifest for " + label + " — skipping verification.");
            return;
        }
        String actual = sha256Hex(file);
        if (!actual.equalsIgnoreCase(expected)) {
            Files.deleteIfExists(file);
            throw new IOException(
                "SHA-256 mismatch for " + label + "!\n" +
                "  Expected: " + expected + "\n" +
                "  Actual:   " + actual   + "\n" +
                "The downloaded file was deleted. Please try again."
            );
        }
        LOG.info("SHA-256 verified OK for " + label);
    }

    /**
     * Returns true if {@code file} exists and its SHA-256 matches {@code expectedHex}.
     */
    private static boolean fileMatchesHash(Path file, String expectedHex) throws IOException {
        if (!Files.exists(file)) return false;
        if (expectedHex == null || expectedHex.isBlank()) return true;
        return sha256Hex(file).equalsIgnoreCase(expectedHex);
    }

    /** Computes the SHA-256 hex digest of a file. */
    private static String sha256Hex(Path file) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            try (InputStream in = new BufferedInputStream(Files.newInputStream(file), 128 * 1024)) {
                byte[] buf = new byte[128 * 1024];
                int len;
                while ((len = in.read(buf)) != -1) md.update(buf, 0, len);
            }
            return HexFormat.of().formatHex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    // ── EDT helper shim ───────────────────────────────────────────────────────

    /** Avoids importing javax.swing.* inside this class. */
    private static final class SwingUtilsThunk {
        static void runOnEDT(Runnable r) {
            if (javax.swing.SwingUtilities.isEventDispatchThread()) r.run();
            else javax.swing.SwingUtilities.invokeLater(r);
        }
    }
}
