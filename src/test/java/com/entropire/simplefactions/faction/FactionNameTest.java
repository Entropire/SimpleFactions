package com.entropire.simplefactions.faction;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class FactionNameTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"ab", "abcdefghijklmnopq", "Red", "red team", "red!"})
    void cannotBeCreatedWithAnInvalidName(String name) {
        assertThrows(IllegalArgumentException.class, () -> new FactionName(name));
    }
}
