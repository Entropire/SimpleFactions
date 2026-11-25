package com.entropire.simplefactions.command.sub;

import com.entropire.simplefactions.faction.FactionService;
import com.entropire.simplefactions.SimpleFactions;
import com.entropire.simplefactions.chat.ChatMessage;
import com.entropire.simplefactions.command.CommandNode;
import com.entropire.simplefactions.faction.FactionMembersPage;
import com.entropire.simplefactions.objects.Pageable;
import com.entropire.simplefactions.objects.Result;
import com.entropire.simplefactions.player.FactionPlayer;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.logging.Level;

public class MembersCommand extends CommandNode {

    private final FactionService factionService;

    public MembersCommand(FactionService factionService) {
        super("members", "Return the list of members of a faction.");
        this.factionService = factionService;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can preform this command!");
            return true;
        }

        boolean ownFaction = args.length == 0 || (args.length == 1 && isNumber(args[0]));

        Result<FactionMembersPage> result;
        if (ownFaction) {
            int page = args.length == 1 ? parsePage(args[0]) : 0;
            result = factionService.getPlayerFactionMembers(player.getUniqueId(), new Pageable<>(page, 10));
        } else {
            int page = args.length > 1 ? parsePage(args[1]) : 0;
            result = factionService.getFactionMembers(args[0], new Pageable<>(page, 10));
        }

        String factionName;
        Pageable<FactionPlayer> factionPlayerPage;
        switch (result) {
            case Result.Success<FactionMembersPage>(FactionMembersPage value) -> {
                factionName = value.faction().name().value();
                factionPlayerPage = value.members();
            }
            case Result.Failure<FactionMembersPage>(String message) -> {
                player.sendMessage(message);
                return true;
            }
            case Result.Error<FactionMembersPage>(String message, Exception e) -> {
                SimpleFactions.getPluginLogger().log(Level.SEVERE, message, e);
                player.sendMessage(ownFaction
                        ? "Something went wrong while retrieving the list of members of your faction."
                        : "Something went wrong while retrieving the list of members of the faction " + args[0] + ".");
                return true;
            }
        }

        String title = factionPlayerPage.maxPages() < 2
                ? "Members of " + factionName
                : "Members of " + factionName + " (%d/%d)".formatted(
                        factionPlayerPage.currentPage() + 1,
                        factionPlayerPage.maxPages());

        String pageHint = ownFaction
                ? "Use /f members [n] to get page n"
                : "Use /f members " + factionName + " [n] to get page n";

        ChatMessage message = new ChatMessage()
                .title(title)
                .textIf(factionPlayerPage.maxPages() > 1, pageHint).setColor(TextColor.color(0xFFD166))
                .list(factionPlayerPage.items().stream().map(FactionPlayer::username).toArray(String[]::new))
                .title(null);

        player.sendMessage(message.build());
        return true;
    }

    private static boolean isNumber(String s) {
        return s.matches("\\d+");
    }

    private static int parsePage(String s) {
        try {
            return Math.max(0, Integer.parseInt(s) - 1);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length != 1 || args[0].isEmpty()) return List.of();

        return switch (factionService.getFactionNames(new Pageable<>(0, 20), args[0])) {
            case Result.Success<Pageable<String>>(Pageable<String> factionNames) -> factionNames.items();
            case Result.Failure<Pageable<String>> failure -> List.of();
            case Result.Error<Pageable<String>>(String message, Exception e) -> {
                SimpleFactions.getPluginLogger().log(Level.SEVERE, message, e);
                yield List.of();
            }
        };
    }

    @Override
    public ChatMessage help(String[] args) {
        return new ChatMessage().text("/f members <faction name> [page]");
    }
}