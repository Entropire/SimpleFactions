package com.entropire.simplefactions.testharness;

import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.command.CommandSender;

import net.md_5.bungee.api.chat.BaseComponent;

/**
 * The {@code sender.spigot()} part of a fake sender. SimpleFactions sends its formatted
 * messages through {@code sender.spigot().sendMessage(BaseComponent...)}, so without this the
 * fake sender would return {@code null} there and every formatted message would crash.
 *
 * <p>It's a separate class so that a server without BungeeCord chat classes only fails the
 * tests that send formatted messages, instead of the whole harness failing to load.
 */
final class SpigotRecorder extends CommandSender.Spigot {

    private final Consumer<String> record;

    SpigotRecorder(Consumer<String> record) {
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
}
