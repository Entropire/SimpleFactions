package com.entropire.simplefactions;

import com.entropire.simplefactions.command.DatabaseResetCommand;
import com.entropire.simplefactions.command.SimpleFactionCommand;
import com.entropire.simplefactions.database.DataBaseContext;
import com.entropire.simplefactions.database.SchemaStatus;
import com.entropire.simplefactions.faction.FactionRepository;
import com.entropire.simplefactions.faction.FactionService;
import com.entropire.simplefactions.faction.membership.FactionMembershipRepository;
import com.entropire.simplefactions.faction.membership.FactionMembershipService;
import com.entropire.simplefactions.listener.PlayerJoinListener;
import com.entropire.simplefactions.objects.Result;
import com.entropire.simplefactions.player.FactionPlayerRepository;
import com.entropire.simplefactions.player.FactionPlayerService;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class SimpleFactions extends JavaPlugin {
    private static final String DATABASE_FILE_NAME = "Simple-Faction.db";

    private static Logger logger;
    private DataBaseContext db;

    @Override
    public void onEnable() {
        logger = getLogger();

        if (!getDataFolder().exists()) getDataFolder().mkdir();
        db = new DataBaseContext(databaseFile().getAbsolutePath());

        Result<SchemaStatus> schemaResult = db.initSchema(pluginVersion());
        if (schemaResult instanceof Result.Success<SchemaStatus> success) {
            if (success.value() instanceof SchemaStatus.Incompatible incompatible) {
                startSafeMode(incompatible);
            } else {
                startPlugin();
            }
        } else if (schemaResult instanceof Result.Failure<SchemaStatus> failure) {
            logger.severe("Could not set up the database: " + failure.message());
            getServer().getPluginManager().disablePlugin(this);
        } else if (schemaResult instanceof Result.Error<SchemaStatus> error) {
            logger.log(Level.SEVERE, "Could not set up the database: " + error.message(), error.exception());
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    public void startPlugin() {
        FactionRepository factionRepository = new FactionRepository();
        FactionMembershipRepository membershipRepository = new FactionMembershipRepository();
        FactionPlayerRepository playerRepository = new FactionPlayerRepository();

        FactionService factionService = new FactionService(db, factionRepository, membershipRepository);
        FactionMembershipService membershipService = new FactionMembershipService(db, membershipRepository);
        FactionPlayerService playerService = new FactionPlayerService(db, playerRepository);

        SimpleFactionCommand factionCommand = new SimpleFactionCommand(factionService, membershipService);
        PluginCommand command = getCommand("faction");
        command.setExecutor(factionCommand);
        command.setTabCompleter(factionCommand);

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(playerService), this);
    }

    private void startSafeMode(SchemaStatus.Incompatible incompatible) {
        DatabaseResetCommand resetCommand = new DatabaseResetCommand(db, pluginVersion(), databasePath(), this::startPlugin);
        PluginCommand command = getCommand("faction");
        command.setExecutor(resetCommand);
        command.setTabCompleter(resetCommand);

        String createdBy = incompatible.createdByPluginVersion() == null
                ? "an older development build of SimpleFactions (made before database versions were tracked)"
                : "SimpleFactions " + incompatible.createdByPluginVersion();
        String keepDataOption = incompatible.createdByPluginVersion() == null
                ? "use the SimpleFactions version that created this file instead of this jar."
                : "use SimpleFactions " + incompatible.createdByPluginVersion() + " instead of this jar.";

        String line = "=".repeat(72);
        logger.warning(line);
        logger.warning("SimpleFactions " + pluginVersion() + " can't use the existing database file:");
        logger.warning("    " + databasePath());
        logger.warning("That file was created by " + createdBy + ".");
        logger.warning("It has database version " + incompatible.foundSchemaVersion()
                + ", this version of SimpleFactions needs database version " + DataBaseContext.SCHEMA_VERSION + ".");
        logger.warning("");
        logger.warning("You have 2 options:");
        logger.warning("  1. Keep your factions: " + keepDataOption);
        logger.warning("  2. Start over with an empty database: run this command in the console:");
        logger.warning("         f database reset");
        logger.warning("     WARNING: this removes ALL current factions, members, invites and join requests!");
        logger.warning("     The old database file is kept as a backup next to the new one.");
        logger.warning("");
        logger.warning("SimpleFactions is disabled until you choose one of these options.");
        logger.warning(line);
    }

    private File databaseFile() {
        return new File(getDataFolder(), DATABASE_FILE_NAME);
    }

    private String databasePath() {
        return databaseFile().getPath().replace('\\', '/');
    }

    private String pluginVersion() {
        return getDescription().getVersion();
    }

    public static Logger getPluginLogger() {
        return logger;
    }
}
