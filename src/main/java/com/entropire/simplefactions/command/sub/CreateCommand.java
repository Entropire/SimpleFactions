package com.entropire.simplefactions.command.sub;

import java.util.List;
import java.util.logging.Level;

import com.entropire.simplefactions.SimpleFactions;
import com.entropire.simplefactions.chat.ChatMessage;
import com.entropire.simplefactions.faction.Faction;
import com.entropire.simplefactions.objects.Result;
import it.unimi.dsi.fastutil.ints.IntLists;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.entropire.simplefactions.FactionApplication;
import com.entropire.simplefactions.command.CommandNode;

public class CreateCommand extends CommandNode{
    
    private final FactionApplication factionApplication;
    
    public CreateCommand(FactionApplication factionApplication){
        super("create", "Create a new faction.");
        this.factionApplication = factionApplication;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args){
        if (!(sender instanceof Player player))
        {
            sender.sendMessage("Only players can preform this command!");
            return false;
        }
        
        if(args.length < 1){
            player.sendMessage(help(args).build());
            return true;
        }

        String color = args.length > 1 ? args[1] : "#ffffff";

        Result<Faction> result = factionApplication.createFaction(args[0], color, player.getUniqueId());

        switch (result) {
            case Result.Success<Faction>(Faction faction) ->
                    player.sendMessage("Created faction " + faction.name() + ".");
            case Result.Failure<Faction>(String message) ->
                    player.sendMessage(message);
            case Result.Error<Faction>(String message, Exception e) -> {
                SimpleFactions.getPluginLogger().log(Level.SEVERE, message, e);
                player.sendMessage("Something went wrong while creating your faction.");
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
        return new ChatMessage().text("/f create <FactionName>");
    }
}