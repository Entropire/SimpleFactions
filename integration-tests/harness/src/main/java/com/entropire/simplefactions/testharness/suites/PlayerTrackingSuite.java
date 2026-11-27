package com.entropire.simplefactions.testharness.suites;

import static com.entropire.simplefactions.testharness.framework.SimpleFactionsPlugin.execute;
import static com.entropire.simplefactions.testharness.framework.SimpleFactionsPlugin.joinedPlayer;
import static com.entropire.simplefactions.testharness.framework.SimpleFactionsPlugin.query;
import static com.entropire.simplefactions.testharness.framework.Suite.test;

import java.util.List;

import com.entropire.simplefactions.testharness.framework.Check;
import com.entropire.simplefactions.testharness.framework.Events;
import com.entropire.simplefactions.testharness.framework.FakePlayer;
import com.entropire.simplefactions.testharness.framework.Suite;
import com.entropire.simplefactions.testharness.framework.TestCase;

public final class PlayerTrackingSuite implements Suite {

    @Override
    public String name() {
        return "Player tracking";
    }

    @Override
    public List<TestCase> tests() {
        return List.of(
                test("joining registers the player", this::joiningRegistersThePlayer),
                test("joining with a new name updates the name", this::joiningWithANewNameUpdatesTheName),
                test("quitting updates last seen", this::quittingUpdatesLastSeen));
    }

    private void joiningRegistersThePlayer() throws Exception {
        long before = System.currentTimeMillis();
        FakePlayer alice = joinedPlayer("Alice");

        Check.equal(alice.name(), username(alice), "username in the database");
        Check.that(lastSeen(alice) >= before, "last_seen should be the join time, was " + lastSeen(alice));
    }

    private void joiningWithANewNameUpdatesTheName() throws Exception {
        FakePlayer alice = joinedPlayer("Alice");
        FakePlayer renamed = alice.withName(alice.name() + "x");

        Events.join(renamed);

        Check.equal(renamed.name(), username(alice), "username after joining with a new name");
    }

    private void quittingUpdatesLastSeen() throws Exception {
        FakePlayer alice = joinedPlayer("Alice");
        execute("UPDATE players SET last_seen = 0 WHERE uuid = ?", alice.uuid().toString());
        long before = System.currentTimeMillis();

        Events.quit(alice);

        Check.that(lastSeen(alice) >= before, "last_seen should be the quit time, was " + lastSeen(alice));
    }

    private static String username(FakePlayer player) throws Exception {
        return query("SELECT username FROM players WHERE uuid = ?", player.uuid().toString());
    }

    private static long lastSeen(FakePlayer player) throws Exception {
        return Long.parseLong(query("SELECT last_seen FROM players WHERE uuid = ?", player.uuid().toString()));
    }
}
