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
            player.sendMessage(help(args).build());
            return true;
        }

        String memberName = args[0];

        switch (membershipService.kickFactionMember(player.getUniqueId(), memberName)) {
            case Result.Success<FactionMembership>(FactionMembership kicked) -> {
                player.sendMessage("You have kicked " + memberName + " from your faction.");
                Player member = Bukkit.getPlayer(kicked.playerUuid());
                if (member != null) member.sendMessage("You have been kicked from your faction.");
            }
            case Result.Failure<FactionMembership>(String message) -> player.sendMessage(message);
            case Result.Error<FactionMembership>(String message, Exception e) -> {
                SimpleFactions.getPluginLogger().log(Level.SEVERE, message, e);
                player.sendMessage("Something went wrong while kicking " + memberName + " from your faction.");
            }
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) return List.of();
        if (args.length != 1) return List.of();

        return switch (membershipService.getFactionMemberNames(new Pageable<>(0, 20), player.getUniqueId(), args[0])) {
            case Result.Success<Pageable<String>>(Pageable<String> memberNames) -> memberNames.items().stream()
                    .filter(name -> !name.equalsIgnoreCase(player.getName()))
                    .toList();
            case Result.Failure<Pageable<String>> failure -> List.of();
            case Result.Error<Pageable<String>>(String message, Exception e) -> {
                SimpleFactions.getPluginLogger().log(Level.SEVERE, message, e);
                yield List.of();
            }
        };
    }

    @Override
    public ChatMessage help(String[] args) {
        return new ChatMessage().text("/f kick <member name>");
    }
}
