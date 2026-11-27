package com.entropire.simplefactions.command;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SimpleFactionCommandTest {

    private final SimpleFactionCommand command = new SimpleFactionCommand(null, null);

    @ParameterizedTest
    @ValueSource(strings = {"abc", "0", "-3", "99", "99999999999"})
    void anInvalidHelpPageDoesNotBreakTheHelp(String page) {
        String help = command.help(new String[] {page}).build().toPlainText();

        assertTrue(help.contains("/f create"), "help should still list the commands, got: " + help);
    }
}
