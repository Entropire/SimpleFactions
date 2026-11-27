package com.entropire.simplefactions.player;

import static com.entropire.simplefactions.testsupport.Results.assertSuccess;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.entropire.simplefactions.testsupport.TestDatabase;

class FactionPlayerServiceTest {

    @TempDir
    Path tempDir;

    private TestDatabase test;
    private FactionPlayerService players;

    @BeforeEach
    void setUp() {
        test = TestDatabase.create(tempDir);
        players = test.playerService;
    }

    @Test
    void rejoiningDoesNotCreateASecondPlayer() {
        UUID alice = UUID.randomUUID();
        assertSuccess(players.registerJoin(alice, "Alice"));

        assertSuccess(players.registerJoin(alice, "Alice2"));

        assertEquals(1, test.count("players"));
    }

    @Test
    void rejoiningDoesNotResetTheChatMode() {
        UUID alice = UUID.randomUUID();
        assertSuccess(players.registerJoin(alice, "Alice"));
        test.execute("UPDATE players SET chat_mode = 'FACTION' WHERE uuid = ?", alice.toString());

        assertSuccess(players.registerJoin(alice, "Alice"));

        assertEquals("FACTION", test.query("SELECT chat_mode FROM players WHERE uuid = ?", alice.toString()));
    }

    @Test
    void leavingWithoutEverJoiningDoesNotCreateAPlayer() {
        assertSuccess(players.registerLeave(UUID.randomUUID()));

        assertEquals(0, test.count("players"));
    }

    @Test
    void playersWithTheSameNameAreNotMerged() {
        assertSuccess(players.registerJoin(UUID.randomUUID(), "Alice"));
        assertSuccess(players.registerJoin(UUID.randomUUID(), "Alice"));

        assertEquals(2, test.count("players"));
    }
}
