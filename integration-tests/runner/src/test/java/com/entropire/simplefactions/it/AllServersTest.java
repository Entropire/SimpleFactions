package com.entropire.simplefactions.it;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;

/**
 * Runs the in-server tests on every server in servers.csv. In the test report every server is
 * a group (for example "paper-1.21.4") containing that server's test results.
 *
 * <p>Servers start in the background, a few at a time ({@code -Pparallel}), while the report
 * shows each server's results as soon as it's done.
 */
class AllServersTest {

    private static final String SERVER_STARTS = "server starts and runs the test harness";
    private static final String PLUGIN_LOADS = "plugin loads and enables";

    @TestFactory
    Stream<DynamicNode> allServers() throws Exception {
        Settings settings = Settings.fromSystemProperties();
        Docker.requireAvailable();

        List<ServerTarget> targets = ServerTarget.load(settings.serversFile(), settings.serverFilter());
        if (targets.isEmpty()) {
            fail("No servers in " + settings.serversFile() + " match -Pservers=" + settings.serverFilter());
        }
        System.out.println("Testing on " + targets.size() + " servers, " + settings.parallel() + " at a time.");

        Path pluginsDir = stagePlugins(settings);
        BuildToolsCache buildTools = new BuildToolsCache(settings.cacheDir(), settings.logsDir());

        ExecutorService pool = Executors.newFixedThreadPool(settings.parallel());
        List<Future<Outcome>> outcomes = targets.stream()
                .map(target -> pool.submit(() -> ServerRun.run(target, settings, pluginsDir, buildTools)))
                .toList();
        pool.shutdown();

        return Stream.iterate(0, i -> i < targets.size(), i -> i + 1)
                .map(i -> dynamicContainer(targets.get(i).id(),
                        Stream.of(outcomes.get(i)).flatMap(future -> testsFor(await(future)))));
    }

    private static Stream<DynamicNode> testsFor(Outcome outcome) {
        if (outcome.error() != null) {
            return Stream.of(dynamicTest(SERVER_STARTS, () -> fail(outcome.error())));
        }
        return Stream.concat(
                Stream.of(dynamicTest(SERVER_STARTS, () -> {})),
                outcome.results().stream().map(result -> testFor(result, outcome)));
    }

    private static DynamicNode testFor(HarnessResult result, Outcome outcome) {
        return switch (result.status()) {
            case "INFO" -> dynamicTest(result.name() + ": " + result.detail(), () -> {});
            case "PASS" -> dynamicTest(result.name(), () -> {});
            case "SKIP" -> dynamicTest(result.name(), () -> Assumptions.abort(result.detail()));
            default -> dynamicTest(result.name(), () -> fail(failureMessage(result, outcome)));
        };
    }

    /** For a plugin that didn't load, the reason is in the server log, so that goes in the failure too. */
    private static String failureMessage(HarnessResult result, Outcome outcome) {
        if (!result.name().equals(PLUGIN_LOADS)) return result.detail();
        return result.detail() + "\n\nServer log lines about SimpleFactions (full log: " + outcome.logFile() + "):\n"
                + LogFiles.linesAboutThePlugin(outcome.logFile(), 30);
    }

    /** Both jars go into one folder that every server container mounts read-only as /plugins. */
    private static Path stagePlugins(Settings settings) throws Exception {
        Path dir = settings.cacheDir().resolve("plugins-under-test");
        Files.createDirectories(dir);
        Files.copy(settings.pluginJar(), dir.resolve("SimpleFactions.jar"), StandardCopyOption.REPLACE_EXISTING);
        Files.copy(settings.harnessJar(), dir.resolve("SimpleFactionsTestHarness.jar"), StandardCopyOption.REPLACE_EXISTING);
        return dir;
    }

    private static Outcome await(Future<Outcome> future) {
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CompletionException(e);
        } catch (ExecutionException e) {
            throw new CompletionException(e.getCause());
        }
    }
}
