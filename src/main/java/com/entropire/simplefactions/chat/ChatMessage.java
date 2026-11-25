package com.entropire.simplefactions.chat;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class ChatMessage {

    private final List<MessageComponent> components = new ArrayList<>();

    public ChatMessage title(String title) {
        components.add(new TitleComponent(title));
        return this;
    }

    public ChatMessage list(String... items) {
        components.add(new ListComponent(items));
        return this;
    }

    public ChatMessage text(String text) {
        components.add(new TextComponent(text));
        return this;
    }

    public ChatMessage textIf(boolean bool,String text) {
        if(bool) components.add(new TextComponent(text));
        return this;
    }

    public BaseComponent build() {
        BaseComponent message = MessageComponent.text("", null);

        for (int i = 0; i < components.size(); i++) {
            message.addExtra(components.get(i).build());

            if (i != components.size() - 1) {
                message.addExtra(MessageComponent.text("\n", null));
            }
        }

        return message;
    }

    public void sendTo(CommandSender sender) {
        sender.spigot().sendMessage(build());
    }

    public ChatMessage setBold(boolean bold){
        components.get(components.size() - 1).setBold(bold);
        return this;
    }

    /** Colors the last added component, for example {@code setColor(0xFFD166)}. */
    public ChatMessage setColor(int rgb){
        components.get(components.size() - 1).setColor(MessageComponent.rgb(rgb));
        return this;
    }

    public ChatMessage message(ChatMessage message){
        components.addAll(message.components);
        return this;
    }
}
