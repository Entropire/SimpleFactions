package com.entropire.simplefactions;

import com.entropire.simplefactions.db.DataBaseContext;
import com.entropire.simplefactions.db.faction.FactionRepository;
import com.entropire.simplefactions.db.faction.FactionService;
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

        var factionRepository = new FactionRepository(db);
        var factionService = new FactionService(factionRepository);
    }

    @Override
    public void onDisable() {
        db.closeConnection();
    }

    public static Logger getPluginLogger() {
        return logger;
    }
}
