package com.entropire.simplefactions.testharness.framework;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Properties;

import org.bukkit.Bukkit;

public final class PhaseData {

    public static final String FRESH = "fresh";
    public static final String RESTART = "restart";
    public static final String INCOMPATIBLE_DB = "incompatible-db";

    private static final String FILE = "phase-data.properties";

    private PhaseData() {}

    public static String currentPhase() {
        String phase = System.getenv("SF_TEST_PHASE");
        return phase == null || phase.isBlank() ? FRESH : phase;
    }

    public static void save(Properties data) throws IOException {
        File file = file();
        file.getParentFile().mkdirs();
        try (Writer writer = new FileWriter(file)) {
            data.store(writer, null);
        }
    }

    public static Properties load() throws IOException {
        File file = file();
        Check.that(file.isFile(), "No data from the previous boot at " + file.getPath()
                + ". The test that saves it in the previous phase probably failed.");
        Properties data = new Properties();
        try (Reader reader = new FileReader(file)) {
            data.load(reader);
        }
        return data;
    }

    private static File file() {
        return new File(Bukkit.getPluginManager().getPlugin("SimpleFactionsTestHarness").getDataFolder(), FILE);
    }
}
