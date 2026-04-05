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

        // Redirect client stdout/stderr to a log file so errors are visible
        // even when the launcher has no console window (jpackage install).
        try {
            java.nio.file.Path logsDir = dataDir.resolve("logs");
            Files.createDirectories(logsDir);
            String timestamp = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            java.io.File logFile = logsDir.resolve("client-" + timestamp + ".log").toFile();
            pb.redirectErrorStream(true);
            pb.redirectOutput(logFile);
            LOG.info("Client output → " + logFile.getAbsolutePath());
        } catch (Exception e) {
            LOG.warning("Could not create log file, falling back to inheritIO: " + e.getMessage());
            pb.inheritIO();
        }

        return pb.start();
    }

    // ── JVM resolution ────────────────────────────────────────────────────────

    /**
     * Resolves the java executable path.
     *
     * Priority:
     *   1. The JRE bundled by jpackage into the launcher's own install directory.
     *   2. The running JVM's java.home (the JRE used to launch this launcher).
     *   3. JAVA_HOME environment variable.
     *   4. "java" on the system PATH (last resort).
     *
     * On Windows, javaw.exe is preferred over java.exe to avoid a black console
     * window appearing behind the game client.
     */
    private static String resolveJavaExecutable() {
        boolean isWindows = OSUtils.getCurrentOS() == OSUtils.OS.WINDOWS;
        // javaw.exe on Windows: same as java.exe but no console window
        String[] candidates = isWindows ? new String[]{"javaw.exe", "java.exe"} : new String[]{"java"};

        // 1. jpackage puts the bundled JRE at <install>/runtime/bin/
        Path selfDir = selfInstallDirectory();
        if (selfDir != null) {
            Path binDir = selfDir.resolve("runtime").resolve("bin");
            for (String name : candidates) {
                Path bundled = binDir.resolve(name);
                if (Files.isExecutable(bundled)) {
                    LOG.info("Using bundled JRE: " + bundled);
                    return bundled.toAbsolutePath().toString();
                }
            }
        }

        // 2. java.home of the currently-running JVM
        String javaHome = System.getProperty("java.home");
        if (javaHome != null) {
            for (String name : candidates) {
                Path fromHome = Path.of(javaHome, "bin", name);
                if (Files.isExecutable(fromHome)) {
                    LOG.info("Using java.home JRE: " + fromHome);
                    return fromHome.toAbsolutePath().toString();
                }
            }
        }

        // 3. JAVA_HOME environment variable
        String javaHomeEnv = System.getenv("JAVA_HOME");
        if (javaHomeEnv != null && !javaHomeEnv.isBlank()) {
            for (String name : candidates) {
                Path fromEnv = Path.of(javaHomeEnv, "bin", name);
                if (Files.isExecutable(fromEnv)) {
                    LOG.info("Using JAVA_HOME: " + fromEnv);
                    return fromEnv.toAbsolutePath().toString();
                }
            }
        }

        // 4. Rely on PATH
        LOG.warning("Could not find bundled java — falling back to PATH. Client may fail to launch.");
        return isWindows ? "javaw.exe" : "java";
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
