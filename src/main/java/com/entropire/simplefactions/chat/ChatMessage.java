package com.entropire.simplefactions.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

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

    public Component build() {
        Component message = Component.empty();

        for (int i = 0; i < components.size(); i++) {
            message = message.append(components.get(i).build());

            if (i != components.size() - 1) {
                message = message.append(Component.newline());
            }
        }

        return message;
    }

    public ChatMessage setBold(boolean bold){
        components.getLast().setBold(bold);
        return this;
    }

    public ChatMessage setColor(TextColor color){
        components.getLast().setColor(color);
        return this;
    }

    public ChatMessage message(ChatMessage message){
        components.addAll(message.components);
        return this;
    }
}