package com.entropire.simplefactions.testharness;

final class Check {

    private Check() {}

    static void that(boolean condition, String failureMessage) {
        if (!condition) throw new AssertionError(failureMessage);
    }
}
