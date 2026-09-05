package com.entropire.simplefactions.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class ListComponent extends MessageComponent {

    private final String[] items;

    public ListComponent(String[] items) {
        this.items = items;
    }

    @Override
    public Component build() {

        Component list = Component.empty();

        for (int i = 0; i < items.length; i++) {

            list = list.append(
                    Component.text("• ", TextColor.color(0x55FF55))
                            .append(Component.text(items[i], TextColor.color(0xFFFFFF)))
            );

            if (i != items.length - 1) {
                list = list.append(Component.newline());
            }
        }

        return list;
    }
}