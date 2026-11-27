package com.entropire.simplefactions.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.List;

import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.entropire.simplefactions.faction.Faction;
import com.entropire.simplefactions.testsupport.TestCommands;
import com.entropire.simplefactions.testsupport.TestDatabase;
import com.entropire.simplefactions.testsupport.TestPlayer;

class FactionCommandsTest {

    @TempDir
    Path tempDir;

    private TestDatabase test;
    private TestCommands commands;

    @BeforeEach
    void setUp() {
        test = TestDatabase.create(tempDir);
        commands = new TestCommands(test);
    }

    @ParameterizedTest
    @ValueSource(strings = {"create red", "delete", "list", "members", "owner", "leave", "kick Bob"})
    void theConsoleCannotUsePlayerCommands(String arguments) {
        ConsoleCommandSender console = mock(ConsoleCommandSender.class);
        when(console.spigot()).thenReturn(mock(CommandSender.Spigot.class));

        commands.run(console, arguments);

        verify(console).sendMessage("Only players can preform this command!");
        assertEquals(0, test.count("factions"));
    }

    @Nested
    class Create {

        @Test
        void withoutANameDoesNotCreateAFaction() {
            TestPlayer alice = commands.online("Alice");

            commands.run(alice, "create");

            assertEquals(0, test.count("factions"));
            assertTrue(alice.allText().contains("/f create <FactionName>"), alice.allText());
        }

        @Test
        void anInvalidColorIsNotReportedAsAnInternalError() {
            TestPlayer alice = commands.online("Alice");

            commands.run(alice, "create red blue");

            assertEquals(List.of("A faction color must be a hex color like #ff0000."), alice.messages());
            assertEquals(0, test.count("factions"));
        }

        @Test
        void aRefusedCreateDoesNotFailSilently() {
            TestPlayer alice = commands.online("Alice");

            commands.run(alice, "create Red");

            assertEquals(List.of("A faction name must be 3-16 characters: lowercase letters, numbers or _."), alice.messages());
        }
    }

    @Nested
    class Delete {

        @Test
        void helpDoesNotDeleteTheFaction() {
            TestPlayer alice = commands.online("Alice");
            commands.faction("red", alice);

            commands.run(alice, "delete help");

            assertEquals(1, test.count("factions"));
        }

        @Test
        void aMemberCannotDeleteAndNobodyIsTold() {
            TestPlayer alice = commands.online("Alice");
            Faction red = commands.faction("red", alice);
            TestPlayer bob = commands.online("Bob");
            commands.member(red, bob);

            commands.run(bob, "delete");

            assertEquals(1, test.count("factions"));
            assertEquals(List.of("Only the owner can delete the faction!"), bob.messages());
            assertTrue(alice.messages().isEmpty());
        }

        @Test
        void doesNotTellTheOwnerOrOtherFactionsThatTheFactionWasDeleted() {
            TestPlayer alice = commands.online("Alice");
            Faction red = commands.faction("red", alice);
            commands.member(red, commands.online("Bob"));
            TestPlayer carol = commands.online("Carol");
            commands.faction("blue", carol);

            commands.run(alice, "delete");

            assertFalse(alice.allText().contains("Your faction has been deleted!"), alice.allText());
            assertTrue(carol.messages().isEmpty(), carol.allText());
        }
    }

    @Nested
    class Kick {

        @Test
        void withoutANameDoesNotKickAnybody() {
            TestPlayer alice = commands.online("Alice");
            Faction red = commands.faction("red", alice);
            commands.member(red, commands.online("Bob"));

            commands.run(alice, "kick");

            assertEquals(2, test.count("faction_memberships"));
            assertTrue(alice.allText().contains("/f kick <member name>"), alice.allText());
        }

        @Test
        void aRefusedKickDoesNotTellTheTarget() {
            TestPlayer alice = commands.online("Alice");
            Faction red = commands.faction("red", alice);
            TestPlayer bob = commands.online("Bob");
            TestPlayer carol = commands.online("Carol");
            commands.member(red, bob);
            commands.member(red, carol);

            commands.run(bob, "kick Carol");

            assertTrue(carol.messages().isEmpty(), carol.allText());
            assertEquals(3, test.count("faction_memberships"));
        }

        @Test
        void kickingAnOfflineMemberDoesNotFail() {
            TestPlayer alice = commands.online("Alice");
            Faction red = commands.faction("red", alice);
            commands.member(red, commands.offline("Bob"));

            commands.run(alice, "kick Bob");

            assertEquals(List.of("You have kicked Bob from your faction."), alice.messages());
        }

        @Test
        void tabCompletionDoesNotSuggestYourself() {
            TestPlayer alice = commands.online("Alice");
            Faction red = commands.faction("red", alice);
            commands.member(red, commands.online("Bob"));

            assertEquals(List.of("Bob"), commands.tabComplete(alice, "kick "));
        }

        @Test
        void tabCompletionDoesNotSuggestAnythingAfterTheName() {
            TestPlayer alice = commands.online("Alice");
            Faction red = commands.faction("red", alice);
            commands.member(red, commands.online("Bob"));

            assertEquals(List.of(), commands.tabComplete(alice, "kick Bob "));
        }
    }

    @Nested
    class Lists {

        @ParameterizedTest
        @ValueSource(strings = {"list abc", "list 0", "list -5", "list 999", "list 99999999999"})
        void anInvalidPageDoesNotBreakTheList(String arguments) {
            TestPlayer alice = commands.online("Alice");
            commands.faction("red", alice);

            commands.run(alice, arguments);

            assertTrue(alice.allText().contains("red"), alice.allText());
        }

        @ParameterizedTest
        @ValueSource(strings = {"members 0", "members 999", "members 99999999999", "members red abc", "members red 999"})
        void anInvalidPageDoesNotBreakTheMembersList(String arguments) {
            TestPlayer alice = commands.online("Alice");
            commands.faction("red", alice);

            commands.run(alice, arguments);

            assertTrue(alice.allText().contains("Alice"), alice.allText());
        }

        @Test
        void tabCompletionDoesNotSuggestEveryFactionForAnEmptyInput() {
            TestPlayer alice = commands.online("Alice");
            commands.faction("red", alice);

            assertEquals(List.of(), commands.tabComplete(alice, "owner "));
            assertEquals(List.of(), commands.tabComplete(alice, "members "));
        }
    }
}
