package com.entropire.simplefactions.player;

import com.entropire.simplefactions.player.exception.PlayerException;

public class PlayerService {
    private final PlayerRepository repository;

    public PlayerService(PlayerRepository repository) {
        this.repository = repository;
    }

    public void save(Player player) throws PlayerException {
        repository.save(player);
    }
}
