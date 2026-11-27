package com.entropire.simplefactions.it;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** One line of servers.csv: a server type, Minecraft version, Java version and run size. */
record ServerTarget(String type, String version, int java, String size) {

    private static final Set<String> TYPES = Set.of("paper", "purpur", "folia", "spigot", "craftbukkit");
    static final List<String> SIZES = List.of("smoke", "default", "full");

    String id() {
        return type + "-" + version;
    }

    /** Spigot and CraftBukkit can't be downloaded, they're built with BuildTools. */
    boolean needsBuildTools() {
        return type.equals("spigot") || type.equals("craftbukkit");
    }

    /** The TYPE setting of the itzg/minecraft-server image. */
    String imageType() {
        return needsBuildTools() ? "CUSTOM" : type.toUpperCase(Locale.ROOT);
    }

    /**
     * A filter is a comma-separated list where each entry is a type ("paper"), a version
     * ("1.21.4") or a full id ("folia-1.21.4"). An empty filter matches everything.
     */
    boolean matches(String filter) {
        if (filter == null || filter.isBlank()) return true;
        return Arrays.stream(filter.split(","))
                .map(String::trim)
                .filter(entry -> !entry.isEmpty())
                .anyMatch(entry -> entry.equalsIgnoreCase(type)
                        || entry.equalsIgnoreCase(version)
                        || entry.equalsIgnoreCase(id()));
    }

    boolean inMatrix(String matrix) {
        return SIZES.indexOf(size) <= SIZES.indexOf(matrix);
    }

    static List<ServerTarget> load(Path csv, String filter, String matrix) throws IOException {
        if (!SIZES.contains(matrix)) {
            throw new IllegalArgumentException("Unknown -Pmatrix=" + matrix + ", expected one of " + SIZES);
        }

        List<ServerTarget> targets = new ArrayList<>();
        int lineNumber = 0;

        for (String line : Files.readAllLines(csv)) {
            lineNumber++;
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;

            String[] parts = trimmed.split("\\s*,\\s*");
            if (parts.length != 4) {
                throw new IllegalArgumentException(csv.getFileName() + " line " + lineNumber
                        + ": expected 'type, version, java, size' but got: " + line);
            }

            String type = parts[0].toLowerCase(Locale.ROOT);
            if (!TYPES.contains(type)) {
                throw new IllegalArgumentException(csv.getFileName() + " line " + lineNumber
                        + ": unknown server type '" + parts[0] + "', expected one of " + TYPES);
            }

            String size = parts[3].toLowerCase(Locale.ROOT);
            if (!SIZES.contains(size)) {
                throw new IllegalArgumentException(csv.getFileName() + " line " + lineNumber
                        + ": unknown size '" + parts[3] + "', expected one of " + SIZES);
            }

            ServerTarget target = new ServerTarget(type, parts[1], Integer.parseInt(parts[2]), size);
            if (target.inMatrix(matrix) && target.matches(filter)) targets.add(target);
        }
        return targets;
    }
}
