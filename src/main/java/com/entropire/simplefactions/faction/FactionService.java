package com.entropire.simplefactions.faction;

import java.sql.Connection;
import java.util.List;
import java.util.UUID;

import com.entropire.simplefactions.database.DataBaseContext;
import com.entropire.simplefactions.faction.membership.FactionMembership;
import com.entropire.simplefactions.faction.membership.FactionMembershipRepository;
import com.entropire.simplefactions.faction.membership.Role;
import com.entropire.simplefactions.objects.Pageable;
import com.entropire.simplefactions.objects.Result;
import com.entropire.simplefactions.player.FactionPlayer;

public class FactionService {
    private final DataBaseContext db;
    private final FactionRepository factionRepository;
    private final FactionMembershipRepository membershipRepository;

    public FactionService(
        DataBaseContext db,
        FactionRepository factionRepository,
        FactionMembershipRepository membershipRepository
    ){
        this.db = db;
        this.factionRepository = factionRepository;
        this.membershipRepository = membershipRepository;
    }

    public Result<Faction> createFaction(String name, String color, UUID ownerUuid) {
        return db.transaction(conn -> {
            Result<FactionMembership> membershipResult = membershipRepository.getByPlayerUuid(conn, ownerUuid);
            if (!(membershipResult instanceof Result.Success(var membership))) return membershipResult.propagate();
            if (membership != null) return new Result.Failure<>("You are already part of a faction!");

            Result<Faction> factionResult = factionRepository.save(conn, new Faction(name, color, ownerUuid));
            if (!(factionResult instanceof Result.Success(var faction))) return factionResult;

            Result<Boolean> ownerResult = membershipRepository.save(conn, new FactionMembership(ownerUuid, faction.uuid(), Role.OWNER, null));
            if (!ownerResult.isSuccess()) return ownerResult.propagate();

            return new Result.Success<>(faction);
        });
    }

    public Result<List<UUID>> deleteFaction(UUID ownerUuid) {
        return db.transaction(conn -> {
            Result<FactionMembership> memberResult = membershipRepository.getByPlayerUuid(conn, ownerUuid);
            if (!(memberResult instanceof Result.Success(var membership))) return memberResult.propagate();
            if (membership == null) return new Result.Failure<>("You are not in a faction!");
            if (membership.role() != Role.OWNER) return new Result.Failure<>("Only the owner can delete the faction!");

            Result<List<FactionMembership>> membersResult = membershipRepository.getAllByFactionUuid(conn, membership.factionUuid());
            if (!(membersResult instanceof Result.Success(var memberships))) return membersResult.propagate();

            Result<Boolean> deleted = factionRepository.delete(conn, membership.factionUuid());
            if (!deleted.isSuccess()) return deleted.propagate();

            return new Result.Success<>(memberships.stream().map(FactionMembership::playerUuid).toList());
        });
    }

    public Result<Pageable<Faction>> getFactions(Pageable<Faction> pageable) {
        return db.transaction(conn -> {
            Result<Integer> countResult = factionRepository.getFactionsCount(conn);
            if (!(countResult instanceof Result.Success(var factionCount))) return countResult.propagate();

            int maxPages = Math.max(1, (factionCount + pageable.itemsPerPage() - 1) / pageable.itemsPerPage());
            int page = Math.clamp(pageable.currentPage(), 0, maxPages - 1);

            Result<List<Faction>> factionsResult = factionRepository.getFactions(conn, new Pageable<>(page, pageable.itemsPerPage()));
            if (!(factionsResult instanceof Result.Success(var factions))) return factionsResult.propagate();

            return new Result.Success<>(new Pageable<>(factions, page, maxPages, pageable.itemsPerPage()));
        });
    }

    public Result<Pageable<String>> getFactionNames(Pageable<String> pageable, String contains){
        return db.withConnection(conn -> {
            Result<List<String>> factionNamesResult = factionRepository.getFactionNames(conn, pageable, contains);
            if (!(factionNamesResult instanceof Result.Success(var factionNames))) return factionNamesResult.propagate();

            return new Result.Success<>(new Pageable<>(factionNames, pageable.currentPage(), 1, pageable.itemsPerPage()));
        });
    }

    public Result<FactionPlayer> getFactionOwner(String factionName){
        return db.withConnection(conn -> factionRepository.getOwner(conn, factionName));
    }

    public Result<FactionPlayer> getPlayerFactionOwner(UUID playerUuid) {
        return db.transaction(conn -> {
            Result<Faction> factionResult = getPlayerFaction(conn, playerUuid);
            if (!(factionResult instanceof Result.Success(var faction))) return factionResult.propagate();

            return factionRepository.getOwner(conn, faction.name());
        });
    }

    public Result<FactionMembersPage> getFactionMembers(String factionName, Pageable<FactionPlayer> pageable) {
        return db.transaction(conn -> {
            Result<Faction> factionResult = factionRepository.getByName(conn, factionName);
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

    private Result<Faction> getPlayerFaction(Connection conn, UUID playerUuid) {
        Result<FactionMembership> membershipResult = membershipRepository.getByPlayerUuid(conn, playerUuid);
        if (!(membershipResult instanceof Result.Success(var membership))) return membershipResult.propagate();
        if (membership == null) return new Result.Failure<>("You are not in a faction!");

        return factionRepository.getByUuid(conn, membership.factionUuid());
    }

    private Result<FactionMembersPage> getFactionMembers(Connection conn, Faction faction, Pageable<FactionPlayer> pageable) {
        Result<Integer> countResult = factionRepository.getFactionMembersCount(conn, faction.name());
        if (!(countResult instanceof Result.Success(var memberCount))) return countResult.propagate();

        int maxPages = Math.max(1, (memberCount + pageable.itemsPerPage() - 1) / pageable.itemsPerPage());
        int page = Math.clamp(pageable.currentPage(), 0, maxPages - 1);

        Result<List<FactionPlayer>> membersResult = factionRepository.getFactionMembers(conn, faction.name(), new Pageable<>(page, pageable.itemsPerPage()));
        if (!(membersResult instanceof Result.Success(var members))) return membersResult.propagate();

        return new Result.Success<>(new FactionMembersPage(faction, new Pageable<>(members, page, maxPages, pageable.itemsPerPage())));
    }
}
