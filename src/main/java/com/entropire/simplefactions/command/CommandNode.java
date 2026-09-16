package com.entropire.simplefactions.command;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.entropire.simplefactions.chat.ChatMessage;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jspecify.annotations.NonNull;

public abstract class CommandNode implements CommandExecutor, TabCompleter {
    private final Map<String, CommandNode> children = new HashMap<>();
    private final String commandName;
    private final String commandDescription;

    public List<CommandNode> getChildren(){
        return children.values().stream().toList();
    }

    public String getCommandName(){
        return commandName;
    }

    public String getCommandDescription(){
        return commandDescription;
    }

    public CommandNode(String commandName, String commandDescription) {
        this.commandName = commandName;
        this.commandDescription = commandDescription;
    }

    public void register(CommandNode command){
        children.put(command.commandName, command);
    }

    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String alias, String[] args){
        if (args.length > 0) {
            if(args[0].equalsIgnoreCase("help")){
                onHelp(sender, Arrays.copyOfRange(args, 1, args.length));
                return true;
            }

            CommandNode child = children.get(args[0].toLowerCase());

            if (child != null) {
                return child.onCommand(sender, command, alias, Arrays.copyOfRange(args, 1, args.length));
            }
        }

        return execute(sender, args);
    }

    @Override
    public List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command command, @NonNull String alias, String[] args){
        if (args.length == 1) {
            return children.keySet().stream() .filter(s -> s.startsWith(args[0].toLowerCase())).toList();
        }

        CommandNode child = children.get(args[0].toLowerCase());

        if (child != null) {
            return child.onTabComplete(sender, command, alias, Arrays.copyOfRange(args, 1, args.length));
        }

        return Collections.emptyList();
    }

    private void onHelp(CommandSender sender, String[] args){
        ChatMessage message = new ChatMessage()
                .title(commandName + " - help")
                .textIf(!commandDescription.isEmpty(), commandDescription)
                .message(help(args))
                .title(null);

        sender.sendMessage(message.build());
    }

    public abstract boolean execute(CommandSender sender, String[] args);

    public abstract List<String> tabComplete(CommandSender sender, String[] args);

    public abstract ChatMessage help(String[] args);
}
