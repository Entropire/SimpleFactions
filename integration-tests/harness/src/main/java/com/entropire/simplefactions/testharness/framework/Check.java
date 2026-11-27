package com.entropire.simplefactions.testharness.framework;

import java.util.Collection;
import java.util.Objects;

public final class Check {

    private Check() {}

    public static void that(boolean condition, String failureMessage) {
        if (!condition) throw new AssertionError(failureMessage);
    }

    public static void contains(String text, String expected) {
        that(text != null && text.contains(expected), "Expected '" + expected + "' in:\n" + text);
    }

    public static void doesNotContain(String text, String unexpected) {
        that(text == null || !text.contains(unexpected), "Did not expect '" + unexpected + "' in:\n" + text);
    }

    public static void equal(Object expected, Object actual, String what) {
        that(Objects.equals(expected, actual), what + ": expected " + expected + " but was " + actual);
    }

    public static void containsItem(Collection<?> items, Object expected, String what) {
        that(items != null && items.contains(expected), what + ": expected " + expected + " in " + items);
    }
}
