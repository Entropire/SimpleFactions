package com.entropire.simplefactions.command.sub;

import com.entropire.simplefactions.faction.membership.FactionMembershipService;
import com.entropire.simplefactions.SimpleFactions;
import com.entropire.simplefactions.chat.ChatMessage;
import com.entropire.simplefactions.command.CommandNode;
import com.entropire.simplefactions.faction.membership.FactionMembership;
import com.entropire.simplefactions.objects.Pageable;
import com.entropire.simplefactions.objects.Result;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.logging.Level;

public class KickCommand extends CommandNode {

    private final FactionMembershipService membershipService;

    public KickCommand(FactionMembershipService membershipService) {
        super("kick", "Kick a member from your faction.");
        this.membershipService = membershipService;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player))
        {
            sender.sendMessage("Only players can preform this command!");
            return true;
        }

        if (args.length == 0) {
            help(args).sendTo(player);
            return true;
        }

        String memberName = args[0];

        Result<FactionMembership> result = membershipService.kickFactionMember(player.getUniqueId(), memberName);
        if (result instanceof Result.Success<FactionMembership> success) {
            player.sendMessage("You have kicked " + memberName + " from your faction.");
            Player member = Bukkit.getPlayer(success.value().playerUuid());
            if (member != null) member.sendMessage("You have been kicked from your faction.");
        } else if (result instanceof Result.Failure<FactionMembership> failure) {
            player.sendMessage(failure.message());
        } else if (result instanceof Result.Error<FactionMembership> error) {
            SimpleFactions.getPluginLogger().log(Level.SEVERE, error.message(), error.exception());
            player.sendMessage("Something went wrong while kicking " + memberName + " from your faction.");
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) return List.of();
        if (args.length != 1) return List.of();

        Result<Pageable<String>> result = membershipService.getFactionMemberNames(new Pageable<>(0, 20), player.getUniqueId(), args[0]);
        if (result instanceof Result.Success<Pageable<String>> success) {
            return success.value().items().stream()
                    .filter(name -> !name.equalsIgnoreCase(player.getName()))
                    .toList();
        }
        if (result instanceof Result.Error<Pageable<String>> error) {
            SimpleFactions.getPluginLogger().log(Level.SEVERE, error.message(), error.exception());
        }
        return List.of();
    }

    @Override
    public ChatMessage help(String[] args) {
        return new ChatMessage().text("/f kick <member name>");
    }
}
