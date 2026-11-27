package com.entropire.simplefactions.testharness;

/**
 * One line in the results file: {@code STATUS <tab> name <tab> millis <tab> detail}.
 * Tabs, newlines and backslashes in the detail are escaped, so every result stays on one line.
 */
record TestResult(Status status, String name, long millis, String detail) {

    enum Status { PASS, FAIL, SKIP, INFO }

    static TestResult pass(String name, long millis) {
        return new TestResult(Status.PASS, name, millis, "");
    }

    static TestResult fail(String name, long millis, String detail) {
        return new TestResult(Status.FAIL, name, millis, detail);
    }

    static TestResult skip(String name, String reason) {
        return new TestResult(Status.SKIP, name, 0, reason);
    }

    static TestResult info(String name, String detail) {
        return new TestResult(Status.INFO, name, 0, detail);
    }

    String toLine() {
        return status + "\t" + escape(name) + "\t" + millis + "\t" + escape(detail);
    }

    private static String escape(String text) {
        return text.replace("\\", "\\\\").replace("\t", "\\t").replace("\r", "").replace("\n", "\\n");
    }
}
