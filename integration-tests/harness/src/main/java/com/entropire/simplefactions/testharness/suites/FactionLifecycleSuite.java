package com.entropire.simplefactions.testharness.suites;

import static com.entropire.simplefactions.testharness.framework.SimpleFactionsPlugin.addMember;
import static com.entropire.simplefactions.testharness.framework.SimpleFactionsPlugin.factionOf;
import static com.entropire.simplefactions.testharness.framework.SimpleFactionsPlugin.joinedPlayer;
import static com.entropire.simplefactions.testharness.framework.SimpleFactionsPlugin.run;
import static com.entropire.simplefactions.testharness.framework.SimpleFactionsPlugin.tabComplete;
import static com.entropire.simplefactions.testharness.framework.SimpleFactionsPlugin.uniqueFactionName;
import static com.entropire.simplefactions.testharness.framework.Suite.test;

import java.util.List;

import com.entropire.simplefactions.testharness.framework.Check;
import com.entropire.simplefactions.testharness.framework.FakePlayer;
import com.entropire.simplefactions.testharness.framework.Suite;
import com.entropire.simplefactions.testharness.framework.TestCase;

public final class FactionLifecycleSuite implements Suite {

    @Override
    public String name() {
        return "Factions";
    }

    @Override
    public List<TestCase> tests() {
        return List.of(
                test("a player creates a faction and finds it in list, owner and members", this::createAndLookUp),
                test("owner and members tab completion suggest faction names", this::factionNameCompletion),
                test("the owner deletes the faction and every member ends up without one", this::deleteFaction),
                test("a player creates a new faction after deleting the old one", this::createAgainAfterDelete));
    }

    private void createAndLookUp() throws Exception {
        FakePlayer alice = joinedPlayer("Alice");
        String faction = uniqueFactionName("red");

        run(alice, "create " + faction);
        Check.contains(alice.allText(), "Created faction " + faction + ".");
        Check.equal(faction, factionOf(alice), "faction of the creator");

        alice.clearMessages();
        run(alice, "list");
        Check.contains(alice.allText(), faction);

        alice.clearMessages();
        run(alice, "owner " + faction);
        Check.equal(List.of(alice.name()), alice.messages(), "/f owner " + faction);

        alice.clearMessages();
        run(alice, "members");
        Check.contains(alice.allText(), "Members of " + faction);
        Check.contains(alice.allText(), alice.name());
    }

    private void factionNameCompletion() throws Exception {
        FakePlayer alice = joinedPlayer("Alice");
        String faction = uniqueFactionName("tab");
        run(alice, "create " + faction);

        Check.containsItem(tabComplete(alice, "owner " + faction.substring(0, 3)), faction, "completions for /f owner");
        Check.containsItem(tabComplete(alice, "members " + faction.substring(0, 3)), faction, "completions for /f members");
    }

    private void deleteFaction() throws Exception {
        FakePlayer alice = joinedPlayer("Alice");
        FakePlayer bob = joinedPlayer("Bob");
        String faction = uniqueFactionName("red");
        run(alice, "create " + faction);
        addMember(faction, bob);

        run(alice, "delete");

        Check.contains(alice.allText(), "You have deleted your faction.");
        Check.equal(null, factionOf(alice), "faction of the owner after delete");
        Check.equal(null, factionOf(bob), "faction of the member after delete");
        alice.clearMessages();
        run(alice, "owner " + faction);
        Check.contains(alice.allText(), "There is no faction named " + faction);
    }

    private void createAgainAfterDelete() throws Exception {
        FakePlayer alice = joinedPlayer("Alice");
        run(alice, "create " + uniqueFactionName("old"));
        run(alice, "delete");
        String faction = uniqueFactionName("new");

        run(alice, "create " + faction);

        Check.equal(faction, factionOf(alice), "faction after creating a new one");
    }
}
