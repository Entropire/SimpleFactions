package com.entropire.simplefactions.command.sub;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.entropire.simplefactions.chat.ChatMessage;
import com.entropire.simplefactions.command.CommandNode;

public class HelpCommand extends CommandNode{
    
    @Override
    public boolean execute(CommandSender sender, Command command, String alias, String[] args){
        ChatMessage message = new ChatMessage()
        .title("Help")
        // .text("Use /help [n] to get page n of help").setColor(TextColor.color(0xFFD166))
        .list("/sf help", 
            "/sf create", 
            "/sf delete",
            "/sf list",
            "/sf info",
            "/sf members")
        .title(null);

        sender.sendMessage(message.build());

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args){
        return List.of();
    }
}
