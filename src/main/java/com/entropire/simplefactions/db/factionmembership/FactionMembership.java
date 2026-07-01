package com.entropire.simplefactions.db.factionmembership;

import java.time.Instant;
import java.util.UUID;

public record FactionMembership(
        UUID playerUuid,
        UUID factionUuid,
        Role role,
        Instant joinedAt
) {
    public FactionMembership{
        if (playerUuid == null) {
            throw new IllegalArgumentException("Player UUID cannot be null");
        }

        if (factionUuid == null) {
            throw new IllegalArgumentException("Faction UUID cannot be null");
        }

        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
    }
}