package com.entropire.simplefactions.command;

import java.util.List;

import com.entropire.simplefactions.chat.ChatMessage;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.CommandSender;

import com.entropire.simplefactions.FactionApplication;
import com.entropire.simplefactions.command.sub.CreateCommand;
import com.entropire.simplefactions.command.sub.DeleteCommand;
import com.entropire.simplefactions.command.sub.ListCommand;
import com.entropire.simplefactions.command.sub.OwnerCommand;

public class SimpleFactionCommand extends CommandNode {

    public SimpleFactionCommand(FactionApplication factionApplication){
        super("simplefactions", "");
        register(new CreateCommand(factionApplication));
        register(new DeleteCommand(factionApplication));
        register(new ListCommand(factionApplication));
        register(new OwnerCommand(factionApplication));
    }

    @Override
    public boolean execute(CommandSender sender, String[] args){
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender){
        return List.of();
    }

    @Override
    public String help(String[] args) {
        List<CommandNode> children = getChildren();
        int page = 0;
        int maxPage = (int)Math.ceil((double) children.size() / 2);

        if(args.length > 0){
            page = Math.clamp(Integer.parseInt(args[0]) - 1, 0, maxPage - 1);
        }

        ChatMessage message = new ChatMessage()
                .textIf(children.size() > 10, "Use /help [n] to get page n of help").setColor(TextColor.color(0xFFD166))
                .textIf(children.size() > 10, "(Page " + page + "/" + maxPage)
                .list(children.subList(page * 10, page * 10 + 10)
                        .stream()
                        .map(commandNode -> "/f " + commandNode.getCommandName() + " - " + commandNode.getCommandDescription())
                        .toArray(String[]::new))
                .title(null);

        return message.build().toString();
    }
}
