package com.entropire.simplefactions.player;

import java.time.Instant;
import java.util.UUID;

import com.entropire.simplefactions.database.DataBaseContext;
import com.entropire.simplefactions.objects.Result;

public class FactionPlayerService {
    private final DataBaseContext db;
    private final FactionPlayerRepository playerRepository;

    public FactionPlayerService(DataBaseContext db, FactionPlayerRepository playerRepository) {
        this.db = db;
        this.playerRepository = playerRepository;
    }

    public Result<Boolean> registerJoin(UUID playerUuid, String username) {
        Instant now = Instant.now();

        return db.transaction(conn -> {
            Result<Boolean> saveResult = playerRepository.saveIfNotExists(conn, new FactionPlayer(playerUuid, username, ChatMode.PUBLIC, now));
            if (!saveResult.isSuccess()) return saveResult;

            return playerRepository.updatePlayerLastSeen(conn, playerUuid, now);
        });
    }

    public Result<Boolean> registerLeave(UUID playerUuid) {
        return db.withConnection(conn -> playerRepository.updatePlayerLastSeen(conn, playerUuid, Instant.now()));
    }
}
