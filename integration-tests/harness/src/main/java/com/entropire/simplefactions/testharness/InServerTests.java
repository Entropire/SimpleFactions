package com.entropire.simplefactions.testharness;

import static com.entropire.simplefactions.testharness.Check.that;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;

/**
 * The tests that run inside a real server. Add new tests in {@link #runAll()}.
 *
 * <p>Every test runs on the server thread (the global region thread on Folia) and catches
 * everything, including {@link LinkageError}s such as {@code NoSuchMethodError} and
 * {@code NoClassDefFoundError}: those are exactly the errors you get when the plugin calls
 * an API that a server version or server type doesn't have.
 */
final class InServerTests {

    private static final String PLUGIN_NAME = "SimpleFactions";
    static final String PLUGIN_LOADS = "plugin loads and enables";

    private final List<TestResult> results = new ArrayList<>();

    List<TestResult> runAll() {
        results.add(TestResult.info("environment", describeEnvironment()));

        boolean pluginWorks = run(PLUGIN_LOADS, this::pluginLoadsAndEnables);

        // These all need a working plugin; without one they'd only repeat the same failure.
        runIf(pluginWorks, "database file is created in the plugin folder", this::databaseFileIsCreated);
        runIf(pluginWorks, "/faction is registered with alias /f", this::factionCommandIsRegistered);
        runIf(pluginWorks, "/f create from the console says it's for players only", this::consoleCreateIsRejected);
        runIf(pluginWorks, "/f help sends the help message", this::helpMessageIsSent);
        runIf(pluginWorks, "tab completion suggests subcommands", this::tabCompletionSuggestsSubcommands);

        return results;
    }

    // ---------------------------------------------------------------- tests

    private void pluginLoadsAndEnables() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(PLUGIN_NAME);
        that(plugin != null, "SimpleFactions was not loaded at all. Common causes: the jar is compiled for a newer "
                + "Java version than this server runs on (UnsupportedClassVersionError), plugin.yml's api-version "
                + "is newer than this server, or 'folia-supported: true' is missing on Folia.");
        that(plugin.isEnabled(), "SimpleFactions loaded but isn't enabled, so onEnable() most likely threw an "
                + "exception, for example a NoSuchMethodError for an API method this server doesn't have.");
    }

    private void databaseFileIsCreated() {
        File database = new File(plugin().getDataFolder(), "Simple-Faction.db");
        that(database.isFile(), "Expected the database file at " + database.getPath());
    }

    private void factionCommandIsRegistered() {
        PluginCommand command = factionCommand();
        that(command.getPlugin() == plugin(), "/faction belongs to " + command.getPlugin().getName());
        that(command.getAliases().contains("f"), "/faction should have alias f, aliases are " + command.getAliases());
        that(command.getExecutor() != plugin(), "/faction has no executor, so startPlugin() didn't finish");
    }

    private void consoleCreateIsRejected() {
        RecordingSender sender = new RecordingSender();
        factionCommand().execute(sender.sender(), "f", new String[] {"create", "TestFaction"});
        that(sender.allText().contains("Only players can"),
                "Expected the players-only message, got: " + sender.messages());
    }

    private void helpMessageIsSent() {
        RecordingSender sender = new RecordingSender();
        factionCommand().execute(sender.sender(), "f", new String[] {"help"});
        that(!sender.messages().isEmpty(), "/f help sent no message");
        that(sender.allText().contains("/f create"), "/f help doesn't list /f create, got: " + sender.messages());
    }

    private void tabCompletionSuggestsSubcommands() {
        RecordingSender sender = new RecordingSender();
        List<String> completions = factionCommand().tabComplete(sender.sender(), "f", new String[] {"cr"});
        that(completions != null && completions.contains("create"),
                "Expected 'create' for '/f cr', got: " + completions);
    }

    // ---------------------------------------------------------------- helpers

    private static Plugin plugin() {
        return Bukkit.getPluginManager().getPlugin(PLUGIN_NAME);
    }

    private static PluginCommand factionCommand() {
        PluginCommand command = Bukkit.getPluginCommand("faction");
        that(command != null, "/faction is not registered");
        return command;
    }

    private static String describeEnvironment() {
        return Bukkit.getName() + " " + Bukkit.getVersion()
                + " (API " + Bukkit.getBukkitVersion() + ")"
                + ", Java " + System.getProperty("java.version")
                + (Scheduling.isFolia() ? ", Folia threading" : "");
    }

    private boolean run(String name, Runnable test) {
        long start = System.nanoTime();
        try {
            test.run();
            results.add(TestResult.pass(name, elapsedMillis(start)));
            return true;
        } catch (Throwable t) {
            results.add(TestResult.fail(name, elapsedMillis(start), describe(t)));
            return false;
        }
    }

    private void runIf(boolean condition, String name, Runnable test) {
        if (condition) {
            run(name, test);
        } else {
            results.add(TestResult.skip(name, "skipped because '" + PLUGIN_LOADS + "' failed"));
        }
    }

    private static long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }

    /** Assertion failures show only their message, real errors their (shortened) stack trace. */
    private static String describe(Throwable t) {
        if (t instanceof AssertionError && t.getCause() == null) return String.valueOf(t.getMessage());

        StringWriter text = new StringWriter();
        t.printStackTrace(new PrintWriter(text));
        String[] lines = text.toString().split("\\R");
        int keep = Math.min(lines.length, 40);
        StringBuilder shortened = new StringBuilder();
        for (int i = 0; i < keep; i++) shortened.append(lines[i]).append('\n');
        if (keep < lines.length) shortened.append("\t... ").append(lines.length - keep).append(" more lines");
        return shortened.toString().trim();
    }
}
