package com.entropire.simplefactions.chat;

import net.md_5.bungee.api.chat.BaseComponent;

public class TextComponent extends MessageComponent {

    private final String text;

    public TextComponent(String text) {
        this.text = text;
    }

    @Override
    public BaseComponent build() {
        BaseComponent component = text(text, color);
        if (bold) component.setBold(true);
        return component;
    }
}
