package com.entropire.simplefactions.faction;

import java.sql.Connection;

import com.entropire.simplefactions.faction.exception.FactionException;

public class FactionService {
    private final FactionRepository repository = new FactionRepository();

    public Faction save(Connection connection, Faction faction) throws FactionException {
        return repository.save(connection, faction);
    }
}
