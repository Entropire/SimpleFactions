package com.entropire.simplefactions.it;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Runs the docker command line tool. Output always goes to a file, so a full pipe can't block docker. */
final class Docker {

    private Docker() {}

    record Result(int exitCode, String output) {
        boolean ok() {
            return exitCode == 0;
        }
    }

    static Result run(Duration timeout, List<String> args) throws IOException, InterruptedException {
        Path output = Files.createTempFile("sf-docker-", ".log");
        try {
            int exitCode = runToFile(timeout, args, output);
            return new Result(exitCode, Files.readString(output, StandardCharsets.UTF_8).trim());
        } finally {
            Files.deleteIfExists(output);
        }
    }

    static int runToFile(Duration timeout, List<String> args, Path outputFile) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add("docker");
        command.addAll(args);

        Files.createDirectories(outputFile.toAbsolutePath().getParent());
        Process process = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .redirectOutput(outputFile.toFile())
                .start();

        if (!process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
            process.destroyForcibly();
            throw new IOException("'docker " + String.join(" ", args) + "' didn't finish within " + timeout);
        }
        return process.exitValue();
    }

    static void requireAvailable() {
        try {
            Result result = run(Duration.ofSeconds(30), List.of("version", "--format", "{{.Server.Version}}"));
            if (!result.ok()) {
                throw new IllegalStateException("Docker is installed but not running. Start Docker Desktop and try again.\n"
                        + result.output());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not run 'docker'. Install Docker Desktop and make sure "
                    + "'docker' works in a terminal.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    static boolean isRunning(String container) throws IOException, InterruptedException {
        Result result = run(Duration.ofSeconds(30), List.of("inspect", "-f", "{{.State.Running}}", container));
        return result.ok() && result.output().equals("true");
    }

    static void removeQuietly(String container) {
        try {
            run(Duration.ofSeconds(60), List.of("rm", "-f", container));
        } catch (IOException | InterruptedException ignored) {
            // Best effort: it may not exist.
        }
    }

    /** On Linux, files in bind mounts should belong to you, not to the container's user. */
    static List<String> hostUserArgs() {
        if (System.getProperty("os.name", "").toLowerCase().contains("win")) return List.of();
        try {
            String uid = new String(new ProcessBuilder("id", "-u").start().getInputStream().readAllBytes()).trim();
            String gid = new String(new ProcessBuilder("id", "-g").start().getInputStream().readAllBytes()).trim();
            if (uid.isEmpty() || gid.isEmpty()) return List.of();
            return List.of("-e", "UID=" + uid, "-e", "GID=" + gid);
        } catch (IOException e) {
            return List.of();
        }
    }
}
