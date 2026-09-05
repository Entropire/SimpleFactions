package com.entropire.simplefactions.command.sub;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.entropire.simplefactions.FactionApplication;
import com.entropire.simplefactions.chat.ChatMessage;
import com.entropire.simplefactions.command.CommandNode;
import com.entropire.simplefactions.faction.Faction;
import com.entropire.simplefactions.objects.Pageable;

import net.kyori.adventure.text.format.TextColor;

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

        ChatMessage message = new ChatMessage()
        .title(
            factionsPage.maxPages() < 2
                ? "List"
                : "List (%d/%d)".formatted(
                    factionsPage.currentPage() + 1,
                    factionsPage.maxPages()
                )
        )
        .textIf(factionsPage.maxPages() > 1, "Use /sf list [n] to get page n of list").setColor(TextColor.color(0xFFD166))
        .list(factionsPage.items().stream().map(faction -> faction.name()).toList().toArray(String[]::new))
        .title(null);

        sender.sendMessage(message.build());
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args){
        return List.of();
    }
}
