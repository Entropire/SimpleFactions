package com.entropire.simplefactions.db.factioninvite;

import com.entropire.simplefactions.db.factioninvite.exception.FactionInviteException;

public class FactionInviteService {
    private final FactionInviteRepository repository;

    public FactionInviteService(FactionInviteRepository repository) {
        this.repository = repository;
    }

    public void save(FactionInvite factionInvite) throws FactionInviteException {
        repository.save(factionInvite);
    }
}
