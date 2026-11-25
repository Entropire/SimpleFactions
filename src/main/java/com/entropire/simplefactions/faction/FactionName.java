package com.entropire.simplefactions.faction;

import com.entropire.simplefactions.objects.Result;

public record FactionName(String value) {
    private static final String PATTERN = "^[a-z0-9_]{3,16}$";

    public FactionName {
        if (value == null || !value.matches(PATTERN)) {
            throw new IllegalArgumentException("Invalid faction name: " + value);
        }
    }

    public static Result<FactionName> parse(String input) {
        if (input == null || input.isBlank()) return new Result.Failure<>("A faction name can't be empty.");
        if (!input.matches(PATTERN)) return new Result.Failure<>("A faction name must be 3-16 characters: lowercase letters, numbers or _.");
        return new Result.Success<>(new FactionName(input));
    }

    @Override
    public String toString() { return value; }
}
    