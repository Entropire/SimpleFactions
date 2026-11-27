package com.entropire.simplefactions.testharness.suites;

import static com.entropire.simplefactions.testharness.framework.Suite.test;

import java.io.File;
import java.util.List;

import org.bukkit.plugin.Plugin;

import com.entropire.simplefactions.testharness.framework.Check;
import com.entropire.simplefactions.testharness.framework.SimpleFactionsPlugin;
import com.entropire.simplefactions.testharness.framework.Suite;
import com.entropire.simplefactions.testharness.framework.TestCase;

public final class PluginLifecycleSuite implements Suite {

    public static final TestCase PLUGIN_LOADS = test("plugin loads and enables", PluginLifecycleSuite::pluginLoadsAndEnables);

    @Override
    public String name() {
        return "Plugin";
    }

    @Override
    public List<TestCase> tests() {
        return List.of(test("database file is created in the plugin folder", this::databaseFileIsCreated));
    }

    private static void pluginLoadsAndEnables() {
        Plugin plugin = SimpleFactionsPlugin.plugin();
        Check.that(plugin != null, "SimpleFactions was not loaded at all. Common causes: the jar is compiled for a newer "
                + "Java version than this server runs on (UnsupportedClassVersionError), plugin.yml's api-version "
                + "is newer than this server, or 'folia-supported: true' is missing on Folia.");
        Check.that(plugin.isEnabled(), "SimpleFactions loaded but isn't enabled, so onEnable() most likely threw an "
                + "exception, for example a NoSuchMethodError for an API method this server doesn't have.");
    }

    private void databaseFileIsCreated() {
        File database = SimpleFactionsPlugin.databaseFile();
        Check.that(database.isFile(), "Expected the database file at " + database.getPath());
    }
}
