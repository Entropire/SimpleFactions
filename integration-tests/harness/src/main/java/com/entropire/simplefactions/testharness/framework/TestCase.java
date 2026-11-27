package com.entropire.simplefactions.testharness.framework;

public record TestCase(String name, Body body) {

    @FunctionalInterface
    public interface Body {
        void run() throws Exception;
    }
}
