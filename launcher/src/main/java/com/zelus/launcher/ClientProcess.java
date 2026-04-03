package com.zelus.launcher;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Launches the game client as a child JVM process.
 *
 * Why a child process instead of Class.forName() / URLClassLoader?
 *   - Complete heap isolation: the launcher's 256 MB heap is separate from
 *     the client's heap (configurable via manifest jvm_args).
 *   - No risk of static state or classloader conflicts between launcher and client.
 *   - The launcher can stay open in the system tray (future feature) or exit cleanly.
 *   - Works even if the client JAR uses module-path or add-opens flags.
 */
public final class ClientProcess {

    private static final Logger LOG = Logger.getLogger(ClientProcess.class.getName());

    private ClientProcess() {}

    /**
     * Assembles and starts the game client JVM process.
     *
     * @param dataDir  the OS-native data directory (contains client/ and cache/)
     * @param manifest the latest patch manifest (provides JVM args, main class, server info)
     */
    public static Process launch(Path dataDir, PatchManifest manifest)
            throws IOException {

        Path clientJar = OSUtils.getClientDirectory(dataDir)
                                .resolve(manifest.client.filename);

        if (!Files.exists(clientJar)) {
            throw new IOException("Client JAR not found: " + clientJar);
        }

        // Locate the JVM binary to use — prefer the JRE bundled by jpackage
        String javaExecutable = resolveJavaExecutable();
        LOG.info("Using JVM: " + javaExecutable);

        List<String> cmd = new ArrayList<>();
        cmd.add(javaExecutable);

        // JVM flags from the manifest (heap size, GC settings, etc.)
        if (manifest.jvm_args != null) {
            cmd.addAll(manifest.jvm_args);
        }

        // Tell the client where its cache is
        cmd.add("-Dzelus.cache=" + OSUtils.getCacheDirectory(dataDir).toAbsolutePath());

        // Game server connection info
        if (manifest.game_server != null && !manifest.game_server.isBlank()) {
            cmd.add("-Dzelus.server=" + manifest.game_server);
            cmd.add("-Dzelus.port="   + manifest.game_port);
        }

        // macOS: suppress the dock icon for the child JVM (launcher owns the dock slot)
        if (OSUtils.getCurrentOS() == OSUtils.OS.MAC) {
            cmd.add("-Dapple.awt.UIElement=false");
        }

        // Classpath: just the fat client JAR
        cmd.add("-cp");
        cmd.add(clientJar.toAbsolutePath().toString());

        // Main class
        String mainClass = (manifest.main_class != null && !manifest.main_class.isBlank())
                ? manifest.main_class
                : "client.Main";
        cmd.add(mainClass);

        LOG.info("Launching client: " + String.join(" ", cmd));

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(dataDir.toFile());
        pb.inheritIO();   // forward stdout/stderr to the launcher's console

        return pb.start();
    }

    // ── JVM resolution ────────────────────────────────────────────────────────

    /**
     * Resolves the java executable path.
     *
     * Priority:
     *   1. The JRE bundled by jpackage into the launcher's own install directory.
     *   2. The running JVM's java.home (the JRE used to launch this launcher).
     *   3. "java" on the system PATH.
     *
     * This ensures that even if the user has no global Java installed, the bundled
     * JRE from jpackage is used to launch the client.
     */
    private static String resolveJavaExecutable() {
        String exeName = (OSUtils.getCurrentOS() == OSUtils.OS.WINDOWS) ? "java.exe" : "java";

        // 1. jpackage puts the bundled JRE at <install>/runtime/bin/java
        Path selfDir = selfInstallDirectory();
        if (selfDir != null) {
            Path bundled = selfDir.resolve("runtime").resolve("bin").resolve(exeName);
            if (Files.isExecutable(bundled)) {
                return bundled.toAbsolutePath().toString();
            }
        }

        // 2. java.home of the currently-running JVM
        String javaHome = System.getProperty("java.home");
        if (javaHome != null) {
            Path fromHome = Path.of(javaHome, "bin", exeName);
            if (Files.isExecutable(fromHome)) {
                return fromHome.toAbsolutePath().toString();
            }
        }

        // 3. Rely on PATH
        return "java";
    }

    /**
     * Returns the directory that contains this launcher's executable,
     * or null if it cannot be determined (e.g., running from IDE).
     */
    private static Path selfInstallDirectory() {
        try {
            Path codeSource = Path.of(
                    ClientProcess.class.getProtectionDomain()
                                       .getCodeSource()
                                       .getLocation()
                                       .toURI()
            );
            // In a jpackage install the JAR is at <install>/app/zelus-launcher.jar
            // So go up two levels: JAR → app/ → install root
            Path parent = codeSource.getParent();
            if (parent != null && parent.getFileName() != null
                    && parent.getFileName().toString().equals("app")) {
                return parent.getParent();
            }
            return parent;
        } catch (Exception e) {
            return null;
        }
    }
}
