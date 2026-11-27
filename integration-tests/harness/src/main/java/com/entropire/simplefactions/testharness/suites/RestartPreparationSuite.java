package com.entropire.simplefactions.testharness.suites;

import static com.entropire.simplefactions.testharness.framework.SimpleFactionsPlugin.addMember;
import static com.entropire.simplefactions.testharness.framework.SimpleFactionsPlugin.factionOf;
import static com.entropire.simplefactions.testharness.framework.SimpleFactionsPlugin.joinedPlayer;
import static com.entropire.simplefactions.testharness.framework.SimpleFactionsPlugin.run;
import static com.entropire.simplefactions.testharness.framework.SimpleFactionsPlugin.uniqueFactionName;
import static com.entropire.simplefactions.testharness.framework.Suite.test;

import java.util.List;
import java.util.Properties;

import com.entropire.simplefactions.testharness.framework.Check;
import com.entropire.simplefactions.testharness.framework.FakePlayer;
import com.entropire.simplefactions.testharness.framework.PhaseData;
import com.entropire.simplefactions.testharness.framework.Suite;
import com.entropire.simplefactions.testharness.framework.TestCase;

public final class RestartPreparationSuite implements Suite {

    @Override
    public String name() {
        return "Restart";
    }

    @Override
    public List<TestCase> tests() {
        return List.of(test("a faction with a member is saved for the restart check", this::saveFactionForRestart));
    }

    private void saveFactionForRestart() throws Exception {
        FakePlayer owner = joinedPlayer("Owner");
        FakePlayer member = joinedPlayer("Member");
        String faction = uniqueFactionName("keep");
        run(owner, "create " + faction);
        Check.equal(faction, factionOf(owner), "faction of the owner");
        addMember(faction, member);

        Properties data = new Properties();
        data.setProperty("faction", faction);
        data.setProperty("owner.uuid", owner.uuid().toString());
        data.setProperty("owner.name", owner.name());
        data.setProperty("member.uuid", member.uuid().toString());
        data.setProperty("member.name", member.name());
        PhaseData.save(data);
    }
}
