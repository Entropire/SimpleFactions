package com.entropire.simplefactions.command.sub;

import java.util.List;

import com.entropire.simplefactions.chat.ChatMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.entropire.simplefactions.FactionApplication;
import com.entropire.simplefactions.command.CommandNode;
import com.entropire.simplefactions.player.FactionPlayer;

public class OwnerCommand extends CommandNode {
    
    private final FactionApplication factionApplication;
    
    public OwnerCommand(FactionApplication factionApplication){
        super("owner", "Return the owner of the faction.");
        this.factionApplication = factionApplication;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args){
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
    public List<String> tabComplete(CommandSender sender){
        return List.of();
    }

    @Override
    public ChatMessage help(String[] args) {
        return new ChatMessage().text("/f owner <FactionName>");
    }
}
