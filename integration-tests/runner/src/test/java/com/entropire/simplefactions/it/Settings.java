package com.entropire.simplefactions.it;

import java.nio.file.Path;
import java.time.Duration;

/** Everything the Gradle task passes in as system properties. */
record Settings(
        Path pluginJar,
        Path harnessJar,
        Path serversFile,
        Path cacheDir,
        Path logsDir,
        String serverFilter,
        String size,
        int parallel,
        Duration startupTimeout) {

    static Settings fromSystemProperties() {
        return new Settings(
                Path.of(required("sf.pluginJar")),
                Path.of(required("sf.harnessJar")),
                Path.of(required("sf.serversFile")),
                Path.of(required("sf.cacheDir")),
                Path.of(required("sf.logsDir")),
                System.getProperty("sf.servers", ""),
                System.getProperty("sf.size", "default"),
                Math.max(1, Integer.parseInt(System.getProperty("sf.parallel", "2"))),
                Duration.ofMinutes(Long.parseLong(System.getProperty("sf.startupTimeoutMinutes", "20"))));
    }

    private static String required(String key) {
        String value = System.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("System property " + key + " is missing. "
                    + "Run the integration tests through Gradle: gradlew integrationTest");
        }
        return value;
    }
}
