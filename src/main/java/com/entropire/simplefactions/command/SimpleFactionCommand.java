package com.entropire.simplefactions.command;

import java.util.List;

import com.entropire.simplefactions.chat.ChatMessage;
import com.entropire.simplefactions.command.sub.*;
import org.bukkit.command.CommandSender;

import com.entropire.simplefactions.faction.FactionService;
import com.entropire.simplefactions.faction.membership.FactionMembershipService;

public class SimpleFactionCommand extends CommandNode {

    private static final int HELP_PAGE_SIZE = 10;

    public SimpleFactionCommand(FactionService factionService, FactionMembershipService membershipService){
        super("simplefactions", "");
        register(new CreateCommand(factionService));
        register(new DeleteCommand(factionService));
        register(new ListCommand(factionService));
        register(new OwnerCommand(factionService));
        register(new MembersCommand(factionService));
        register(new LeaveCommand(membershipService));
        register(new KickCommand(membershipService));
    }

    @Override
    public boolean execute(CommandSender sender, String[] args){
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args){
        return List.of();
    }

    @Override
    public ChatMessage help(String[] args) {
        List<CommandNode> children = getChildren();
        int maxPage = Math.max(1, (children.size() + HELP_PAGE_SIZE - 1) / HELP_PAGE_SIZE);
        int page = Math.max(0, Math.min(parsePage(args) - 1, maxPage - 1));

        int from = page * HELP_PAGE_SIZE;
        int to = Math.min(from + HELP_PAGE_SIZE, children.size());

        return new ChatMessage()
                .textIf(maxPage > 1, "Use /f help [n] to get page n of help")
                .textIf(maxPage > 1, "(Page " + (page + 1) + "/" + maxPage + ")")
                .list(children.subList(from, to)
                        .stream()
                        .map(commandNode -> "/f " + commandNode.getCommandName() + " - " + commandNode.getCommandDescription())
                        .toArray(String[]::new));
    }

    private static int parsePage(String[] args) {
        if (args.length == 0) return 1;
        try {
            return Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            return 1;
        }
    }
}
