package com.entropire.simplefactions.player;

import java.sql.Connection;
import java.time.Instant;
import java.util.UUID;

import com.entropire.simplefactions.player.exception.PlayerException;

public class FactionPlayerService {
    private final FactionPlayerRepository repository = new FactionPlayerRepository();

    public void saveIfNotExists(Connection connection, FactionPlayer player) throws PlayerException {
        repository.saveIfNotExists(connection, player);
    }

    public void updatePlayerLastSeen(Connection connection, UUID uuid, Instant lastseen){
        repository.updatePlayerLastSeen(connection, uuid, lastseen);
    }
}
