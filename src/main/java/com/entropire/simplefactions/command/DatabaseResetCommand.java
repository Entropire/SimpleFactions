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
        Result<Path> resetResult = db.resetDatabase();
        if (!(resetResult instanceof Result.Success<Path> resetSuccess)) {
            if (resetResult instanceof Result.Failure<Path> failure) {
                sender.sendMessage("Could not reset the database: " + failure.message());
            } else if (resetResult instanceof Result.Error<Path> error) {
                SimpleFactions.getPluginLogger().log(Level.SEVERE, error.message(), error.exception());
                sender.sendMessage("Could not reset the database, see the server log for details.");
            }
            return;
        }
        Path backup = resetSuccess.value();

        Result<SchemaStatus> schemaResult = db.initSchema(pluginVersion);
        if (schemaResult instanceof Result.Success<SchemaStatus> schemaSuccess) {
            if (schemaSuccess.value() instanceof SchemaStatus.Incompatible) {
                sender.sendMessage("The database was reset, but is still not compatible. This should not happen, please report it.");
            } else {
                onReady.run();
                sender.sendMessage("Database reset. The old file was saved as " + backup.getFileName() + ".");
                sender.sendMessage("SimpleFactions is enabled again.");
            }
        } else if (schemaResult instanceof Result.Failure<SchemaStatus> failure) {
            sender.sendMessage("The database was reset, but could not be set up: " + failure.message());
        } else if (schemaResult instanceof Result.Error<SchemaStatus> error) {
            SimpleFactions.getPluginLogger().log(Level.SEVERE, error.message(), error.exception());
            sender.sendMessage("The database was reset, but could not be set up. See the server log for details.");
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
