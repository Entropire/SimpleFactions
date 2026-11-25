package com.entropire.simplefactions.chat;

import net.md_5.bungee.api.chat.BaseComponent;

public class ListComponent extends MessageComponent {

    private final String[] items;

    public ListComponent(String[] items) {
        this.items = items;
    }

    @Override
    public BaseComponent build() {

        BaseComponent list = text("", null);

        for (int i = 0; i < items.length; i++) {

            list.addExtra(text("• ", rgb(0x55FF55)));
            list.addExtra(text(items[i], rgb(0xFFFFFF)));

            if (i != items.length - 1) {
                list.addExtra(text("\n", null));
            }
        }

        return list;
    }
}
