package com.entropire.simplefactions.faction.invite;

import java.time.Instant;
import java.util.UUID;

public record FactionInvite(
        UUID uuid,
        UUID factionUuid,
        UUID playerUuid,
        UUID invitedBy,
        Instant invitedAt,
        Instant expiresAt
) {
    public FactionInvite {
        if (factionUuid == null) {
            throw new IllegalArgumentException("factionUuid cannot be null");
        }
        if (playerUuid == null) {
            throw new IllegalArgumentException("playerUuid cannot be null");
        }
        if (invitedBy == null) {
            throw new IllegalArgumentException("invitedBy cannot be null");
        }
        if (expiresAt == null) {
            throw new IllegalArgumentException("expiresAt cannot be null");
        }
    }
}
