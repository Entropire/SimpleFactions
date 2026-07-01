package com.entropire.simplefactions.command;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.entropire.simplefactions.command.sub.HelpCommand;

public class SimpleFactionCommand extends CommandNode {

    public SimpleFactionCommand(){
        register("help", new HelpCommand());
    }

    @Override
    public boolean execute(CommandSender sender, Command command, String alias, String[] args){
        return false;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args){
        return List.of();
    }
}
