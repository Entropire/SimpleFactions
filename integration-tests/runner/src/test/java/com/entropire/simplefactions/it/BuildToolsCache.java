package com.entropire.simplefactions.it;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Spigot and CraftBukkit jars can't be downloaded, so they're built with BuildTools inside a
 * Java container. Each version is built once (both jars in one go) and kept in the cache.
 */
final class BuildToolsCache {

    private static final String BUILD_TOOLS_URL =
            "https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar";
    private static final Duration BUILD_TIMEOUT = Duration.ofMinutes(60);

    private final Path jarsDir;
    private final Path logsDir;
    private final Map<String, Object> locks = new ConcurrentHashMap<>();

    BuildToolsCache(Path cacheDir, Path logsDir) {
        this.jarsDir = cacheDir.resolve("buildtools-jars").toAbsolutePath();
        this.logsDir = logsDir;
    }

    Path jarsDir() {
        return jarsDir;
    }

    /** Returns the jar's file name inside {@link #jarsDir()}, building it first if needed. */
    String jarFor(ServerTarget target) throws IOException, InterruptedException {
        String fileName = target.type() + "-" + target.version() + ".jar";
        Path jar = jarsDir.resolve(fileName);

        synchronized (locks.computeIfAbsent(target.version(), version -> new Object())) {
            if (!Files.isRegularFile(jar)) build(target);
        }
        if (!Files.isRegularFile(jar)) {
            throw new IOException("BuildTools finished, but " + jar + " doesn't exist. "
                    + "See " + logFile(target.version()));
        }
        return fileName;
    }

    private void build(ServerTarget target) throws IOException, InterruptedException {
        Files.createDirectories(jarsDir);
        Path log = logFile(target.version());
        System.out.println("[BuildTools] Building Spigot and CraftBukkit " + target.version()
                + " (takes a few minutes, only the first time). Log: " + log);

        String script = String.join(" && ",
                "apt-get update -qq",
                "apt-get install -y -qq git curl > /dev/null",
                "mkdir -p /work && cd /work",
                "curl -fsSL -o BuildTools.jar " + BUILD_TOOLS_URL,
                "java -jar BuildTools.jar --rev " + target.version()
                        + " --compile spigot,craftbukkit --output-dir /out");

        int exitCode = Docker.runToFile(BUILD_TIMEOUT, List.of(
                "run", "--rm",
                "--mount", "type=bind,source=" + jarsDir + ",target=/out",
                "eclipse-temurin:" + target.java() + "-jdk",
                "bash", "-c", script), log);

        if (exitCode != 0) {
            throw new IOException("BuildTools failed for " + target.version() + " (exit code " + exitCode
                    + "). See " + log);
        }
    }

    private Path logFile(String version) {
        return logsDir.resolve("buildtools-" + version + ".log");
    }
}
