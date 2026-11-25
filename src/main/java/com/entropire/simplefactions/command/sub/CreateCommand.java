package com.entropire.simplefactions.command.sub;

import java.util.List;
import java.util.logging.Level;

import com.entropire.simplefactions.SimpleFactions;
import com.entropire.simplefactions.chat.ChatMessage;
import com.entropire.simplefactions.faction.Faction;
import com.entropire.simplefactions.objects.Result;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.entropire.simplefactions.faction.FactionService;
import com.entropire.simplefactions.command.CommandNode;

public class CreateCommand extends CommandNode{
    
    private final FactionService factionService;
    
    public CreateCommand(FactionService factionService){
        super("create", "Create a new faction.");
        this.factionService = factionService;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args){
        if (!(sender instanceof Player player))
        {
            sender.sendMessage("Only players can preform this command!");
            return false;
        }
        
        if(args.length < 1){
            help(args).sendTo(player);
            return true;
        }

        String color = args.length > 1 ? args[1] : "#ffffff";

        Result<Faction> result = factionService.createFaction(args[0], color, player.getUniqueId());

        if (result instanceof Result.Success<Faction> success) {
            player.sendMessage("Created faction " + success.value().name().value() + ".");
        } else if (result instanceof Result.Failure<Faction> failure) {
            player.sendMessage(failure.message());
        } else if (result instanceof Result.Error<Faction> error) {
            SimpleFactions.getPluginLogger().log(Level.SEVERE, error.message(), error.exception());
            player.sendMessage("Something went wrong while creating your faction.");
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args){
        return List.of();
    }

    @Override
    public ChatMessage help(String[] args) {
        return new ChatMessage().text("/f create <FactionName>");
    }
}