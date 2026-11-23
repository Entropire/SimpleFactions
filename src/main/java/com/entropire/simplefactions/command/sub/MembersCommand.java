package com.entropire.simplefactions.command.sub;

import com.entropire.simplefactions.FactionApplication;
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

    private final FactionApplication factionApplication;

    public MembersCommand(FactionApplication factionApplication) {
        super("members", "Return the list of members of a faction.");
        this.factionApplication = factionApplication;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player))
        {
            sender.sendMessage("Only players can preform this command!");
            return false;
        }

        if(args.length < 1){
            player.sendMessage(help(args).build());
            return true;
        }
        String factionName = args[0];

        int pageRequest;
        try{
            pageRequest = Integer.parseInt(args[1]);
        }catch (Exception _){
            pageRequest = 0;
        }
        int page = args.length == 1 ? 0 : Math.max(0, pageRequest - 1);

        Pageable<FactionPlayer> factionPlayerPage;
        switch (factionApplication.getFactionMembers(factionName, new Pageable<>(page, 10))) {
            case Result.Success<FactionMembersPage>(FactionMembersPage value) -> {
                factionName = value.faction().name();
                factionPlayerPage = value.members();
            }
            case Result.Failure<FactionMembersPage>(String message) -> {
                player.sendMessage(message);
                return true;
            }
            case Result.Error<FactionMembersPage>(String message, Exception e) -> {
                SimpleFactions.getPluginLogger().log(Level.SEVERE, message, e);
                player.sendMessage("Something went wrong while retrieving the list of members of the faction " + factionName + "." );
                return true;
            }
        }

        ChatMessage message = new ChatMessage()
                .title(
                        factionPlayerPage.maxPages() < 2
                                ? "Members of " + factionName
                                : "Members of " + factionName + " (%d/%d)".formatted(
                                factionPlayerPage.currentPage() + 1,
                                factionPlayerPage.maxPages()
                        )
                )
                .textIf(factionPlayerPage.maxPages() > 1, "Use /f members " + factionName + " [n] to get page n").setColor(TextColor.color(0xFFD166))
                .list(factionPlayerPage.items().stream().map(FactionPlayer::username).toList().toArray(String[]::new))
                .title(null);

        sender.sendMessage(message.build());
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length != 1 || args[0].isEmpty()) return List.of();

        return switch (factionApplication.getFactionNames(new Pageable<>(0, 20), args[0])) {
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
