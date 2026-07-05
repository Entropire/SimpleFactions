package com.entropire.simplefactions.command.sub;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.entropire.simplefactions.FactionApplication;
import com.entropire.simplefactions.command.CommandNode;
import com.entropire.simplefactions.faction.Faction;
import com.entropire.simplefactions.objects.Pageable;

public class ListCommand extends CommandNode {

    private FactionApplication factionApplication;
    
    public ListCommand(FactionApplication factionApplication){
        this.factionApplication = factionApplication;
    }

    @Override
    public boolean execute(CommandSender sender, Command command, String alias, String[] args){
        if (!(sender instanceof Player player))
        {
            sender.sendMessage("Only players can preform this command!");
            return false;
        }
        
        int page = args.length == 0 ? 0 : Math.max(0, Integer.parseInt(args[0]) - 1);

        Pageable<Faction> factionsPage = factionApplication.getFactions(player, new Pageable<Faction>(page, 10));

        sender.sendMessage("=-=-=-=-=- Factions -=-=-=-=-=\npage " + (factionsPage.currentPage() + 1) + "/" + factionsPage.maxPages() + "\n");
        factionsPage.items().forEach(faction -> {
            sender.sendMessage("- " + faction.name());
        });
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args){
        return List.of();
    }
}
