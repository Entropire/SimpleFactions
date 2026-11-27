package com.entropire.simplefactions.testharness.suites;

import static com.entropire.simplefactions.testharness.framework.SimpleFactionsPlugin.joinedPlayer;
import static com.entropire.simplefactions.testharness.framework.SimpleFactionsPlugin.run;
import static com.entropire.simplefactions.testharness.framework.Suite.test;

import java.util.List;

import com.entropire.simplefactions.testharness.framework.Check;
import com.entropire.simplefactions.testharness.framework.FakePlayer;
import com.entropire.simplefactions.testharness.framework.RecordingSender;
import com.entropire.simplefactions.testharness.framework.Suite;
import com.entropire.simplefactions.testharness.framework.TestCase;

public final class HelpSuite implements Suite {

    @Override
    public String name() {
        return "Help";
    }

    @Override
    public List<TestCase> tests() {
        return List.of(
                test("/f help lists the commands", this::helpListsTheCommands),
                test("/f create help explains create to a player", this::subcommandHelp));
    }

    private void helpListsTheCommands() {
        RecordingSender console = new RecordingSender();
        run(console.sender(), "help");
        for (String command : List.of("create", "delete", "list", "owner", "members", "leave", "kick")) {
            Check.contains(console.allText(), "/f " + command);
        }
    }

    private void subcommandHelp() throws Exception {
        FakePlayer alice = joinedPlayer("Alice");
        run(alice, "create help");
        Check.contains(alice.allText(), "create - help");
        Check.contains(alice.allText(), "/f create <FactionName>");
    }
}
