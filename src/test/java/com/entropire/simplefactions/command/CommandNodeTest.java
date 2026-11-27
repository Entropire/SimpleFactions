package com.entropire.simplefactions.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.entropire.simplefactions.chat.ChatMessage;

class CommandNodeTest {

    private final Command command = mock(Command.class);
    private final CommandSender sender = mock(CommandSender.class);

    private TestNode root;
    private TestNode create;
    private TestNode delete;

    @BeforeEach
    void setUp() {
        when(sender.spigot()).thenReturn(mock(CommandSender.Spigot.class));

        root = new TestNode("simplefactions");
        create = new TestNode("create");
        delete = new TestNode("delete");
        root.register(create);
        root.register(delete);
    }

    @Test
    void anUnknownSubcommandDoesNotRunAnySubcommand() {
        root.onCommand(sender, command, "f", new String[] {"nope"});

        assertEquals(List.of(List.of("nope")), root.executed);
        assertTrue(create.executed.isEmpty());
        assertTrue(delete.executed.isEmpty());
    }

    @Test
    void helpAfterASubcommandDoesNotRunTheSubcommand() {
        root.onCommand(sender, command, "f", new String[] {"delete", "help"});

        assertTrue(delete.executed.isEmpty());
        assertTrue(root.executed.isEmpty());
    }

    @Test
    void tabCompletionDoesNotSuggestSubcommandsThatDontMatch() {
        assertEquals(List.of("create"), root.onTabComplete(sender, command, "f", new String[] {"CR"}));
    }

    @Test
    void tabCompletionAfterASubcommandDoesNotSuggestSubcommandNames() {
        create.completions = List.of("red");

        assertEquals(List.of("red"), root.onTabComplete(sender, command, "f", new String[] {"create", ""}));
    }

    private static final class TestNode extends CommandNode {

        final List<List<String>> executed = new ArrayList<>();
        List<String> completions = List.of();

        TestNode(String name) {
            super(name, name + " description");
        }

        @Override
        public boolean execute(CommandSender sender, String[] args) {
            executed.add(List.of(args));
            return true;
        }

        @Override
        public List<String> tabComplete(CommandSender sender, String[] args) {
            return completions;
        }

        @Override
        public ChatMessage help(String[] args) {
            return new ChatMessage().text("help for " + getCommandName());
        }
    }
}
