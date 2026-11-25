package com.entropire.simplefactions.command.sub;

import java.util.List;
import java.util.logging.Level;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.entropire.simplefactions.faction.FactionService;
import com.entropire.simplefactions.chat.ChatMessage;
import com.entropire.simplefactions.command.CommandNode;
import com.entropire.simplefactions.faction.Faction;
import com.entropire.simplefactions.objects.Pageable;
import com.entropire.simplefactions.objects.Result;
import com.entropire.simplefactions.SimpleFactions;

import net.kyori.adventure.text.format.TextColor;

public class ListCommand extends CommandNode {

    private final FactionService factionService;
    
    public ListCommand(FactionService factionService){
        super("list", "Returns the list of factions.");
        this.factionService = factionService;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args){
        if (!(sender instanceof Player player))
        {
            sender.sendMessage("Only players can preform this command!");
            return false;
        }

        int pageRequest;
        try{
            pageRequest = Integer.parseInt(args[0]);
        }catch (Exception e){
            pageRequest = 0;
        }
        int page = args.length == 0 ? 0 : Math.max(0, pageRequest - 1);

        Result<Pageable<Faction>> result = factionService.getFactions(new Pageable<>(page, 10));
        if (!(result instanceof Result.Success<Pageable<Faction>> success)) {
            if (result instanceof Result.Failure<Pageable<Faction>> failure) {
                player.sendMessage(failure.message());
            } else if (result instanceof Result.Error<Pageable<Faction>> error) {
                SimpleFactions.getPluginLogger().log(Level.SEVERE, error.message(), error.exception());
                player.sendMessage("Something went wrong while retrieving the list of factions.");
            }
            return true;
        }
        Pageable<Faction> factionsPage = success.value();

        ChatMessage message = new ChatMessage()
        .title(
            factionsPage.maxPages() < 2
                ? "List"
                : "List (%d/%d)".formatted(
                    factionsPage.currentPage() + 1,
                    factionsPage.maxPages()
                )
        )
        .textIf(factionsPage.maxPages() > 1, "Use /f list [n] to get page n of list").setColor(TextColor.color(0xFFD166))
        .list(factionsPage.items().stream().map(faction -> faction.name().value()).toArray(String[]::new))
        .title(null);

        sender.sendMessage(message.build());
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args){
        return List.of();
    }

    @Override
    public ChatMessage help(String[] args) {
        return new ChatMessage().text("/f list [page]");
    }
}
