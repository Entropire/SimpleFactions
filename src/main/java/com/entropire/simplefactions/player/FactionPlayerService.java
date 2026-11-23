package com.entropire.simplefactions.player;

import java.sql.Connection;
import java.time.Instant;
import java.util.UUID;

import com.entropire.simplefactions.objects.Result;

public class FactionPlayerService {
    private final FactionPlayerRepository repository = new FactionPlayerRepository();

    public Result<Boolean> saveIfNotExists(Connection connection, FactionPlayer player) {
        return repository.saveIfNotExists(connection, player);
    }

    public Result<Boolean> updatePlayerLastSeen(Connection connection, UUID uuid, Instant lastseen){
        return repository.updatePlayerLastSeen(connection, uuid, lastseen);
    }
}
