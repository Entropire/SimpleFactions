package com.entropire.simplefactions.faction;

import static com.entropire.simplefactions.testsupport.Results.assertFailure;
import static com.entropire.simplefactions.testsupport.Results.assertSuccess;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.entropire.simplefactions.objects.Pageable;
import com.entropire.simplefactions.player.FactionPlayer;
import com.entropire.simplefactions.testsupport.Given;
import com.entropire.simplefactions.testsupport.TestDatabase;

class FactionServiceTest {

    @TempDir
    Path tempDir;

    private TestDatabase test;
    private FactionService factions;
    private Given given;

    @BeforeEach
    void setUp() {
        test = TestDatabase.create(tempDir);
        factions = test.factionService;
        given = test.given;
    }

    @Nested
    class CreateFaction {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void rejectsEmptyNames(String name) {
            assertFailure(factions.createFaction(name, "#ffffff", given.player("Alice")),
                    "A faction name can't be empty.");
            assertEquals(0, test.count("factions"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"ab", "abcdefghijklmnopq", "Red", "red team", "red!", "rød"})
        void rejectsInvalidNames(String name) {
            assertFailure(factions.createFaction(name, "#ffffff", given.player("Alice")),
                    "A faction name must be 3-16 characters: lowercase letters, numbers or _.");
            assertEquals(0, test.count("factions"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"blue", "#fff", "ff0000", "#gggggg"})
        void rejectsInvalidColors(String color) {
            assertFailure(factions.createFaction("red", color, given.player("Alice")),
                    "A faction color must be a hex color like #ff0000.");
            assertEquals(0, test.count("factions"));
        }

        @Test
        void rejectsATakenName() {
            given.faction("red", given.player("Alice"));
            UUID bob = given.player("Bob");

            assertFailure(factions.createFaction("red", "#ffffff", bob), "Faction name already in use");
            assertEquals(1, test.count("factions"));
            assertEquals(1, test.count("faction_memberships"));
        }

        @Test
        void rejectsAnOwnerWhoAlreadyHasAFaction() {
            UUID alice = given.player("Alice");
            given.faction("red", alice);

            assertFailure(factions.createFaction("blue", "#ffffff", alice), "You are already part of a faction!");
            assertEquals(1, test.count("factions"));
        }

        @Test
        void rejectsAMemberOfAnotherFaction() {
            Faction red = given.faction("red", given.player("Alice"));
            UUID bob = given.memberOf(red, "Bob");

            assertFailure(factions.createFaction("blue", "#ffffff", bob), "You are already part of a faction!");
            assertEquals(1, test.count("factions"));
        }
    }

    @Nested
    class DeleteFaction {

        @Test
        void onlyTheOwnerCanDelete() {
            Faction red = given.faction("red", given.player("Alice"));
            UUID bob = given.memberOf(red, "Bob");

            assertFailure(factions.deleteFaction(bob), "Only the owner can delete the faction!");
            assertEquals(1, test.count("factions"));
            assertEquals(2, test.count("faction_memberships"));
        }

        @Test
        void rejectsAPlayerWithoutFaction() {
            assertFailure(factions.deleteFaction(given.player("Alice")), "You are not in a faction!");
        }

        @Test
        void doesNotTouchOtherFactions() {
            UUID alice = given.player("Alice");
            given.faction("red", alice);
            Faction blue = given.faction("blue", given.player("Bob"));
            given.memberOf(blue, "Carol");

            assertSuccess(factions.deleteFaction(alice));

            assertEquals(1, test.count("factions"));
            assertEquals(2, test.count("faction_memberships"));
        }

        @Test
        void doesNotLeaveInvitesOrJoinRequestsBehind() {
            UUID alice = given.player("Alice");
            Faction red = given.faction("red", alice);
            UUID dave = given.player("Dave");
            long inAMinute = System.currentTimeMillis() + 60_000;
            test.execute("INSERT INTO faction_invites (uuid, faction_uuid, player_uuid, invited_by, expires_at) VALUES (?, ?, ?, ?, ?)",
                    UUID.randomUUID().toString(), red.uuid().toString(), dave.toString(), alice.toString(), inAMinute);
            test.execute("INSERT INTO join_requests (uuid, player_uuid, faction_uuid, expires_at) VALUES (?, ?, ?, ?)",
                    UUID.randomUUID().toString(), dave.toString(), red.uuid().toString(), inAMinute);

            assertSuccess(factions.deleteFaction(alice));

            assertEquals(0, test.count("faction_invites"));
            assertEquals(0, test.count("join_requests"));
        }
    }

    @Nested
    class ListFactions {

        @Test
        void aPageThatDoesNotExistShowsTheNearestPage() {
            for (String name : List.of("aaa", "bbb", "ccc")) given.faction(name, given.player(name + "_owner"));

            assertEquals(1, assertSuccess(factions.getFactions(new Pageable<>(99, 2))).currentPage());
            assertEquals(0, assertSuccess(factions.getFactions(new Pageable<>(-5, 2))).currentPage());
        }
    }

    @Nested
    class FactionNameCompletion {

        @Test
        void doesNotTreatUnderscoreAsAWildcard() {
            for (String name : List.of("a_team", "abteam")) given.faction(name, given.player(name + "_owner"));

            assertEquals(Set.of("a_team"),
                    Set.copyOf(assertSuccess(factions.getFactionNames(new Pageable<>(0, 10), "a_")).items()));
        }
    }

    @Nested
    class Owner {

        @Test
        void rejectsAnUnknownFactionName() {
            assertFailure(factions.getFactionOwner("nope"), "There is no faction named nope");
        }

        @Test
        void rejectsAPlayerWithoutFaction() {
            assertFailure(factions.getPlayerFactionOwner(given.player("Alice")), "You are not in a faction!");
        }
    }

    @Nested
    class Members {

        @Test
        void doesNotListMembersOfOtherFactions() {
            Faction red = given.faction("red", given.player("Alice"));
            given.memberOf(red, "Bob");
            Faction blue = given.faction("blue", given.player("Carol"));
            given.memberOf(blue, "Dave");

            assertEquals(Set.of("Alice", "Bob"), usernames(assertSuccess(factions.getFactionMembers("red", new Pageable<>(0, 10)))));
        }

        @Test
        void aPageThatDoesNotExistShowsTheLastPage() {
            Faction red = given.faction("red", given.player("Owner"));
            for (String name : List.of("Ann", "Ben", "Cat", "Dan")) given.memberOf(red, name);

            FactionMembersPage page = assertSuccess(factions.getFactionMembers("red", new Pageable<>(99, 2)));

            assertEquals(2, page.members().currentPage());
            assertEquals(Set.of("Dan"), usernames(page));
        }

        @Test
        void rejectsAnUnknownFactionName() {
            assertFailure(factions.getFactionMembers("nope", new Pageable<>(0, 10)), "There is no faction named nope");
        }

        @Test
        void rejectsAPlayerWithoutFaction() {
            assertFailure(factions.getPlayerFactionMembers(given.player("Alice"), new Pageable<>(0, 10)),
                    "You are not in a faction!");
        }

        private Set<String> usernames(FactionMembersPage page) {
            return page.members().items().stream().map(FactionPlayer::username).collect(Collectors.toSet());
        }
    }
}
