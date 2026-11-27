package com.entropire.simplefactions.testharness;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Runs the in-server tests shortly after the server has fully started, writes the results
 * to {@value #RESULTS_FILE} in this plugin's data folder and then stops the server.
 *
 * <p>Set the environment variable {@code SF_TEST_SHUTDOWN=false} to keep the server running
 * after the tests, for example when you start a test server by hand to look around.
 */
public final class TestHarnessPlugin extends JavaPlugin {

    static final String RESULTS_FILE = "sf-test-results.tsv";
    private static final long START_DELAY_TICKS = 40L;

    @Override
    public void onEnable() {
        getLogger().info("[SF-TEST] Test harness loaded, the tests start once the server is running.");
        Scheduling.runLater(this, this::runTestsAndReport, START_DELAY_TICKS);
    }

    private void runTestsAndReport() {
        List<TestResult> results = new InServerTests().runAll();

        for (TestResult result : results) {
            getLogger().info("[SF-TEST] " + result.status() + "  " + result.name()
                    + (result.detail().isEmpty() ? "" : "  -  " + firstLine(result.detail())));
        }

        try {
            writeResults(results);
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "[SF-TEST] Could not write the results file", e);
        }

        long failed = results.stream().filter(r -> r.status() == TestResult.Status.FAIL).count();
        long passed = results.stream().filter(r -> r.status() == TestResult.Status.PASS).count();
        long skipped = results.stream().filter(r -> r.status() == TestResult.Status.SKIP).count();
        getLogger().info("[SF-TEST] FINISHED: " + passed + " passed, " + failed + " failed, " + skipped + " skipped");

        if (!"false".equalsIgnoreCase(System.getenv("SF_TEST_SHUTDOWN"))) {
            Bukkit.shutdown();
        }
    }

    /** Writes to a temporary file first, so the runner never reads a half-written file. */
    private void writeResults(List<TestResult> results) throws IOException {
        Path folder = getDataFolder().toPath();
        Files.createDirectories(folder);
        Path target = folder.resolve(RESULTS_FILE);
        Path temp = folder.resolve(RESULTS_FILE + ".tmp");

        StringBuilder content = new StringBuilder();
        for (TestResult result : results) content.append(result.toLine()).append('\n');
        Files.write(temp, content.toString().getBytes(StandardCharsets.UTF_8));

        try {
            Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static String firstLine(String text) {
        int end = text.indexOf('\n');
        return end < 0 ? text : text.substring(0, end);
    }
}
