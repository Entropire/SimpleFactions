package com.entropire.simplefactions.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public abstract class MessageComponent {

    int CHAT_WIDTH = 200;

    boolean bold = false;
    TextColor color;

    abstract Component build();

    public void setBold(boolean bold){
        this.bold = bold;
    }

    public void setColor(TextColor color){
        this.color = color;
    }
}