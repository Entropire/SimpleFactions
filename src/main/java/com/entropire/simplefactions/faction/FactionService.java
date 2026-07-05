package com.entropire.simplefactions.faction;

import java.sql.Connection;
import java.util.UUID;

import com.entropire.simplefactions.faction.exception.FactionException;
import com.entropire.simplefactions.faction.exception.FactionNameDuplicateException;
import com.entropire.simplefactions.faction.exception.FactionOwnerDuplicateException;

public class FactionService {
    private final FactionRepository repository = new FactionRepository();

    public Faction save(Connection connection, Faction faction) throws FactionOwnerDuplicateException, FactionNameDuplicateException, FactionException {
        return repository.save(connection, faction);
    }

    public void delete(Connection connection, UUID uuid) throws FactionException {
       repository.delete(connection, uuid); 
    }  
}
