package com.entropire.simplefactions.chat;

import com.entropire.simplefactions.chat.font.FontHelper;
import com.entropire.simplefactions.chat.font.MinecraftFont;

import net.md_5.bungee.api.chat.BaseComponent;

public class TitleComponent extends MessageComponent {

    private final String title;

    public TitleComponent(String title) {
        this.title = title;
    }

    @Override
    public BaseComponent build() {

        if (title == null || title.isEmpty()) {
            return text("─".repeat(CHAT_WIDTH / MinecraftFont.HORIZONTAL_LINE.getWidth()), rgb(0x888888));
        }

        int remaining = (CHAT_WIDTH - FontHelper.width(title)) / MinecraftFont.HORIZONTAL_LINE.getWidth();
        int left = remaining / 2;
        int right = remaining - left;

        BaseComponent line = text("", null);
        line.addExtra(text("─".repeat(left), rgb(0x666666)));
        line.addExtra(text(" " + title + " ", rgb(0xFFD166)));
        line.addExtra(text("─".repeat(right), rgb(0x666666)));
        return line;
    }
}
