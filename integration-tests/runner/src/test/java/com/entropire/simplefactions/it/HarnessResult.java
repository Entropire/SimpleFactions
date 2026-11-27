package com.entropire.simplefactions.it;

import java.util.ArrayList;
import java.util.List;

/** One line of the harness' results file, see TestResult in the harness plugin. */
record HarnessResult(String status, String name, long millis, String detail) {

    static List<HarnessResult> parse(List<String> lines) {
        List<HarnessResult> results = new ArrayList<>();
        for (String line : lines) {
            if (line.isBlank()) continue;
            String[] parts = line.split("\t", -1);
            if (parts.length != 4) throw new IllegalArgumentException("Malformed result line: " + line);
            results.add(new HarnessResult(parts[0], unescape(parts[1]), Long.parseLong(parts[2]), unescape(parts[3])));
        }
        return results;
    }

    private static String unescape(String text) {
        StringBuilder result = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\\' && i + 1 < text.length()) {
                char next = text.charAt(++i);
                result.append(switch (next) {
                    case 't' -> '\t';
                    case 'n' -> '\n';
                    default -> next;
                });
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
}
