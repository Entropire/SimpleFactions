package com.entropire.simplefactions.testharness.suites;

import static com.entropire.simplefactions.testharness.framework.SimpleFactionsPlugin.execute;
import static com.entropire.simplefactions.testharness.framework.SimpleFactionsPlugin.factionOf;
import static com.entropire.simplefactions.testharness.framework.SimpleFactionsPlugin.joinedPlayer;
import static com.entropire.simplefactions.testharness.framework.SimpleFactionsPlugin.query;
import static com.entropire.simplefactions.testharness.framework.SimpleFactionsPlugin.run;
import static com.entropire.simplefactions.testharness.framework.SimpleFactionsPlugin.uniqueFactionName;
import static com.entropire.simplefactions.testharness.framework.Suite.test;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

import com.entropire.simplefactions.testharness.framework.Check;
import com.entropire.simplefactions.testharness.framework.FakePlayer;
import com.entropire.simplefactions.testharness.framework.PhaseData;
import com.entropire.simplefactions.testharness.framework.Suite;
import com.entropire.simplefactions.testharness.framework.TestCase;

public final class PersistenceSuite implements Suite {

    public static final String NEWER_SCHEMA_VERSION = "999";
    public static final String NEWER_PLUGIN_VERSION = "99.0";

    @Override
    public String name() {
        return "Persistence";
    }

    @Override
    public List<TestCase> tests() {
        return List.of(
                test("the faction and its members are still there", this::factionSurvivedTheRestart),
                test("the players are still registered", this::playersSurvivedTheRestart),
                test("a new player can create a faction", this::createAfterRestart),
                test("the database is marked as made by a newer SimpleFactions for the next boot", this::markDatabaseAsNewer));
    }

    private void factionSurvivedTheRestart() throws Exception {
        Properties data = PhaseData.load();
        String faction = data.getProperty("faction");
        FakePlayer owner = player(data, "owner");
        FakePlayer member = player(data, "member");

        Check.equal(faction, factionOf(owner), "faction of the owner after the restart");
        Check.equal(faction, factionOf(member), "faction of the member after the restart");
        run(owner, "members");
        Check.contains(owner.allText(), "Members of " + faction);
        Check.contains(owner.allText(), member.name());
    }

    private void playersSurvivedTheRestart() throws Exception {
        Properties data = PhaseData.load();
        for (String who : List.of("owner", "member")) {
            FakePlayer player = player(data, who);
            Check.equal(player.name(), query("SELECT username FROM players WHERE uuid = ?", player.uuid().toString()),
                    "username of the " + who + " after the restart");
        }
    }

    private void createAfterRestart() throws Exception {
        FakePlayer alice = joinedPlayer("Alice");
        String faction = uniqueFactionName("after");

        run(alice, "create " + faction);

        Check.equal(faction, factionOf(alice), "faction created after the restart");
    }

    private void markDatabaseAsNewer() throws Exception {
        execute("UPDATE plugin_meta SET value = ? WHERE key = 'schema_version'", NEWER_SCHEMA_VERSION);
        execute("UPDATE plugin_meta SET value = ? WHERE key = 'plugin_version'", NEWER_PLUGIN_VERSION);
        Check.equal(NEWER_SCHEMA_VERSION, query("SELECT value FROM plugin_meta WHERE key = 'schema_version'"), "schema_version");
    }

    private static FakePlayer player(Properties data, String who) {
        return FakePlayer.existing(UUID.fromString(data.getProperty(who + ".uuid")), data.getProperty(who + ".name"));
    }
}
