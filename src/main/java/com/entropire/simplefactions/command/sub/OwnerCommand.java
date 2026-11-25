package com.entropire.simplefactions.command.sub;

import java.util.List;
import java.util.logging.Level;

import com.entropire.simplefactions.SimpleFactions;
import com.entropire.simplefactions.chat.ChatMessage;
import com.entropire.simplefactions.objects.Pageable;
import com.entropire.simplefactions.objects.Result;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.entropire.simplefactions.faction.FactionService;
import com.entropire.simplefactions.command.CommandNode;
import com.entropire.simplefactions.player.FactionPlayer;

public class OwnerCommand extends CommandNode {
    
    private final FactionService factionService;
    
    public OwnerCommand(FactionService factionService){
        super("owner", "Return the owner of the faction.");
        this.factionService = factionService;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args){
        if (!(sender instanceof Player player))
        {  
            sender.sendMessage("Only players can preform this command!");
            return true;
        }

        Result<FactionPlayer> result = args.length < 1
                ? factionService.getPlayerFactionOwner(player.getUniqueId())
                : factionService.getFactionOwner(args[0]);

        switch (result) {
            case Result.Success<FactionPlayer>(FactionPlayer owner) -> player.sendMessage(owner.username());
            case Result.Failure<FactionPlayer>(String message) -> player.sendMessage(message);
            case Result.Error<FactionPlayer>(String message, Exception e) -> {
                SimpleFactions.getPluginLogger().log(Level.SEVERE, message, e);
                player.sendMessage("Something went wrong while retrieving the owner of the faction.");
            }
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length != 1 || args[0].isEmpty()) return List.of();

        return switch (factionService.getFactionNames(new Pageable<>(0, 20), args[0])) {
            case Result.Success<Pageable<String>>(Pageable<String> factionNames) -> factionNames.items();
            case Result.Failure<Pageable<String>> failure -> List.of();
            case Result.Error<Pageable<String>>(String message, Exception e) -> {
                SimpleFactions.getPluginLogger().log(Level.SEVERE, message, e);
                yield List.of();
            }
        };
    }

    @Override
    public ChatMessage help(String[] args) {
        return new ChatMessage().text("/f owner <faction name>");
    }
}
