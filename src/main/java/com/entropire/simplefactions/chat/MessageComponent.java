package com.entropire.simplefactions.chat;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;

public abstract class MessageComponent {

    int CHAT_WIDTH = 200;

    boolean bold = false;
    ChatColor color;

    abstract BaseComponent build();

    public void setBold(boolean bold){
        this.bold = bold;
    }

    public void setColor(ChatColor color){
        this.color = color;
    }

    static ChatColor rgb(int rgb) {
        return ChatColor.of(String.format("#%06X", rgb & 0xFFFFFF));
    }

    static net.md_5.bungee.api.chat.TextComponent text(String text, ChatColor color) {
        net.md_5.bungee.api.chat.TextComponent component = new net.md_5.bungee.api.chat.TextComponent(text);
        if (color != null) component.setColor(color);
        return component;
    }
}
