package com.zelus.launcher;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import java.nio.file.*;
import java.util.logging.*;

/**
 * Zelus RSPS Launcher — entry point.
 *
 * Boot sequence:
 *   1. Show splash / progress window (LauncherFrame)
 *   2. Run UpdateManager on a background thread
 *   3. If update required → download + patch with progress callbacks
 *   4. Launch game client as a child JVM process (ClientProcess)
 *   5. Close the launcher window (optionally keep it open as a wrapper)
 */
public final class ZelusLauncher {

    // Remote manifest URL — change to your Cloudflare R2 / S3 bucket URL
    public static final String MANIFEST_URL =
            System.getProperty("zelus.manifest", "https://patch.zelus.gg/manifest.json");

    static final Logger LOG = Logger.getLogger("ZelusLauncher");

    public static void main(String[] args) {
        configureLogging();
        applyLookAndFeel();

        SwingUtilities.invokeLater(() -> {
            LauncherFrame frame = new LauncherFrame();
            frame.setVisible(true);
            startUpdatePipeline(frame);
        });
    }

    // ── Update pipeline ───────────────────────────────────────────────────────

    private static void startUpdatePipeline(LauncherFrame frame) {
        Thread worker = new Thread(() -> {
            try {
                Path dataDir = OSUtils.getDataDirectory();
                Files.createDirectories(dataDir);
                LOG.info("Data directory: " + dataDir);

                UpdateManager mgr = new UpdateManager(MANIFEST_URL, dataDir, frame);
                PatchManifest manifest = mgr.fetchManifest();

                frame.setStatus("Checking for updates...");
                boolean needsClientUpdate = mgr.needsClientUpdate(manifest);
                boolean needsCacheUpdate  = mgr.needsCacheUpdate(manifest);

                if (needsCacheUpdate) {
                    frame.setStatus("Downloading game cache...");
                    mgr.downloadCache(manifest);
                }

                if (needsClientUpdate) {
                    frame.setStatus("Downloading client update...");
                    mgr.downloadClient(manifest);
                }

                frame.setStatus("Launching...");
                frame.setProgress(100);

                // Small delay so the user sees "Launching..."
                Thread.sleep(600);

                ClientProcess.launch(dataDir, manifest);

                // Close launcher after client starts
                SwingUtilities.invokeLater(frame::dispose);

            } catch (Exception ex) {
                LOG.log(Level.SEVERE, "Launch failed", ex);
                SwingUtilities.invokeLater(() ->
                    frame.showError("Launch failed: " + ex.getMessage())
                );
            }
        }, "launcher-worker");

        worker.setDaemon(true);
        worker.start();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static void applyLookAndFeel() {
        try {
            FlatDarkLaf.setup();
            UIManager.put("Button.arc", 6);
            UIManager.put("Component.arc", 6);
        } catch (Exception ignored) {
            // Swing default is acceptable fallback
        }
    }

    private static void configureLogging() {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tH:%1$tM:%1$tS] [%4$s] %5$s%6$s%n");
        Logger root = Logger.getLogger("");
        root.setLevel(Level.INFO);
        for (Handler h : root.getHandlers()) {
            h.setFormatter(new SimpleFormatter());
        }
    }
}
