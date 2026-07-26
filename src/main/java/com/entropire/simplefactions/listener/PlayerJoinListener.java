package com.entropire.simplefactions.listener;

import java.time.Instant;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.entropire.simplefactions.database.DataBaseContext;
import com.entropire.simplefactions.player.FactionPlayerService;
import com.entropire.simplefactions.player.ChatMode;
import com.entropire.simplefactions.player.FactionPlayer;

public class PlayerJoinListener implements Listener {
    
    private final DataBaseContext db;
    private final FactionPlayerService playerService;

    public PlayerJoinListener(DataBaseContext db, FactionPlayerService playerService){
        this.db = db;
        this.playerService = playerService;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        org.bukkit.entity.Player player = (org.bukkit.entity.Player) event.getPlayer();
        db.withConnection(conn -> {
            playerService.saveIfNotExists(conn, new FactionPlayer(player.getUniqueId(), player.getName(), ChatMode.PUBLIC, Instant.now()));
            playerService.updatePlayerLastSeen(conn, player.getUniqueId(), Instant.now());
            return null;
        });
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event){
        org.bukkit.entity.Player player = (org.bukkit.entity.Player) event.getPlayer();
        db.withConnection(conn -> {
            playerService.updatePlayerLastSeen(conn, player.getUniqueId(), Instant.now());
            return null;
        });
    }
}
