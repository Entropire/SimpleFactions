package com.entropire.simplefactions.command.sub;

import com.entropire.simplefactions.faction.membership.FactionMembershipService;
import com.entropire.simplefactions.SimpleFactions;
import com.entropire.simplefactions.chat.ChatMessage;
import com.entropire.simplefactions.command.CommandNode;
import com.entropire.simplefactions.faction.membership.FactionMembership;
import com.entropire.simplefactions.objects.Result;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.logging.Level;

public class LeaveCommand extends CommandNode {

    private final FactionMembershipService membershipService;

    public LeaveCommand(FactionMembershipService membershipService) {
        super("leave", "Leave your faction.");
        this.membershipService = membershipService;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player))
        {
            sender.sendMessage("Only players can preform this command!");
            return true;
        }

        Result<FactionMembership> result = membershipService.leaveFaction(player.getUniqueId());
        if (result instanceof Result.Success<FactionMembership>) {
            player.sendMessage("You have left your faction.");
        } else if (result instanceof Result.Failure<FactionMembership> failure) {
            player.sendMessage(failure.message());
        } else if (result instanceof Result.Error<FactionMembership> error) {
            SimpleFactions.getPluginLogger().log(Level.SEVERE, error.message(), error.exception());
            player.sendMessage("Something went wrong while leaving your faction.");
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }

    @Override
    public ChatMessage help(String[] args) {
        return new ChatMessage().text("/f leave");
    }
}
