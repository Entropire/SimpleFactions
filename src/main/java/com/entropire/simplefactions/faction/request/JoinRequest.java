package com.entropire.simplefactions.faction.request;

import java.time.Instant;
import java.util.UUID;

public record JoinRequest(
        UUID uuid,
        UUID playerUuid,
        UUID factionUuid,
        Instant requestedAt,
        Instant expiresAt
) {
    public JoinRequest {
        if (playerUuid == null) {
            throw new IllegalArgumentException("playerUuid cannot be null");
        }
        if (factionUuid == null) {
            throw new IllegalArgumentException("factionUuid cannot be null");
        }
        if (expiresAt == null) {
            throw new IllegalArgumentException("expiresAt cannot be null");
        }
    }
}