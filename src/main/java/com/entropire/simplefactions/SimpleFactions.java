package com.entropire.simplefactions;

import com.entropire.simplefactions.command.SimpleFactionCommand;
import com.entropire.simplefactions.database.DataBaseContext;
import com.entropire.simplefactions.faction.FactionService;
import com.entropire.simplefactions.faction.membership.FactionMembershipService;
import com.entropire.simplefactions.listener.PlayerJoinListener;
import com.entropire.simplefactions.objects.Result;
import com.entropire.simplefactions.player.FactionPlayerService;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class SimpleFactions extends JavaPlugin {
    private static Logger logger;
    private DataBaseContext db;

    @Override
    public void onEnable() {
        logger = getLogger();

        if (!getDataFolder().exists()) getDataFolder().mkdir();
        db = new DataBaseContext(getDataFolder().getAbsolutePath() + "/Simple-Faction.db");

        switch (db.initSchema()) {
            case Result.Success<Boolean> success -> {}
            case Result.Failure<Boolean>(String message) -> {
                logger.severe("Could not set up the database: " + message);
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
            case Result.Error<Boolean>(String message, Exception e) -> {
                logger.log(Level.SEVERE, "Could not set up the database: " + message, e);
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
        }

        FactionService factionService = new FactionService();
        FactionMembershipService factionMembershipService = new FactionMembershipService();
        FactionPlayerService playerService = new FactionPlayerService();

        FactionApplication factionApplication = new FactionApplication(db, factionService, factionMembershipService);

        getCommand("faction").setExecutor(new SimpleFactionCommand(factionApplication));
        getCommand("faction").setTabCompleter(new SimpleFactionCommand(factionApplication));

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(db, playerService), this);
    }

    @Override
    public void onDisable() {
    }

    public static Logger getPluginLogger() {
        return logger;
    }
}
