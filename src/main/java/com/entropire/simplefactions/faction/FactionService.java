package com.entropire.simplefactions.faction;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;
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

    public Result<Faction> createFaction(String rawName, String color, UUID ownerUuid) {
        Result<FactionName> factionNameResult = FactionName.parse(rawName);
        if (!(factionNameResult instanceof Result.Success<FactionName> factionNameSuccess)) return factionNameResult.propagate();
        FactionName factionName = factionNameSuccess.value();

        return db.transaction(conn -> {
            Result<Optional<FactionMembership>> existingResult = membershipRepository.findByPlayerUuid(conn, ownerUuid);
            if (!(existingResult instanceof Result.Success<Optional<FactionMembership>> existingSuccess)) return existingResult.propagate();
            if (existingSuccess.value().isPresent()) return new Result.Failure<>("You are already part of a faction!");

            Result<Faction> factionResult = factionRepository.save(conn, new Faction(factionName, color, ownerUuid));
            if (!(factionResult instanceof Result.Success<Faction> factionSuccess)) return factionResult;
            Faction faction = factionSuccess.value();

            Result<Boolean> ownerResult = membershipRepository.save(conn, new FactionMembership(ownerUuid, faction.uuid(), Role.OWNER, null));
            if (!ownerResult.isSuccess()) return ownerResult.propagate();

            return new Result.Success<>(faction);
        });
    }

    public Result<List<UUID>> deleteFaction(UUID ownerUuid) {
        return db.transaction(conn -> {
            Result<FactionMembership> memberResult = Result.require(membershipRepository.findByPlayerUuid(conn, ownerUuid), "You are not in a faction!");
            if (!(memberResult instanceof Result.Success<FactionMembership> memberSuccess)) return memberResult.propagate();
            FactionMembership membership = memberSuccess.value();
            if (membership.role() != Role.OWNER) return new Result.Failure<>("Only the owner can delete the faction!");

            Result<List<FactionMembership>> membersResult = membershipRepository.getAllByFactionUuid(conn, membership.factionUuid());
            if (!(membersResult instanceof Result.Success<List<FactionMembership>> membersSuccess)) return membersResult.propagate();
            List<FactionMembership> memberships = membersSuccess.value();

            Result<Boolean> deleted = factionRepository.delete(conn, membership.factionUuid());
            if (!deleted.isSuccess()) return deleted.propagate();

            return new Result.Success<>(memberships.stream().map(FactionMembership::playerUuid).toList());
        });
    }

    public Result<Pageable<Faction>> getFactions(Pageable<Faction> pageable) {
        return db.transaction(conn -> {
            Result<Integer> countResult = factionRepository.getFactionsCount(conn);
            if (!(countResult instanceof Result.Success<Integer> countSuccess)) return countResult.propagate();
            int factionCount = countSuccess.value();

            int maxPages = Math.max(1, (factionCount + pageable.itemsPerPage() - 1) / pageable.itemsPerPage());
            int page = Math.max(0, Math.min(pageable.currentPage(), maxPages - 1));

            Result<List<Faction>> factionsResult = factionRepository.getFactions(conn, new Pageable<>(page, pageable.itemsPerPage()));
            if (!(factionsResult instanceof Result.Success<List<Faction>> factionsSuccess)) return factionsResult.propagate();
            List<Faction> factions = factionsSuccess.value();

            return new Result.Success<>(new Pageable<>(factions, page, maxPages, pageable.itemsPerPage()));
        });
    }

    public Result<Pageable<String>> getFactionNames(Pageable<String> pageable, String contains){
        return db.withConnection(conn -> {
            Result<List<String>> factionNamesResult = factionRepository.getFactionNames(conn, pageable, contains);
            if (!(factionNamesResult instanceof Result.Success<List<String>> factionNamesSuccess)) return factionNamesResult.propagate();
            List<String> factionNames = factionNamesSuccess.value();

            return new Result.Success<>(new Pageable<>(factionNames, pageable.currentPage(), 1, pageable.itemsPerPage()));
        });
    }

    public Result<FactionPlayer> getFactionOwner(String factionName){
        return db.withConnection(conn ->
                Result.require(factionRepository.findOwner(conn, factionName), "There is no faction named " + factionName));
    }

    public Result<FactionPlayer> getPlayerFactionOwner(UUID playerUuid) {
        return db.transaction(conn -> {
            Result<Faction> factionResult = getPlayerFaction(conn, playerUuid);
            if (!(factionResult instanceof Result.Success<Faction> factionSuccess)) return factionResult.propagate();
            Faction faction = factionSuccess.value();

            return Result.require(factionRepository.findOwner(conn, faction.name().value()), "Could not find the owner of your faction.");
        });
    }

    public Result<FactionMembersPage> getFactionMembers(String factionName, Pageable<FactionPlayer> pageable) {
        return db.transaction(conn -> {
            Result<Faction> factionResult = Result.require(factionRepository.findByName(conn, factionName), "There is no faction named " + factionName);
            if (!(factionResult instanceof Result.Success<Faction> factionSuccess)) return factionResult.propagate();

            return getFactionMembers(conn, factionSuccess.value(), pageable);
        });
    }

    public Result<FactionMembersPage> getPlayerFactionMembers(UUID playerUuid, Pageable<FactionPlayer> pageable) {
        return db.transaction(conn -> {
            Result<Faction> factionResult = getPlayerFaction(conn, playerUuid);
            if (!(factionResult instanceof Result.Success<Faction> factionSuccess)) return factionResult.propagate();

            return getFactionMembers(conn, factionSuccess.value(), pageable);
        });
    }

    private Result<Faction> getPlayerFaction(Connection conn, UUID playerUuid) {
        Result<FactionMembership> membershipResult = Result.require(membershipRepository.findByPlayerUuid(conn, playerUuid), "You are not in a faction!");
        if (!(membershipResult instanceof Result.Success<FactionMembership> membershipSuccess)) return membershipResult.propagate();
        FactionMembership membership = membershipSuccess.value();

        return Result.require(factionRepository.findByUuid(conn, membership.factionUuid()), "Your faction no longer exists.");
    }

    private Result<FactionMembersPage> getFactionMembers(Connection conn, Faction faction, Pageable<FactionPlayer> pageable) {
        Result<Integer> countResult = factionRepository.getFactionMembersCount(conn, faction.name().value());
        if (!(countResult instanceof Result.Success<Integer> countSuccess)) return countResult.propagate();
        int memberCount = countSuccess.value();

        int maxPages = Math.max(1, (memberCount + pageable.itemsPerPage() - 1) / pageable.itemsPerPage());
        int page = Math.max(0, Math.min(pageable.currentPage(), maxPages - 1));

        Result<List<FactionPlayer>> membersResult = factionRepository.getFactionMembers(conn, faction.name().value(), new Pageable<>(page, pageable.itemsPerPage()));
        if (!(membersResult instanceof Result.Success<List<FactionPlayer>> membersSuccess)) return membersResult.propagate();
        List<FactionPlayer> members = membersSuccess.value();

        return new Result.Success<>(new FactionMembersPage(faction, new Pageable<>(members, page, maxPages, pageable.itemsPerPage())));
    }
}
