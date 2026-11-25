package com.entropire.simplefactions.command.sub;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import com.entropire.simplefactions.SimpleFactions;
import com.entropire.simplefactions.chat.ChatMessage;
import com.entropire.simplefactions.objects.Result;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.entropire.simplefactions.faction.FactionService;
import com.entropire.simplefactions.command.CommandNode;

public class DeleteCommand extends CommandNode{

    private final FactionService factionService;

    public DeleteCommand(FactionService factionService){
        super("delete", "Delete your faction.");
        this.factionService = factionService;
    }
    
    @Override
    public boolean execute(CommandSender sender, String[] args){
        if (!(sender instanceof Player player))
        {
            sender.sendMessage("Only players can preform this command!");
            return false;
        }
        
        switch (factionService.deleteFaction(player.getUniqueId())) {
            case Result.Success<List<UUID>>(List<UUID> memberUuids) -> {
                player.sendMessage("You have deleted your faction.");
                for (UUID uuid : memberUuids) {
                    if (uuid.equals(player.getUniqueId())) continue;
                    Player member = Bukkit.getPlayer(uuid);
                    if (member != null) member.sendMessage("Your faction has been deleted!");
                }
            }
            case Result.Failure<List<UUID>>(String message) -> player.sendMessage(message);
            case Result.Error<List<UUID>>(String message, Exception e) -> {
                SimpleFactions.getPluginLogger().log(Level.SEVERE, message, e);
                player.sendMessage("Something went wrong while deleting your faction.");
            }
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args){
        return List.of();
    }

    @Override
    public ChatMessage help(String[] args) {
        return new ChatMessage().text("/f delete");
    }
}
