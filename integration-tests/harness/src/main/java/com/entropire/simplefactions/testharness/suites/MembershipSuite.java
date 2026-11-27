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

public final class MembershipSuite implements Suite {

    @Override
    public String name() {
        return "Membership";
    }

    @Override
    public List<TestCase> tests() {
        return List.of(
                test("the owner kicks a member", this::kickMember),
                test("kick tab completion suggests members", this::kickCompletion),
                test("a member leaves and can create their own faction", this::leaveFaction),
                test("members are listed with the owner first", this::membersList));
    }

    private void kickMember() throws Exception {
        FakePlayer alice = joinedPlayer("Alice");
        FakePlayer bob = joinedPlayer("Bob");
        String faction = uniqueFactionName("red");
        run(alice, "create " + faction);
        addMember(faction, bob);

        run(alice, "kick " + bob.name());

        Check.contains(alice.allText(), "You have kicked " + bob.name() + " from your faction.");
        Check.equal(null, factionOf(bob), "faction of the kicked member");
        run(bob, "members");
        Check.contains(bob.allText(), "You are not in a faction!");
    }

    private void kickCompletion() throws Exception {
        FakePlayer alice = joinedPlayer("Alice");
        FakePlayer bob = joinedPlayer("Bob");
        String faction = uniqueFactionName("red");
        run(alice, "create " + faction);
        addMember(faction, bob);

        Check.containsItem(tabComplete(alice, "kick " + bob.name().substring(0, 2)), bob.name(), "completions for /f kick");
    }

    private void leaveFaction() throws Exception {
        FakePlayer alice = joinedPlayer("Alice");
        FakePlayer bob = joinedPlayer("Bob");
        String faction = uniqueFactionName("red");
        run(alice, "create " + faction);
        addMember(faction, bob);

        run(bob, "leave");

        Check.contains(bob.allText(), "You have left your faction.");
        Check.equal(null, factionOf(bob), "faction after leaving");
        Check.equal(faction, factionOf(alice), "faction of the owner after a member left");
        String own = uniqueFactionName("own");
        run(bob, "create " + own);
        Check.equal(own, factionOf(bob), "faction after creating one");
    }

    private void membersList() throws Exception {
        FakePlayer zoe = joinedPlayer("Zoe");
        FakePlayer bob = joinedPlayer("Bob");
        FakePlayer carl = joinedPlayer("Carl");
        String faction = uniqueFactionName("red");
        run(zoe, "create " + faction);
        addMember(faction, carl);
        addMember(faction, bob);

        run(bob, "members " + faction);

        String text = bob.allText();
        int owner = text.indexOf(zoe.name());
        int first = text.indexOf(bob.name());
        int second = text.indexOf(carl.name());
        Check.that(owner >= 0 && owner < first && first < second,
                "Expected " + zoe.name() + ", then " + bob.name() + ", then " + carl.name() + " in:\n" + text);
    }
}
