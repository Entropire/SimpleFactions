package com.entropire.simplefactions;

import com.entropire.simplefactions.command.SimpleFactionCommand;
import com.entropire.simplefactions.database.DataBaseContext;
import com.entropire.simplefactions.faction.FactionService;
import com.entropire.simplefactions.faction.membership.FactionMembershipService;
import com.entropire.simplefactions.listener.PlayerJoinListener;
import com.entropire.simplefactions.player.FactionPlayerService;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class SimpleFactions extends JavaPlugin {
    private static Logger logger;
    private DataBaseContext db;

    @Override
    public void onEnable() {
        logger = getLogger();

        if (!getDataFolder().exists()) getDataFolder().mkdir();
        db = new DataBaseContext(getDataFolder().getAbsolutePath() + "/Simple-Faction.db");
        db.initSchema();

        FactionService factionService = new FactionService();
        FactionMembershipService factionMembershipService = new FactionMembershipService();
        FactionPlayerService playerService = new FactionPlayerService();

        FactionApplication factionApplication = new FactionApplication(db, factionService, factionMembershipService);

        getCommand("simplefactions").setExecutor(new SimpleFactionCommand(factionApplication));
        getCommand("simplefactions").setTabCompleter(new SimpleFactionCommand(factionApplication));

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(db, playerService), this);
    }

    @Override
    public void onDisable() {
    }

    public static Logger getPluginLogger() {
        return logger;
    }
}
