package com.entropire.simplefactions.testsupport;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mockito.MockedStatic;

import com.entropire.simplefactions.command.SimpleFactionCommand;
import com.entropire.simplefactions.faction.Faction;

public final class TestCommands {

    private final TestDatabase test;
    private final Map<UUID, Player> online = new HashMap<>();
    private final SimpleFactionCommand command;
    private final Command bukkitCommand = mock(Command.class);

    public TestCommands(TestDatabase test) {
        this.test = test;
        this.command = new SimpleFactionCommand(test.factionService, test.membershipService);
    }

    public TestPlayer online(String name) {
        TestPlayer player = offline(name);
        online.put(player.uuid, player.player);
        return player;
    }

    public TestPlayer offline(String name) {
        return new TestPlayer(test.given.player(name), name);
    }

    public Faction faction(String name, TestPlayer owner) {
        return test.given.faction(name, owner.uuid);
    }

    public void member(Faction faction, TestPlayer player) {
        test.given.member(faction, player.uuid);
    }

    public void run(TestPlayer player, String arguments) {
        run(player.player, arguments);
    }

    public void run(CommandSender sender, String arguments) {
        String[] args = arguments.isEmpty() ? new String[0] : arguments.split(" ");
        withOnlinePlayers(() -> command.onCommand(sender, bukkitCommand, "f", args));
    }

    public List<String> tabComplete(TestPlayer player, String arguments) {
        return withOnlinePlayers(() -> command.onTabComplete(player.player, bukkitCommand, "f", arguments.split(" ", -1)));
    }

    private <T> T withOnlinePlayers(Supplier<T> action) {
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(() -> Bukkit.getPlayer(any(UUID.class))).thenAnswer(call -> online.get(call.getArgument(0)));
            return action.get();
        }
    }
}
