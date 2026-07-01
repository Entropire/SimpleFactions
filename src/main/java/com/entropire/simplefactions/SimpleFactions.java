package com.entropire.simplefactions;

import com.entropire.simplefactions.db.DataBaseContext;
import org.bukkit.plugin.java.JavaPlugin;

public final class SimpleFactions extends JavaPlugin {
    private DataBaseContext db;

    @Override
    public void onEnable() {
        if (!getDataFolder().exists()) getDataFolder().mkdir();
        db = new DataBaseContext(getDataFolder().getAbsolutePath() + "/Simple-Faction.db");
        db.initSchema();
    }

    @Override
    public void onDisable() {
        db.closeConnection();
    }
}
