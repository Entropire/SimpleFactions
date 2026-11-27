package com.entropire.simplefactions.chat;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ChatMessageTest {

    @Test
    void aTitleWiderThanTheChatDoesNotCrash() {
        String title = "Members of abcdefghijklmnop (100/100) and a lot more text";

        String text = assertDoesNotThrow(() -> new ChatMessage().title(title).build().toPlainText());

        assertTrue(text.contains(title));
    }

    @Test
    void anEmptyListDoesNotCrash() {
        assertDoesNotThrow(() -> new ChatMessage().title("List").list().title(null).build());
    }

    @Test
    void charactersMissingFromTheFontTableDoNotCrash() {
        assertDoesNotThrow(() -> new ChatMessage().title("Fraktion ÄÖÜ 日本").build());
    }
}
