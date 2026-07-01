package com.entropire.simplefactions.db.faction;

import java.time.Instant;
import java.util.UUID;

public record Faction(
        UUID uuid,
        String name,
        String color,
        UUID ownerUUID,
        Instant createdAt,
        boolean isActive,
        Instant lastActivity
) {}
