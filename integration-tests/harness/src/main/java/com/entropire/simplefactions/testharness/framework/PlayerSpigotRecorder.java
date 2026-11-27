package com.entropire.simplefactions.testharness.framework;

import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;

final class PlayerSpigotRecorder extends Player.Spigot {

    private final Consumer<String> record;

    PlayerSpigotRecorder(Consumer<String> record) {
        this.record = record;
    }

    @Override
    public void sendMessage(BaseComponent component) {
        record.accept(component.toPlainText());
    }

    @Override
    public void sendMessage(BaseComponent... components) {
        record.accept(BaseComponent.toPlainText(components));
    }

    @Override
    public void sendMessage(UUID sender, BaseComponent component) {
        sendMessage(component);
    }

    @Override
    public void sendMessage(UUID sender, BaseComponent... components) {
        sendMessage(components);
    }

    @Override
    public void sendMessage(ChatMessageType position, BaseComponent component) {
        sendMessage(component);
    }

    @Override
    public void sendMessage(ChatMessageType position, BaseComponent... components) {
        sendMessage(components);
    }
}
