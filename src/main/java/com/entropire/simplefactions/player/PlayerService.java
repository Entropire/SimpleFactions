package com.entropire.simplefactions.player;

import java.sql.Connection;

import com.entropire.simplefactions.player.exception.PlayerException;

public class PlayerService {
    private final PlayerRepository repository = new PlayerRepository();

    public void save(Connection connection, Player player) throws PlayerException {
        repository.save(connection, player);
    }
}
