package com.entropire.simplefactions.faction.membership;

import static com.entropire.simplefactions.testsupport.Results.assertFailure;
import static com.entropire.simplefactions.testsupport.Results.assertSuccess;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.entropire.simplefactions.faction.Faction;
import com.entropire.simplefactions.objects.Pageable;
import com.entropire.simplefactions.testsupport.Given;
import com.entropire.simplefactions.testsupport.TestDatabase;

class FactionMembershipServiceTest {

    @TempDir
    Path tempDir;

    private TestDatabase test;
    private FactionMembershipService memberships;
    private Given given;

    @BeforeEach
    void setUp() {
        test = TestDatabase.create(tempDir);
        memberships = test.membershipService;
        given = test.given;
    }

    private Optional<FactionMembership> membershipOf(UUID player) {
        return assertSuccess(test.db.withConnection(conn -> test.membershipRepository.findByPlayerUuid(conn, player)));
    }

    @Nested
    class Leave {

        @Test
        void theOwnerCantLeave() {
            UUID alice = given.player("Alice");
            given.faction("red", alice);

            assertFailure(memberships.leaveFaction(alice),
                    "The owner can't leave the faction. Transfer ownership or delete the faction instead.");
            assertEquals(Role.OWNER, membershipOf(alice).orElseThrow().role());
        }

        @Test
        void rejectsAPlayerWithoutFaction() {
            assertFailure(memberships.leaveFaction(given.player("Alice")), "You are not part of a faction!");
        }
    }

    @Nested
    class Kick {

        @Test
        void onlyTheOwnerCanKick() {
            Faction red = given.faction("red", given.player("Alice"));
            UUID bob = given.memberOf(red, "Bob");
            UUID carol = given.memberOf(red, "Carol");

            assertFailure(memberships.kickFactionMember(bob, "Carol"),
                    "You must be the owner to kick somebody from the faction!");
            assertTrue(membershipOf(carol).isPresent());
        }

        @Test
        void theOwnerCantKickThemselves() {
            UUID alice = given.player("Alice");
            given.faction("red", alice);

            assertFailure(memberships.kickFactionMember(alice, "Alice"),
                    "You can't kick yourself. Transfer ownership or delete the faction instead.");
            assertTrue(membershipOf(alice).isPresent());
        }

        @Test
        void cantKickAMemberOfAnotherFaction() {
            UUID alice = given.player("Alice");
            given.faction("red", alice);
            Faction blue = given.faction("blue", given.player("Carol"));
            UUID dave = given.memberOf(blue, "Dave");

            assertFailure(memberships.kickFactionMember(alice, "Dave"), "Dave is not part of your faction!");
            assertTrue(membershipOf(dave).isPresent());
        }

        @Test
        void cantKickAnotherFactionsOwner() {
            UUID alice = given.player("Alice");
            given.faction("red", alice);
            UUID carol = given.player("Carol");
            given.faction("blue", carol);

            assertFailure(memberships.kickFactionMember(alice, "Carol"), "Carol is not part of your faction!");
            assertTrue(membershipOf(carol).isPresent());
        }

        @Test
        void rejectsAPlayerWithoutFactionOrAnUnknownName() {
            UUID alice = given.player("Alice");
            given.faction("red", alice);
            given.player("Bob");

            assertFailure(memberships.kickFactionMember(alice, "Bob"), "Bob is not part of your faction!");
            assertFailure(memberships.kickFactionMember(alice, "Nobody"), "Nobody is not part of your faction!");
        }

        @Test
        void rejectsAKickerWithoutFaction() {
            assertFailure(memberships.kickFactionMember(given.player("Alice"), "Bob"), "You are not in a faction!");
        }
    }

    @Nested
    class MemberNameCompletion {

        @Test
        void doesNotSuggestMembersOfOtherFactions() {
            UUID alice = given.player("Alice");
            Faction red = given.faction("red", alice);
            given.memberOf(red, "Bob");
            Faction blue = given.faction("blue", given.player("Bonnie"));
            given.memberOf(blue, "Boris");

            assertEquals(List.of("Bob"), names(alice, "Bo"));
        }

        @Test
        void doesNotTreatUnderscoreAsAWildcard() {
            UUID alice = given.player("Alice");
            Faction red = given.faction("red", alice);
            given.memberOf(red, "Bo_b");
            given.memberOf(red, "Boxb");

            assertEquals(List.of("Bo_b"), names(alice, "Bo_"));
        }

        @Test
        void rejectsAPlayerWithoutFaction() {
            assertFailure(memberships.getFactionMemberNames(new Pageable<>(0, 10), given.player("Alice"), ""),
                    "You are not in a faction!");
        }

        private List<String> names(UUID player, String prefix) {
            return assertSuccess(memberships.getFactionMemberNames(new Pageable<>(0, 10), player, prefix)).items();
        }
    }
}
