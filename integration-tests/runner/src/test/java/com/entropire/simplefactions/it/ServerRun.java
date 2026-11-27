package com.entropire.simplefactions.it;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * Starts one server in Docker (itzg/minecraft-server) once per {@link Phase}, waits for the
 * harness' results each time and removes the container again.
 *
 * <p>The server's /data folder is kept in .cache/servers/&lt;id&gt; between runs, so the server
 * jar, Mojang's files and libraries are only downloaded once. Worlds, logs and plugins are
 * deleted before every run, so each run starts clean.
 */
final class ServerRun {

    static final String RESULTS_FILE = "plugins/SimpleFactionsTestHarness/sf-test-results.tsv";
    static final String SERVER_RESTARTS = "server restarts and runs the test harness";
    /** How long the harness gets to report after the server logged "Done". */
    private static final Duration HARNESS_GRACE = Duration.ofMinutes(2);
    private static final List<String> FOLDERS_TO_CLEAN =
            List.of("plugins", "world", "world_nether", "world_the_end", "logs", "crash-reports");

    private static final Set<String> RUNNING = ConcurrentHashMap.newKeySet();

    static {
        // Ctrl+C or a cancelled Gradle run shouldn't leave servers running in the background.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> RUNNING.forEach(Docker::removeQuietly)));
    }

    private ServerRun() {}

    static Outcome run(ServerTarget target, Settings settings, Path pluginsDir, BuildToolsCache buildTools) {
        Path logFile = logFile(settings, target, Phase.FRESH);
        Instant start = Instant.now();
        System.out.println("[" + target.id() + "] starting");

        try {
            Path dataDir = settings.cacheDir().resolve("servers").resolve(target.id()).toAbsolutePath();
            prepareDataDir(dataDir);

            List<HarnessResult> results = new ArrayList<>();
            for (Phase phase : Phase.values()) {
                Outcome outcome = runPhase(target, settings, pluginsDir, buildTools, dataDir, phase);

                if (outcome.error() != null) {
                    if (phase == Phase.FRESH) return finish(target, start, outcome);
                    results.add(new HarnessResult("FAIL", phase.label + SERVER_RESTARTS, 0, outcome.error()));
                    break;
                }
                if (phase != Phase.FRESH) results.add(new HarnessResult("PASS", phase.label + SERVER_RESTARTS, 0, ""));
                for (HarnessResult result : outcome.results()) {
                    if (phase != Phase.FRESH && result.status().equals("INFO")) continue;
                    results.add(phase == Phase.FRESH ? result : result.withName(phase.label + result.name()));
                }
            }
            return finish(target, start, Outcome.finished(target, results, logFile));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Outcome.failed(target, "Interrupted", logFile);
        } catch (Exception e) {
            return Outcome.failed(target, "Could not run the server: " + e, logFile);
        }
    }

    private static Outcome finish(ServerTarget target, Instant start, Outcome outcome) {
        System.out.println("[" + target.id() + "] " + (outcome.error() == null ? "finished" : "FAILED")
                + " after " + Duration.between(start, Instant.now()).toSeconds() + "s");
        return outcome;
    }

    private static Outcome runPhase(ServerTarget target, Settings settings, Path pluginsDir,
            BuildToolsCache buildTools, Path dataDir, Phase phase) throws IOException, InterruptedException {

        String container = "sf-it-" + target.id();
        Path logFile = logFile(settings, target, phase);
        Files.deleteIfExists(dataDir.resolve(RESULTS_FILE));
        Files.deleteIfExists(dataDir.resolve("logs/latest.log"));
        if (phase != Phase.FRESH) System.out.println("[" + target.id() + "] " + phase.id);

        try {
            return runServer(target, settings, pluginsDir, buildTools, container, dataDir, phase, logFile);
        } finally {
            saveLogs(container, logFile);
            Docker.removeQuietly(container);
            RUNNING.remove(container);
        }
    }

    private static Outcome runServer(ServerTarget target, Settings settings, Path pluginsDir, BuildToolsCache buildTools,
            String container, Path dataDir, Phase phase, Path logFile) throws IOException, InterruptedException {

        List<String> args = new ArrayList<>(List.of(
                "run", "-d", "--name", container,
                "--mount", "type=bind,source=" + dataDir + ",target=/data",
                "--mount", "type=bind,source=" + pluginsDir.toAbsolutePath() + ",target=/plugins,readonly",
                "-e", "EULA=TRUE",
                "-e", "TYPE=" + target.imageType(),
                "-e", "VERSION=" + target.version(),
                "-e", "ONLINE_MODE=FALSE",
                "-e", "MEMORY=1536M",
                "-e", "VIEW_DISTANCE=3",
                "-e", "SIMULATION_DISTANCE=3",
                "-e", "SPAWN_PROTECTION=0",
                "-e", "GENERATE_STRUCTURES=false",
                "-e", "SF_TEST_PHASE=" + phase.id));
        args.addAll(Docker.hostUserArgs());

        if (target.needsBuildTools()) {
            String jar = buildTools.jarFor(target);
            args.addAll(List.of(
                    "--mount", "type=bind,source=" + buildTools.jarsDir() + ",target=/jars,readonly",
                    "-e", "CUSTOM_SERVER=/jars/" + jar));
        }
        args.add("itzg/minecraft-server:java" + target.java());

        Docker.removeQuietly(container);
        RUNNING.add(container);
        Docker.Result started = Docker.run(Duration.ofMinutes(15), args);
        if (!started.ok()) {
            return Outcome.failed(target, "docker run failed:\n" + started.output(), logFile);
        }

        return waitForResults(target, settings, container, dataDir, logFile);
    }

    private static Outcome waitForResults(ServerTarget target, Settings settings, String container,
            Path dataDir, Path logFile) throws IOException, InterruptedException {

        Path results = dataDir.resolve(RESULTS_FILE);
        Path latestLog = dataDir.resolve("logs/latest.log");
        Instant deadline = Instant.now().plus(settings.startupTimeout());
        Instant serverDoneAt = null;

        while (!Files.isRegularFile(results)) {
            if (!Docker.isRunning(container)) {
                if (Files.isRegularFile(results)) break;
                return failure(target, "The server stopped before the test harness reported any results. "
                        + "It probably crashed or couldn't be downloaded or started.", logFile, container);
            }
            if (serverDoneAt == null && contains(latestLog, "Done (")) {
                serverDoneAt = Instant.now();
            }
            if (serverDoneAt != null && Instant.now().isAfter(serverDoneAt.plus(HARNESS_GRACE))) {
                return failure(target, "The server started, but the test harness didn't report results within "
                        + HARNESS_GRACE.toMinutes() + " minutes. The harness plugin probably failed to load.",
                        logFile, container);
            }
            if (Instant.now().isAfter(deadline)) {
                return failure(target, "The server didn't finish starting within " + settings.startupTimeout().toMinutes()
                        + " minutes. The first run downloads a lot; raise it with -PstartupTimeoutMinutes=...",
                        logFile, container);
            }
            Thread.sleep(2000);
        }

        return Outcome.finished(target, HarnessResult.parse(Files.readAllLines(results, StandardCharsets.UTF_8)), logFile);
    }

    private static Path logFile(Settings settings, ServerTarget target, Phase phase) {
        String suffix = phase == Phase.FRESH ? "" : "-" + phase.id;
        return settings.logsDir().resolve(target.id() + suffix + ".log").toAbsolutePath();
    }

    private static Outcome failure(ServerTarget target, String message, Path logFile, String container) {
        saveLogs(container, logFile);
        return Outcome.failed(target, message + "\n\nLast lines of the server log (" + logFile + "):\n"
                + LogFiles.tail(logFile, 40), logFile);
    }

    private static void prepareDataDir(Path dataDir) throws IOException {
        Files.createDirectories(dataDir);
        for (String folder : FOLDERS_TO_CLEAN) deleteRecursively(dataDir.resolve(folder));
    }

    private static void deleteRecursively(Path path) throws IOException {
        if (!Files.exists(path)) return;
        try (Stream<Path> files = Files.walk(path)) {
            for (Path file : files.sorted(Comparator.reverseOrder()).toList()) Files.delete(file);
        } catch (IOException e) {
            throw new IOException("Could not clean up " + path + " before starting the server. On Linux, delete "
                    + "the .cache/servers folder once with sudo; later runs create files as your own user.", e);
        }
    }

    private static boolean contains(Path file, String text) {
        try {
            return Files.isRegularFile(file)
                    && new String(Files.readAllBytes(file), StandardCharsets.ISO_8859_1).contains(text);
        } catch (IOException e) {
            return false;
        }
    }

    private static void saveLogs(String container, Path logFile) {
        try {
            Docker.runToFile(Duration.ofMinutes(1), List.of("logs", container), logFile);
        } catch (IOException | InterruptedException ignored) {
            // The container may already be gone; then the previous copy of the log stays.
        }
    }
}
