package com.entropire.simplefactions.it;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

final class LogFiles {

    private LogFiles() {}

    static String tail(Path logFile, int lines) {
        List<String> all = read(logFile);
        return String.join("\n", all.subList(Math.max(0, all.size() - lines), all.size()));
    }

    /** Lines about SimpleFactions itself (not the harness), plus typical load errors. */
    static String linesAboutThePlugin(Path logFile, int max) {
        return read(logFile).stream()
                .filter(line -> (line.contains("SimpleFactions") && !line.contains("SimpleFactionsTestHarness")
                                && !line.contains("[SF-TEST]"))
                        || line.contains("UnsupportedClassVersionError")
                        || line.contains("Unsupported API version")
                        || line.contains("folia-supported"))
                .limit(max)
                .collect(Collectors.joining("\n"));
    }

    private static List<String> read(Path logFile) {
        try {
            return Files.readAllLines(logFile, StandardCharsets.ISO_8859_1);
        } catch (IOException e) {
            return List.of("(no server log at " + logFile + ")");
        }
    }
}
