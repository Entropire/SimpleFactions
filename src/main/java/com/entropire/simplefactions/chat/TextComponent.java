package com.entropire.simplefactions.chat;

import net.kyori.adventure.text.Component;

public class TextComponent extends MessageComponent {

    private final String text;

    public TextComponent(String text) {
        this.text = text;
    }

    @Override
    public Component build() {
        return Component.text(text, color);
    }
}