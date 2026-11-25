package com.entropire.simplefactions.faction;

import java.time.Instant;
import java.util.UUID;

public record Faction(
        UUID uuid,
        FactionName name,
        String color,
        UUID ownerUUID,
        Instant createdAt,
        boolean isActive,
        Instant lastActivity
) {
    public Faction(FactionName name, String color, UUID ownerUUID) {
        this(null, requireName(name), requireColor(color), requireOwner(ownerUUID), null, false, null);
    }

    private static FactionName requireName(FactionName name) {
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null");
        }
        return name;
    }

    private static String requireColor(String color) {
        if (color == null || color.isBlank() || !color.matches("^#[a-fA-F0-9]{6}$")) {
            throw new IllegalArgumentException("color cannot be null or blank");
        }
        return color;
    }

    private static UUID requireOwner(UUID ownerUUID) {
        if (ownerUUID == null) {
            throw new IllegalArgumentException("ownerUUID cannot be null");
        }
        return ownerUUID;
    }
}
