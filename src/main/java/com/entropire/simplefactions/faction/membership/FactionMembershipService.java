package com.entropire.simplefactions.faction.membership;

import java.util.List;
import java.util.UUID;

import com.entropire.simplefactions.database.DataBaseContext;
import com.entropire.simplefactions.objects.Pageable;
import com.entropire.simplefactions.objects.Result;

public class FactionMembershipService {
    private final DataBaseContext db;
    private final FactionMembershipRepository membershipRepository;

    public FactionMembershipService(DataBaseContext db, FactionMembershipRepository membershipRepository) {
        this.db = db;
        this.membershipRepository = membershipRepository;
    }

    public Result<FactionMembership> leaveFaction(UUID playerUuid) {
        return db.transaction(conn -> {
            Result<FactionMembership> memberResult = Result.require(membershipRepository.findByPlayerUuid(conn, playerUuid), "You are not part of a faction!");
            if (!(memberResult instanceof Result.Success<FactionMembership> memberSuccess)) return memberResult;
            if (memberSuccess.value().role() == Role.OWNER) return new Result.Failure<>("The owner can't leave the faction. Transfer ownership or delete the faction instead.");

            return Result.require(membershipRepository.deleteByPlayerUuid(conn, playerUuid), "You are not part of a faction!");
        });
    }

    public Result<FactionMembership> kickFactionMember(UUID playerUuid, String userName) {
        return db.transaction(conn -> {
            Result<FactionMembership> membershipResult = Result.require(membershipRepository.findByPlayerUuid(conn, playerUuid), "You are not in a faction!");
            if (!(membershipResult instanceof Result.Success<FactionMembership> membershipSuccess)) return membershipResult;
            FactionMembership membership = membershipSuccess.value();
            if (membership.role() != Role.OWNER) return new Result.Failure<>("You must be the owner to kick somebody from the faction!");

            String notInFaction = userName + " is not part of your faction!";
            Result<FactionMembership> kickMembershipResult = Result.require(membershipRepository.findByPlayerName(conn, userName), notInFaction);
            if (!(kickMembershipResult instanceof Result.Success<FactionMembership> kickMembershipSuccess)) return kickMembershipResult;
            FactionMembership kickMembership = kickMembershipSuccess.value();
            if (!kickMembership.factionUuid().equals(membership.factionUuid())) return new Result.Failure<>(notInFaction);
            if (kickMembership.playerUuid().equals(playerUuid)) return new Result.Failure<>("You can't kick yourself. Transfer ownership or delete the faction instead.");

            return Result.require(membershipRepository.deleteByPlayerUuid(conn, kickMembership.playerUuid()), notInFaction);
        });
    }

    public Result<Pageable<String>> getFactionMemberNames(Pageable<String> pageable, UUID playerUuid, String contains) {
        return db.withConnection(conn -> {
            Result<FactionMembership> membershipResult = Result.require(membershipRepository.findByPlayerUuid(conn, playerUuid), "You are not in a faction!");
            if (!(membershipResult instanceof Result.Success<FactionMembership> membershipSuccess)) return membershipResult.propagate();

            Result<List<String>> namesResult = membershipRepository.getFactionMemberNames(conn, pageable, membershipSuccess.value().factionUuid(), contains);
            if (!(namesResult instanceof Result.Success<List<String>> namesSuccess)) return namesResult.propagate();

            return new Result.Success<>(new Pageable<>(namesSuccess.value(), pageable.currentPage(), 1, pageable.itemsPerPage()));
        });
    }
}
