package com.entropire.simplefactions.command.sub;

import java.util.List;

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
        
        if(args.length > 0){
            String color = args.length > 1 ? args[1] : "#ffffff";
         
            factionApplication.createFaction(args[0], color, player);
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender){
        return List.of();
    }

    @Override
    public String help(String[] args) {
        return "/f create <FactionName>";
    }
}