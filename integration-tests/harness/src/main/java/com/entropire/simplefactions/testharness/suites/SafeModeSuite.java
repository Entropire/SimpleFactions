package com.entropire.simplefactions.testharness.suites;

import static com.entropire.simplefactions.testharness.framework.SimpleFactionsPlugin.databaseFile;
import static com.entropire.simplefactions.testharness.framework.SimpleFactionsPlugin.factionOf;
import static com.entropire.simplefactions.testharness.framework.SimpleFactionsPlugin.joinedPlayer;
import static com.entropire.simplefactions.testharness.framework.SimpleFactionsPlugin.query;
import static com.entropire.simplefactions.testharness.framework.SimpleFactionsPlugin.run;
import static com.entropire.simplefactions.testharness.framework.SimpleFactionsPlugin.uniqueFactionName;
import static com.entropire.simplefactions.testharness.framework.Suite.test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import com.entropire.simplefactions.testharness.framework.Check;
import com.entropire.simplefactions.testharness.framework.FakePlayer;
import com.entropire.simplefactions.testharness.framework.RecordingSender;
import com.entropire.simplefactions.testharness.framework.Suite;
import com.entropire.simplefactions.testharness.framework.TestCase;

public final class SafeModeSuite implements Suite {

    @Override
    public String name() {
        return "Safe mode";
    }

    @Override
    public List<TestCase> tests() {
        return List.of(
                test("players are told the plugin is disabled and nothing changes", this::playersAreToldItIsDisabled),
                test("the server log explains the options", this::serverLogExplainsTheOptions),
                test("'f database reset confirm' from the console brings the plugin back", this::resetBringsThePluginBack));
    }

    private void playersAreToldItIsDisabled() throws Exception {
        FakePlayer alice = new FakePlayer("Alice");
        String factionsBefore = query("SELECT COUNT(*) FROM factions");

        run(alice, "create " + uniqueFactionName("blocked"));

        Check.contains(alice.allText(), "SimpleFactions is disabled");
        Check.equal(factionsBefore, query("SELECT COUNT(*) FROM factions"), "number of factions");
    }

    private void serverLogExplainsTheOptions() throws Exception {
        String log = new String(Files.readAllBytes(new File("logs/latest.log").toPath()), StandardCharsets.UTF_8);

        Check.contains(log, "can't use the existing database file");
        Check.contains(log, "That file was created by SimpleFactions " + PersistenceSuite.NEWER_PLUGIN_VERSION + ".");
        Check.contains(log, "f database reset");
    }

    private void resetBringsThePluginBack() throws Exception {
        RecordingSender console = RecordingSender.console();

        run(console.sender(), "database reset confirm");

        Check.contains(console.allText(), "Database reset.");
        Check.contains(console.allText(), "SimpleFactions is enabled again.");
        File[] backups = databaseFile().getParentFile().listFiles((dir, name) -> name.startsWith("Simple-Faction.db.backup-"));
        Check.that(backups != null && backups.length == 1, "Expected one backup of the old database next to the new one");

        FakePlayer alice = joinedPlayer("Alice");
        String faction = uniqueFactionName("reset");
        run(alice, "create " + faction);
        Check.equal(faction, factionOf(alice), "faction created after the reset");
    }
}
