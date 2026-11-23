package com.entropire.simplefactions.faction;

import java.sql.Connection;
import java.util.List;
import java.util.UUID;

import com.entropire.simplefactions.objects.Pageable;
import com.entropire.simplefactions.objects.Result;
import com.entropire.simplefactions.player.FactionPlayer;

public class FactionService {
    private final FactionRepository repository = new FactionRepository();

    public Result<Faction> save(Connection connection, Faction faction) {
        return repository.save(connection, faction);
    }

    public Result<Boolean> delete(Connection connection, UUID uuid) {
       return repository.delete(connection, uuid);
    }  

    public Result<List<Faction>> getFactions(Connection connection, Pageable<Faction> pageable){
        return repository.getFactions(connection, pageable);
    }

    public Result<Integer> getFactionsCount(Connection connection){
        return repository.getFactionsCount(connection);
    }

    public Result<FactionPlayer> getOwner(Connection connection, String factionName){
        return repository.getOwner(connection, factionName);
    }

    public Result<List<String>> getFactionNames(Connection connection, Pageable<String> pageable, String contains){
        return repository.getFactionNames(connection, pageable, contains);
    }

    public Result<Faction> getByName(Connection connection, String factionName){
        return repository.getByName(connection, factionName);
    }

    public Result<List<FactionPlayer>> getFactionMembers(Connection connection, String factionName, Pageable<FactionPlayer> pageable){
        return repository.getFactionMembers(connection, factionName, pageable);
    }

    public Result<Integer> getFactionMembersCount(Connection connection, String factionName){
        return repository.getFactionMembersCount(connection, factionName);
    }

}
