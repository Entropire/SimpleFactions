package com.entropire.simplefactions.faction.invite;

import java.sql.Connection;

import com.entropire.simplefactions.objects.Result;

public class FactionInviteService {
    private final FactionInviteRepository repository = new FactionInviteRepository();

    public Result<Boolean> save(Connection connection, FactionInvite factionInvite) {
        return repository.save(connection, factionInvite);
    }
}
