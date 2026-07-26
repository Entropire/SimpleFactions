package com.entropire.simplefactions.player;

import java.time.Instant;
import java.util.UUID;

public record FactionPlayer(
        UUID uuid,
        String username,
        ChatMode chatMode,
        Instant lastSeen
) {
    public FactionPlayer {
        if (uuid == null) {
            throw new IllegalArgumentException("uuid cannot be null");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username cannot be null or blank");
        }
    }
}