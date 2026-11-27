package com.entropire.simplefactions.it;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class CompatibilityReport {

    private final List<Outcome> outcomes;
    private final Set<String> tests = new LinkedHashSet<>();
    private final Map<String, Map<String, HarnessResult>> resultsPerServer = new LinkedHashMap<>();
    private final Map<String, String> environments = new LinkedHashMap<>();

    CompatibilityReport(List<Outcome> outcomes) {
        this.outcomes = outcomes;
        tests.add(AllServersTest.SERVER_STARTS);
        for (Outcome outcome : outcomes) {
            Map<String, HarnessResult> results = new LinkedHashMap<>();
            String server = outcome.target().id();
            if (outcome.error() != null) {
                results.put(AllServersTest.SERVER_STARTS, new HarnessResult("FAIL", AllServersTest.SERVER_STARTS, 0, outcome.error()));
            } else {
                results.put(AllServersTest.SERVER_STARTS, new HarnessResult("PASS", AllServersTest.SERVER_STARTS, 0, ""));
                for (HarnessResult result : outcome.results()) {
                    if (result.status().equals("INFO")) {
                        environments.put(server, result.detail());
                        continue;
                    }
                    tests.add(result.name());
                    results.put(result.name(), result);
                }
            }
            resultsPerServer.put(server, results);
        }
    }

    List<Path> write(Path folder) throws IOException {
        Files.createDirectories(folder);
        Path html = folder.resolve("index.html");
        Path markdown = folder.resolve("compatibility.md");
        Files.writeString(html, html(), StandardCharsets.UTF_8);
        Files.writeString(markdown, markdown(), StandardCharsets.UTF_8);
        return List.of(html, markdown);
    }

    List<String> failingServers() {
        List<String> failing = new ArrayList<>();
        resultsPerServer.forEach((server, results) -> {
            if (results.values().stream().anyMatch(r -> r.status().equals("FAIL"))) failing.add(server);
        });
        return failing;
    }

    private String summary() {
        List<String> failing = failingServers();
        return failing.isEmpty()
                ? "All " + outcomes.size() + " servers passed every test."
                : failing.size() + " of " + outcomes.size() + " servers have failures: " + String.join(", ", failing);
    }

    private String markdown() {
        StringBuilder md = new StringBuilder("# SimpleFactions compatibility\n\n")
                .append(timestamp()).append("\n\n").append(summary()).append("\n\n| Test |");
        for (String server : resultsPerServer.keySet()) md.append(' ').append(server).append(" |");
        md.append("\n|---|");
        md.append(":-:|".repeat(resultsPerServer.size())).append('\n');
        for (String test : tests) {
            md.append("| ").append(test.replace("|", "\\|")).append(" |");
            for (Map<String, HarnessResult> results : resultsPerServer.values()) {
                md.append(' ').append(symbol(results.get(test), "✅", "❌", "➖")).append(" |");
            }
            md.append('\n');
        }
        return md.toString();
    }

    private String html() {
        StringBuilder html = new StringBuilder("""
                <!doctype html>
                <html lang="en"><head><meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <title>SimpleFactions compatibility</title>
                <style>
                :root { --bg: #ffffff; --fg: #1f2328; --muted: #656d76; --line: #d0d7de; --head: #f6f8fa;
                        --pass: #1a7f37; --fail: #cf222e; --skip: #8c959f; }
                @media (prefers-color-scheme: dark) {
                  :root { --bg: #0d1117; --fg: #e6edf3; --muted: #8d96a0; --line: #30363d; --head: #161b22;
                          --pass: #3fb950; --fail: #f85149; --skip: #6e7681; }
                }
                body { background: var(--bg); color: var(--fg); font: 14px/1.4 system-ui, sans-serif; margin: 16px; }
                h1 { font-size: 20px; margin: 0 0 4px; }
                .muted { color: var(--muted); }
                .summary { margin: 12px 0; font-weight: 600; }
                .wrap { overflow: auto; max-height: 80vh; border: 1px solid var(--line); }
                table { border-collapse: collapse; }
                th, td { border-bottom: 1px solid var(--line); padding: 4px 8px; }
                thead th { position: sticky; top: 0; background: var(--head); z-index: 2; }
                thead th.server { writing-mode: vertical-rl; transform: rotate(180deg); white-space: nowrap; }
                th a { color: inherit; }
                td.test, th.test { position: sticky; left: 0; background: var(--bg); text-align: left; z-index: 1; }
                thead th.test { z-index: 3; background: var(--head); }
                td.cell { text-align: center; font-weight: 700; }
                .PASS { color: var(--pass); } .FAIL { color: var(--fail); } .SKIP { color: var(--skip); }
                </style></head><body>
                <h1>SimpleFactions compatibility</h1>
                """);
        html.append("<div class=\"muted\">").append(escape(timestamp())).append(" · hover a column for the exact server and Java version, a ✗ for the failure</div>\n");
        html.append("<div class=\"summary ").append(failingServers().isEmpty() ? "PASS" : "FAIL").append("\">")
                .append(escape(summary())).append("</div>\n");
        html.append("<div class=\"wrap\"><table><thead><tr><th class=\"test\">Test</th>");
        for (String server : resultsPerServer.keySet()) {
            html.append("<th class=\"server\" title=\"").append(escape(environments.getOrDefault(server, server))).append("\">")
                    .append("<a href=\"../../server-logs/").append(escape(server)).append(".log\">")
                    .append(escape(server)).append("</a></th>");
        }
        html.append("</tr></thead><tbody>\n");
        for (String test : tests) {
            html.append("<tr><td class=\"test\">").append(escape(test)).append("</td>");
            for (Map<String, HarnessResult> results : resultsPerServer.values()) {
                HarnessResult result = results.get(test);
                String status = result == null ? "" : result.status();
                String title = result == null ? "not run" : result.detail().isEmpty() ? status : result.detail();
                html.append("<td class=\"cell ").append(status).append("\" title=\"").append(escape(shorten(title))).append("\">")
                        .append(symbol(result, "✓", "✗", "–")).append("</td>");
            }
            html.append("</tr>\n");
        }
        return html.append("</tbody></table></div></body></html>\n").toString();
    }

    private static String symbol(HarnessResult result, String pass, String fail, String skip) {
        if (result == null) return "";
        return switch (result.status()) {
            case "PASS" -> pass;
            case "SKIP" -> skip;
            default -> fail;
        };
    }

    private static String timestamp() {
        return "Generated " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    private static String shorten(String text) {
        return text.length() <= 600 ? text : text.substring(0, 600) + " ...";
    }

    private static String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
