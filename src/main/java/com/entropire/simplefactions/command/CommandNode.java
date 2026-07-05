package com.entropire.simplefactions.command;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public abstract class CommandNode implements CommandExecutor, TabCompleter {
    private Map<String, CommandNode> children = new HashMap<>();

    public void register(String string, CommandNode command){
        children.put(string, command);
    }

    public Map<String, CommandNode> getChildren(){
        return children;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args){
        if (args.length > 0) {
            CommandNode child = children.get(args[0].toLowerCase());

            if (child != null) {
                return child.onCommand(sender,command, alias, Arrays.copyOfRange(args, 1, args.length));
            }
        }

        return execute(sender, command, alias, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args){
        if (args.length == 1) {
            return children.keySet().stream() .filter(s -> s.startsWith(args[0].toLowerCase())).toList();
        }

        CommandNode child = children.get(args[0].toLowerCase());

        if (child != null) {
            return child.onTabComplete(sender, command, alias, Arrays.copyOfRange(args, 1, args.length));
        }

        return Collections.emptyList();
    }

    public abstract boolean execute(CommandSender sender, Command command, String alias, String[] args);

    public abstract List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args);
}
