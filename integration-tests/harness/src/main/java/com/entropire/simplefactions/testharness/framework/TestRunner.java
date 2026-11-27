package com.entropire.simplefactions.testharness.framework;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Runs the gate test first. If it fails, every suite is skipped, because they'd only repeat
 * the same failure. Every test catches everything, including {@link LinkageError}s such as
 * {@code NoSuchMethodError}: those are what you get when the plugin calls an API that a
 * server version or server type doesn't have.
 */
public final class TestRunner {

    private final List<TestResult> results = new ArrayList<>();

    public List<TestResult> run(String environment, TestCase gate, List<Suite> suites) {
        results.add(TestResult.info("environment", environment));

        boolean gatePassed = run(gate.name(), gate.body());

        for (Suite suite : suites) {
            for (TestCase test : suite.tests()) {
                String name = suite.name() + " > " + test.name();
                if (gatePassed) {
                    run(name, test.body());
                } else {
                    results.add(TestResult.skip(name, "skipped because '" + gate.name() + "' failed"));
                }
            }
        }
        return results;
    }

    private boolean run(String name, TestCase.Body body) {
        long start = System.nanoTime();
        try {
            body.run();
            results.add(TestResult.pass(name, elapsedMillis(start)));
            return true;
        } catch (Throwable t) {
            results.add(TestResult.fail(name, elapsedMillis(start), describe(t)));
            return false;
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
