package com.entropire.simplefactions.faction;

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
) {
    public Faction(String name, String color, UUID ownerUUID) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name cannot be null or blank");
        }

        if (color == null || color.isBlank() || !color.matches("^#[a-fA-F0-9]{6}$")) {
            throw new IllegalArgumentException("color cannot be null or blank");
        }

        if (ownerUUID == null) {
            throw new IllegalArgumentException("ownerUUID cannot be null");
        }

        this(null, name, color, ownerUUID, null, false, null);
    }
}
