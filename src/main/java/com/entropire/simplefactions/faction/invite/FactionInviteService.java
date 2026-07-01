package com.entropire.simplefactions.faction.invite;

import com.entropire.simplefactions.faction.invite.exception.FactionInviteException;

public class FactionInviteService {
    private final FactionInviteRepository repository;

    public FactionInviteService(FactionInviteRepository repository) {
        this.repository = repository;
    }

    public void save(FactionInvite factionInvite) throws FactionInviteException {
        repository.save(factionInvite);
    }
}
