package com.entropire.simplefactions.faction.invite;

import java.sql.Connection;

import com.entropire.simplefactions.faction.invite.exception.FactionInviteException;

public class FactionInviteService {
    private final FactionInviteRepository repository = new FactionInviteRepository();

    public void save(Connection connection, FactionInvite factionInvite) throws FactionInviteException {
        repository.save(connection, factionInvite);
    }
}
