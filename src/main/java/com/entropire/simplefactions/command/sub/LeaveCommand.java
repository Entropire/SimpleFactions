package com.entropire.simplefactions.command.sub;

import com.entropire.simplefactions.FactionApplication;
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

    private final FactionApplication factionApplication;

    public LeaveCommand(FactionApplication factionApplication) {
        super("leave", "Leave your faction.");
        this.factionApplication = factionApplication;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player))
        {
            sender.sendMessage("Only players can preform this command!");
            return true;
        }

        switch (factionApplication.leaveFaction(player.getUniqueId())) {
            case Result.Success<FactionMembership>(FactionMembership _) -> player.sendMessage("You have left your faction.");
            case Result.Failure<FactionMembership>(String message) -> player.sendMessage(message);
            case Result.Error<FactionMembership>(String message, Exception e) -> {
                SimpleFactions.getPluginLogger().log(Level.SEVERE, message, e);
                player.sendMessage("Something went wrong while leaving your faction.");
            }
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
