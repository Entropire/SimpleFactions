package com.entropire.simplefactions.listener;

import java.time.Instant;
import java.util.logging.Level;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.entropire.simplefactions.SimpleFactions;
import com.entropire.simplefactions.database.DataBaseContext;
import com.entropire.simplefactions.objects.Result;
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
        Player player = event.getPlayer();
        Instant now = Instant.now();

        Result<Boolean> result = db.transaction(conn -> {
            Result<Boolean> saveResult = playerService.saveIfNotExists(conn, new FactionPlayer(player.getUniqueId(), player.getName(), ChatMode.PUBLIC, now));
            if (!saveResult.isSuccess()) return saveResult;

            return playerService.updatePlayerLastSeen(conn, player.getUniqueId(), now);
        });

        logIfFailed(result, "Failed to register player " + player.getName() + " on join");
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event){
        Player player = event.getPlayer();

        Result<Boolean> result = db.withConnection(conn ->
                playerService.updatePlayerLastSeen(conn, player.getUniqueId(), Instant.now()));

        logIfFailed(result, "Failed to update last seen of player " + player.getName() + " on leave");
    }

    private void logIfFailed(Result<?> result, String context) {
        switch (result) {
            case Result.Success<?> success -> {}
            case Result.Failure<?>(String message) ->
                    SimpleFactions.getPluginLogger().warning(context + ": " + message);
            case Result.Error<?>(String message, Exception e) ->
                    SimpleFactions.getPluginLogger().log(Level.SEVERE, context + ": " + message, e);
        }
    }
}
