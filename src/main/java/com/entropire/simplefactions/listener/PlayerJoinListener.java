package com.entropire.simplefactions.listener;

import java.time.Instant;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.entropire.simplefactions.database.DataBaseContext;
import com.entropire.simplefactions.player.PlayerService;
import com.entropire.simplefactions.player.ChatMode;
import com.entropire.simplefactions.player.Player;

public class PlayerJoinListener implements Listener {
    
    private final DataBaseContext db;
    private final PlayerService playerService;

    public PlayerJoinListener(DataBaseContext db, PlayerService playerService){
        this.db = db;
        this.playerService = playerService;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        org.bukkit.entity.Player player = (org.bukkit.entity.Player) event.getPlayer();
        db.withConnection(conn -> {
            playerService.saveIfNotExists(conn, new Player(player.getUniqueId(), player.getName(), ChatMode.PUBLIC, Instant.now()));
            playerService.updatePlayerLastSeen(conn, player.getUniqueId(), Instant.now());
        });


    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event){
        org.bukkit.entity.Player player = (org.bukkit.entity.Player) event.getPlayer();
        db.withConnection(conn -> {
            playerService.updatePlayerLastSeen(conn, player.getUniqueId(), Instant.now());
        });
    }
}
