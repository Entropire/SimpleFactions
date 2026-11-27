package com.entropire.simplefactions.testsupport;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;

import net.md_5.bungee.api.chat.BaseComponent;

public final class TestPlayer {

    public final UUID uuid;
    public final String name;
    public final Player player = mock(Player.class);

    private final List<String> messages = new ArrayList<>();

    TestPlayer(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;

        when(player.getUniqueId()).thenReturn(uuid);
        when(player.getName()).thenReturn(name);
        doAnswer(call -> record(call.getArgument(0))).when(player).sendMessage(anyString());

        Player.Spigot spigot = mock(Player.Spigot.class);
        doAnswer(call -> record(((BaseComponent) call.getArgument(0)).toPlainText()))
                .when(spigot).sendMessage(any(BaseComponent.class));
        when(player.spigot()).thenReturn(spigot);
    }

    private Object record(String message) {
        messages.add(message);
        return null;
    }

    public List<String> messages() {
        return List.copyOf(messages);
    }

    public String allText() {
        return String.join("\n", messages);
    }

    public void clearMessages() {
        messages.clear();
    }
}
