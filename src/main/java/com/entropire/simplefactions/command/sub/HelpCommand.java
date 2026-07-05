package com.entropire.simplefactions.command.sub;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.entropire.simplefactions.command.CommandNode;

public class HelpCommand extends CommandNode{
    
    @Override
    public boolean execute(CommandSender sender, Command command, String alias, String[] args){
        sender.sendMessage("""
                =-=-=-=-=- Simple Factions -=-=-=-=-=
                /sp help - get this usefull piece of text
                /sp create [name] [collor] - create a new faction
                /sp delete - delete your faction
                """);
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args){
        return List.of();
    }
}
