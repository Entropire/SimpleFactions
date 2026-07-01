package com.entropire.simplefactions.db.player;

import com.entropire.simplefactions.db.player.exception.PlayerException;

public class PlayerService {
    private final PlayerRepository repository;

    public PlayerService(PlayerRepository repository) {
        this.repository = repository;
    }

    public void save(Player player) throws PlayerException {
        repository.save(player);
    }
}
