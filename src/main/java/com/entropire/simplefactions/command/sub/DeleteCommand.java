package com.entropire.simplefactions.command.sub;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.entropire.simplefactions.FactionApplication;
import com.entropire.simplefactions.command.CommandNode;

public class DeleteCommand extends CommandNode{

    private final FactionApplication factionApplication;

    public DeleteCommand(FactionApplication factionApplication){
        super("delete", "Delete your faction.");
        this.factionApplication = factionApplication;
    }
    
    @Override
    public boolean execute(CommandSender sender, String[] args){
        if (!(sender instanceof Player player))
        {
            sender.sendMessage("Only players can preform this command!");
            return false;
        }
        
        factionApplication.deleteFaction(player);
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender){
        return List.of();
    }

    @Override
    public String help(String[] args) {
        return "/f delete";
    }
}
