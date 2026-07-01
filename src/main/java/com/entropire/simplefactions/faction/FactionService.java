package com.entropire.simplefactions.faction;

import com.entropire.simplefactions.faction.exception.FactionException;

public class FactionService {
    private final FactionRepository repository;

    public FactionService(FactionRepository repository) {
        this.repository = repository;
    }

    public void save(Faction faction) throws FactionException {
        repository.save(faction);
    }
}
