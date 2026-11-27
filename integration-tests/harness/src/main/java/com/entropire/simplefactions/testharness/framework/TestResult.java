package com.entropire.simplefactions.testharness.framework;

/**
 * One line in the results file: {@code STATUS <tab> name <tab> millis <tab> detail}.
 * Tabs, newlines and backslashes in the detail are escaped, so every result stays on one line.
 */
public record TestResult(Status status, String name, long millis, String detail) {

    public enum Status { PASS, FAIL, SKIP, INFO }

    public static TestResult pass(String name, long millis) {
        return new TestResult(Status.PASS, name, millis, "");
    }

    public static TestResult fail(String name, long millis, String detail) {
        return new TestResult(Status.FAIL, name, millis, detail);
    }

    public static TestResult skip(String name, String reason) {
        return new TestResult(Status.SKIP, name, 0, reason);
    }

    public static TestResult info(String name, String detail) {
        return new TestResult(Status.INFO, name, 0, detail);
    }

    public String toLine() {
        return status + "\t" + escape(name) + "\t" + millis + "\t" + escape(detail);
    }

    private static String escape(String text) {
        return text.replace("\\", "\\\\").replace("\t", "\\t").replace("\r", "").replace("\n", "\\n");
    }
}
