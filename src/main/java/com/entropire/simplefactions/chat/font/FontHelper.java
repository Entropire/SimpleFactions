package com.entropire.simplefactions.chat.font;

public class FontHelper {
    public static int width(String text) {
        int width = 0;

        for (char c : text.toCharArray()) {
            width += MinecraftFont.get(c).getWidth();
        }

        return width;
    }
}
