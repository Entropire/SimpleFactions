package com.entropire.simplefactions.command;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.entropire.simplefactions.FactionApplication;
import com.entropire.simplefactions.command.sub.CreateCommand;
import com.entropire.simplefactions.command.sub.DeleteCommand;
import com.entropire.simplefactions.command.sub.HelpCommand;
import com.entropire.simplefactions.command.sub.ListCommand;

public class SimpleFactionCommand extends CommandNode {

    public SimpleFactionCommand(FactionApplication factionApplication){
        register("help", new HelpCommand());
        register("create", new CreateCommand(factionApplication));
        register("delete", new DeleteCommand(factionApplication));
        register("list", new ListCommand(factionApplication));
    }

    @Override
    public boolean execute(CommandSender sender, Command command, String alias, String[] args){
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args){
        return List.of();
    }
}
