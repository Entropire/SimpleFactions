package com.entropire.simplefactions.command;

import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabCompleter;
import org.jspecify.annotations.NonNull;

import com.entropire.simplefactions.SimpleFactions;
import com.entropire.simplefactions.database.DataBaseContext;
import com.entropire.simplefactions.database.SchemaStatus;
import com.entropire.simplefactions.objects.Result;

public class DatabaseResetCommand implements CommandExecutor, TabCompleter {

    private final DataBaseContext db;
    private final String pluginVersion;
    private final String databasePath;
    private final Runnable onReady;

    public DatabaseResetCommand(DataBaseContext db, String pluginVersion, String databasePath, Runnable onReady) {
        this.db = db;
        this.pluginVersion = pluginVersion;
        this.databasePath = databasePath;
        this.onReady = onReady;
    }

    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String alias, String[] args) {
        if (!(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage("SimpleFactions is disabled: its database file isn't compatible with this version. A server admin can fix this from the console.");
            return true;
        }

        boolean isReset = args.length >= 2 && args[0].equalsIgnoreCase("database") && args[1].equalsIgnoreCase("reset");
        boolean isConfirmed = isReset && args.length >= 3 && args[2].equalsIgnoreCase("confirm");

        if (!isReset) {
            sender.sendMessage("SimpleFactions is disabled: the database file " + databasePath + " isn't compatible with this version.");
            sender.sendMessage("See the warning in the server log for your options, or run: f database reset");
            return true;
        }

        if (!isConfirmed) {
            sender.sendMessage("WARNING: this removes ALL current factions, members, invites and join requests!");
            sender.sendMessage("The old database file " + databasePath + " is kept as a backup, but SimpleFactions won't use it anymore.");
            sender.sendMessage("To continue, run: f database reset confirm");
            return true;
        }

        reset(sender);
        return true;
    }

    private void reset(CommandSender sender) {
        Path backup;
        switch (db.resetDatabase()) {
            case Result.Success<Path>(Path value) -> backup = value;
            case Result.Failure<Path>(String message) -> {
                sender.sendMessage("Could not reset the database: " + message);
                return;
            }
            case Result.Error<Path>(String message, Exception e) -> {
                SimpleFactions.getPluginLogger().log(Level.SEVERE, message, e);
                sender.sendMessage("Could not reset the database, see the server log for details.");
                return;
            }
        }

        switch (db.initSchema(pluginVersion)) {
            case Result.Success<SchemaStatus>(SchemaStatus.Ready ready) -> {
                onReady.run();
                sender.sendMessage("Database reset. The old file was saved as " + backup.getFileName() + ".");
                sender.sendMessage("SimpleFactions is enabled again.");
            }
            case Result.Success<SchemaStatus>(SchemaStatus.Incompatible incompatible) ->
                    sender.sendMessage("The database was reset, but is still not compatible. This should not happen, please report it.");
            case Result.Failure<SchemaStatus>(String message) ->
                    sender.sendMessage("The database was reset, but could not be set up: " + message);
            case Result.Error<SchemaStatus>(String message, Exception e) -> {
                SimpleFactions.getPluginLogger().log(Level.SEVERE, message, e);
                sender.sendMessage("The database was reset, but could not be set up. See the server log for details.");
            }
        }
    }

    @Override
    public List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command command, @NonNull String alias, String[] args) {
        if (!(sender instanceof ConsoleCommandSender)) return List.of();

        return switch (args.length) {
            case 1 -> List.of("database");
            case 2 -> List.of("reset");
            case 3 -> List.of("confirm");
            default -> List.of();
        };
    }
}
