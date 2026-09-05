package com.entropire.simplefactions.chat;

import com.entropire.simplefactions.chat.font.FontHelper;
import com.entropire.simplefactions.chat.font.MinecraftFont;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class TitleComponent extends MessageComponent {

    private final String title;

    public TitleComponent(String title) {
        this.title = title;
    }

    @Override
    public Component build() {

        if (title == null || title.isEmpty()) {
            return Component.text("─".repeat(CHAT_WIDTH / MinecraftFont.HORIZONTAL_LINE.getWidth()))
                    .color(TextColor.color(0x888888));
        }

        int remaining = (CHAT_WIDTH - FontHelper.width(title)) / MinecraftFont.HORIZONTAL_LINE.getWidth();
        int left = remaining / 2;
        int right = remaining - left;

        return Component.text()
                .append(Component.text("─".repeat(left))
                        .color(TextColor.color(0x666666)))
                .append(Component.text(" " + title + " ")
                        .color(TextColor.color(0xFFD166)))
                .append(Component.text("─".repeat(right))
                        .color(TextColor.color(0x666666)))
                .build();
    }
}