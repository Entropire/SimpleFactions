package com.entropire.simplefactions.command.sub;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.entropire.simplefactions.FactionApplication;
import com.entropire.simplefactions.command.CommandNode;

public class CreateCommand extends CommandNode{
    
    private FactionApplication factionApplication;
    
    public CreateCommand(FactionApplication factionApplication){
        this.factionApplication = factionApplication;
    }

    @Override
    public boolean execute(CommandSender sender, Command command, String alias, String[] args){
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
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args){
        return List.of();
    }
}