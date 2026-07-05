package com.entropire.simplefactions.faction;

import java.sql.Connection;
import java.util.List;
import java.util.UUID;

import com.entropire.simplefactions.faction.exception.FactionException;
import com.entropire.simplefactions.objects.Pageable;

public class FactionService {
    private final FactionRepository repository = new FactionRepository();

    public Faction save(Connection connection, Faction faction) throws FactionException {
        return repository.save(connection, faction);
    }

    public void delete(Connection connection, UUID uuid) throws FactionException {
       repository.delete(connection, uuid); 
    }  

    public List<Faction> getFactions(Connection connection, Pageable<Faction> pageable){
        return repository.getFactions(connection, pageable);
    }

    public int getFactionsCount(Connection connection){
        return repository.getfactionsCount(connection);
    }
}
