package com.entropire.simplefactions.player;

import java.time.Instant;
import java.util.UUID;

public record Player(
        UUID uuid,
        String username,
        ChatMode chatMode,
        Instant lastSeen
) {
    public Player {
        if (uuid == null) {
            throw new IllegalArgumentException("uuid cannot be null");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username cannot be null or blank");
        }
    }
}