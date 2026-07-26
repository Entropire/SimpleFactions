package com.entropire.simplefactions.command.sub;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.entropire.simplefactions.FactionApplication;
import com.entropire.simplefactions.command.CommandNode;
import com.entropire.simplefactions.player.FactionPlayer;

public class OwnerCommand extends CommandNode {
    
    private FactionApplication factionApplication;
    
    public OwnerCommand(FactionApplication factionApplication){
        this.factionApplication = factionApplication;
    }

    @Override
    public boolean execute(CommandSender sender, Command command, String alias, String[] args){
        if (!(sender instanceof Player player))
        {  
            sender.sendMessage("Only players can preform this command!");
            return true;
        }

        if(args.length < 1){
            player.sendMessage("/faction owner <faction name>");
            return true;
        }

        FactionPlayer factionPlayer = factionApplication.getFactionOwner(player, args[0]);
        
        if(factionPlayer == null){
            player.sendMessage("Could not find the owner of faction " + args[0]);
        }
        else{
            player.sendMessage(factionPlayer.username());
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args){
        return List.of();
    }
}
