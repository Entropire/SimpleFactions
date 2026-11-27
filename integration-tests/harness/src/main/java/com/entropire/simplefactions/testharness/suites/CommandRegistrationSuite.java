package com.entropire.simplefactions.testharness.suites;

import static com.entropire.simplefactions.testharness.framework.Suite.test;

import java.util.List;

import org.bukkit.command.PluginCommand;

import com.entropire.simplefactions.testharness.framework.Check;
import com.entropire.simplefactions.testharness.framework.RecordingSender;
import com.entropire.simplefactions.testharness.framework.SimpleFactionsPlugin;
import com.entropire.simplefactions.testharness.framework.Suite;
import com.entropire.simplefactions.testharness.framework.TestCase;

public final class CommandRegistrationSuite implements Suite {

    @Override
    public String name() {
        return "Commands";
    }

    @Override
    public List<TestCase> tests() {
        return List.of(
                test("/faction is registered with alias /f", this::factionCommandIsRegistered),
                test("the console gets the players-only message for /f create", this::consoleCreateIsRejected),
                test("tab completion suggests subcommands", this::tabCompletionSuggestsSubcommands));
    }

    private void factionCommandIsRegistered() {
        PluginCommand command = SimpleFactionsPlugin.factionCommand();
        Check.that(command.getPlugin() == SimpleFactionsPlugin.plugin(), "/faction belongs to " + command.getPlugin().getName());
        Check.containsItem(command.getAliases(), "f", "aliases of /faction");
        Check.that(command.getExecutor() != SimpleFactionsPlugin.plugin(), "/faction has no executor, so startPlugin() didn't finish");
    }

    private void consoleCreateIsRejected() {
        RecordingSender console = new RecordingSender();
        SimpleFactionsPlugin.run(console.sender(), "create testfaction");
        Check.contains(console.allText(), "Only players can");
    }

    private void tabCompletionSuggestsSubcommands() {
        RecordingSender console = new RecordingSender();
        List<String> completions = SimpleFactionsPlugin.factionCommand().tabComplete(console.sender(), "f", new String[] {"cr"});
        Check.containsItem(completions, "create", "completions for '/f cr'");
    }
}
