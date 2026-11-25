package com.entropire.simplefactions;

import java.sql.Connection;
import java.util.List;
import java.util.UUID;

import com.entropire.simplefactions.objects.Result;
import com.entropire.simplefactions.database.DataBaseContext;
import com.entropire.simplefactions.faction.Faction;
import com.entropire.simplefactions.faction.FactionMembersPage;
import com.entropire.simplefactions.faction.FactionService;
import com.entropire.simplefactions.faction.membership.FactionMembership;
import com.entropire.simplefactions.faction.membership.FactionMembershipService;
import com.entropire.simplefactions.faction.membership.Role;
import com.entropire.simplefactions.objects.Pageable;
import com.entropire.simplefactions.player.FactionPlayer;

public class FactionApplication {
    private final DataBaseContext db;
    private final FactionService factionService;
    private final FactionMembershipService factionMembershipService;

    public FactionApplication(
        DataBaseContext db,
        FactionService factionService,
        FactionMembershipService factionMembershipService
    ){
        this.db = db;
        this.factionService = factionService;
        this.factionMembershipService = factionMembershipService;
    }

    public Result<Faction> createFaction(String name, String color, UUID ownerUuid) {
        return db.transaction(conn -> {
            Result<FactionMembership> membershipResult = factionMembershipService.getByPlayerUuid(conn, ownerUuid);
            if (!(membershipResult instanceof Result.Success(var membership))) return membershipResult.propagate();
            if (membership != null) return new Result.Failure<>("You are already part of a faction!");

            Result<Faction> factionResult = factionService.save(conn, new Faction(name, color, ownerUuid));
            if (!(factionResult instanceof Result.Success(var faction))) return factionResult;

            Result<Boolean> ownerResult = factionMembershipService.save(conn, new FactionMembership(ownerUuid, faction.uuid(), Role.OWNER, null));
            if (!ownerResult.isSuccess()) return ownerResult.propagate();

            return new Result.Success<>(faction);
        });
    }

    public Result<List<UUID>> deleteFaction(UUID ownerUuid) {
        return db.transaction(conn -> {
            Result<FactionMembership> memberResult = factionMembershipService.getByPlayerUuid(conn, ownerUuid);
            if (!(memberResult instanceof Result.Success(var membership))) return memberResult.propagate();
            if (membership == null) return new Result.Failure<>("You are not in a faction!");
            if (membership.role() != Role.OWNER) return new Result.Failure<>("Only the owner can delete the faction!");

            Result<List<FactionMembership>> membersResult = factionMembershipService.getAllByFactionUuid(conn, membership.factionUuid());
            if (!(membersResult instanceof Result.Success(var memberships))) return membersResult.propagate();

            Result<Boolean> deleted = factionService.delete(conn, membership.factionUuid());
            if (!deleted.isSuccess()) return deleted.propagate();

            return new Result.Success<>(memberships.stream().map(FactionMembership::playerUuid).toList());
        });
    }

    public Result<Pageable<Faction>> getFactions(Pageable<Faction> pageable) {
        return db.transaction(conn -> {
            Result<Integer> countResult = factionService.getFactionsCount(conn);
            if (!(countResult instanceof Result.Success(var factionCount))) return countResult.propagate();

            int maxPages = Math.max(1, (factionCount + pageable.itemsPerPage() - 1) / pageable.itemsPerPage());
            int page = Math.clamp(pageable.currentPage(), 0, maxPages - 1);

            Result<List<Faction>> factionsResult = factionService.getFactions(conn, new Pageable<>(page, pageable.itemsPerPage()));
            if (!(factionsResult instanceof Result.Success(var factions))) return factionsResult.propagate();

            return new Result.Success<>(new Pageable<>(factions, page, maxPages, pageable.itemsPerPage()));
        });
    }

    public Result<Pageable<String>> getFactionNames(Pageable<String> pageable, String contains){
        return db.withConnection(conn -> {
            Result<List<String>> factionNamesResult = factionService.getFactionNames(conn, pageable, contains);
            if (!(factionNamesResult instanceof Result.Success(var factionNames))) return factionNamesResult.propagate();

            return new Result.Success<>(new Pageable<>(factionNames, pageable.currentPage(), 1, pageable.itemsPerPage()));
        });
    }

    private Result<Faction> getPlayerFaction(Connection conn, UUID playerUuid) {
        Result<FactionMembership> membershipResult = factionMembershipService.getByPlayerUuid(conn, playerUuid);
        if (!(membershipResult instanceof Result.Success(var membership))) return membershipResult.propagate();
        if (membership == null) return new Result.Failure<>("You are not in a faction!");

        return factionService.getByUuid(conn, membership.factionUuid());
    }

    public Result<FactionPlayer> getFactionOwner(String factionName){
        return db.withConnection(conn -> factionService.getOwner(conn, factionName));
    }

    public Result<FactionPlayer> getPlayerFactionOwner(UUID playerUuid) {
        return db.transaction(conn -> {
            Result<Faction> factionResult = getPlayerFaction(conn, playerUuid);
            if (!(factionResult instanceof Result.Success(var faction))) return factionResult.propagate();

            return factionService.getOwner(conn, faction.name());
        });
    }

    public Result<FactionMembersPage> getFactionMembers(String factionName, Pageable<FactionPlayer> pageable) {
        return db.transaction(conn -> {
            Result<Faction> factionResult = factionService.getByName(conn, factionName);
            if (!(factionResult instanceof Result.Success(var faction))) return factionResult.propagate();

            return getFactionMembers(conn, faction, pageable);
        });
    }


    public Result<FactionMembersPage> getPlayerFactionMembers(UUID playerUuid, Pageable<FactionPlayer> pageable) {
        return db.transaction(conn -> {
            Result<Faction> factionResult = getPlayerFaction(conn, playerUuid);
            if (!(factionResult instanceof Result.Success(var faction))) return factionResult.propagate();

            return getFactionMembers(conn, faction, pageable);
        });
    }

    private Result<FactionMembersPage> getFactionMembers(Connection conn, Faction faction, Pageable<FactionPlayer> pageable) {
        Result<Integer> countResult = factionService.getFactionMembersCount(conn, faction.name());
        if (!(countResult instanceof Result.Success(var memberCount))) return countResult.propagate();

        int maxPages = Math.max(1, (memberCount + pageable.itemsPerPage() - 1) / pageable.itemsPerPage());
        int page = Math.clamp(pageable.currentPage(), 0, maxPages - 1);

        Result<List<FactionPlayer>> membersResult = factionService.getFactionMembers(conn, faction.name(), new Pageable<>(page, pageable.itemsPerPage()));
        if (!(membersResult instanceof Result.Success(var members))) return membersResult.propagate();

        return new Result.Success<>(new FactionMembersPage(faction, new Pageable<>(members, page, maxPages, pageable.itemsPerPage())));
    }

    public Result<FactionMembership> leaveFaction(UUID playerUuid) {
        return db.transaction(conn -> {
            Result<FactionMembership> memberResult = factionMembershipService.getByPlayerUuid(conn, playerUuid);
            if(!(memberResult instanceof Result.Success(var member))) return memberResult.propagate();
            if (member == null) return new Result.Failure<>("You are not part of a faction!");
            if(member.role() == Role.OWNER) return new Result.Failure<>("The owner can't leave the faction. Transfer ownership or delete the faction instead.");

            return factionMembershipService.deleteByPlayerUuid(conn, playerUuid);
        });
    }

    public Result<Pageable<String>> getFactionMemberNames(Pageable<String> pageable, UUID playerUuid, String contains) {
        return db.transaction(conn -> {
            Result<Faction>  factionResult = getPlayerFaction(conn, playerUuid);
            if (!(factionResult instanceof Result.Success(var faction))) return factionResult.propagate();

            Result<List<String>> factionMemberNamesResult = factionMembershipService.getFactionMemberNames(conn, pageable, faction.uuid(), contains);
            if (!(factionMemberNamesResult instanceof Result.Success(var factionMemberNames))) return factionMemberNamesResult.propagate();

            return new Result.Success<>(new Pageable<>(factionMemberNames, pageable.currentPage(), 1, pageable.itemsPerPage()));
        });
    }

    public Result<FactionMembership> kickFactionMember(UUID playerUuid, String userName){
        return db.transaction(conn -> {
           Result<FactionMembership> membershipResult = factionMembershipService.getByPlayerUuid(conn, playerUuid);
           if(!(membershipResult instanceof Result.Success(var membership))) return membershipResult.propagate();
           if (membership == null || membership.role() != Role.OWNER) return  new Result.Failure<>("You must be the owner to kick somebody from the faction!");

           Result<FactionMembership> kickMembershipResult = factionMembershipService.getByPlayerName(conn, userName);
           if (!(kickMembershipResult instanceof Result.Success(var kickMembership))) return kickMembershipResult.propagate();
           if (kickMembership == null || !kickMembership.factionUuid().equals(membership.factionUuid())) return new Result.Failure<>(userName + " is not part of your faction!");
           if (kickMembership.playerUuid().equals(playerUuid)) return new Result.Failure<>("You can't kick yourself. Transfer ownership or delete the faction instead.");

           return factionMembershipService.deleteByPlayerUuid(conn, kickMembership.playerUuid());
        });
    }
}