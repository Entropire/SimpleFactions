package com.entropire.simplefactions.testharness.framework;

import java.util.List;

public interface Suite {

    String name();

    List<TestCase> tests();

    static TestCase test(String name, TestCase.Body body) {
        return new TestCase(name, body);
    }
}
