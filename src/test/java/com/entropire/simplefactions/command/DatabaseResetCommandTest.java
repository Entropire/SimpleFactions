package com.entropire.simplefactions.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.command.Command;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.entropire.simplefactions.database.DataBaseContext;

class DatabaseResetCommandTest {

    @TempDir
    Path tempDir;

    private Path dbFile;
    private final AtomicInteger restarts = new AtomicInteger();
    private DatabaseResetCommand reset;
    private final Command command = mock(Command.class);

    @BeforeEach
    void setUp() throws Exception {
        dbFile = tempDir.resolve("Simple-Faction.db");
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE factions (uuid TEXT PRIMARY KEY)");
        }
        reset = new DatabaseResetCommand(new DataBaseContext(dbFile.toString()), "test", dbFile.toString(), restarts::incrementAndGet);
    }

    private void assertNotReset() throws Exception {
        assertTrue(Files.exists(dbFile), "the database file should not be moved");
        try (var backups = Files.list(tempDir)) {
            assertEquals(1, backups.count(), "no backup should be made");
        }
        assertEquals(0, restarts.get(), "the plugin should not be started");
    }

    @Test
    void aPlayerCannotResetTheDatabase() throws Exception {
        reset.onCommand(mock(Player.class), command, "f", new String[] {"database", "reset", "confirm"});

        assertNotReset();
    }

    @Test
    void resetWithoutConfirmDoesNotReset() throws Exception {
        reset.onCommand(mock(ConsoleCommandSender.class), command, "f", new String[] {"database", "reset"});

        assertNotReset();
    }

    @Test
    void otherCommandsDoNotReset() throws Exception {
        reset.onCommand(mock(ConsoleCommandSender.class), command, "f", new String[] {"confirm"});
        reset.onCommand(mock(ConsoleCommandSender.class), command, "f", new String[] {"reset", "database", "confirm"});

        assertNotReset();
    }

    @Test
    void tabCompletionDoesNotSuggestTheResetToPlayers() {
        assertEquals(List.of(), reset.onTabComplete(mock(Player.class), command, "f", new String[] {""}));
    }
}
