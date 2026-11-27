package com.entropire.simplefactions.listener;

import java.util.logging.Level;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.entropire.simplefactions.SimpleFactions;
import com.entropire.simplefactions.objects.Result;
import com.entropire.simplefactions.player.FactionPlayerService;

public class PlayerJoinListener implements Listener {

    private final FactionPlayerService playerService;

    public PlayerJoinListener(FactionPlayerService playerService){
        this.playerService = playerService;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        Result<Boolean> result = playerService.registerJoin(player.getUniqueId(), player.getName());
        logIfFailed(result, "Failed to register player " + player.getName() + " on join");
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event){
        Player player = event.getPlayer();
        Result<Boolean> result = playerService.registerLeave(player.getUniqueId());
        logIfFailed(result, "Failed to update last seen of player " + player.getName() + " on leave");
    }

    private void logIfFailed(Result<?> result, String context) {
        if (result instanceof Result.Failure<?> failure) {
            SimpleFactions.getPluginLogger().warning(context + ": " + failure.message());
        } else if (result instanceof Result.Error<?> error) {
            SimpleFactions.getPluginLogger().log(Level.SEVERE, context + ": " + error.message(), error.exception());
        }
    }
}
