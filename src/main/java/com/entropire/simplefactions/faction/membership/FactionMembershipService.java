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
            Result<FactionMembership> memberResult = membershipRepository.getByPlayerUuid(conn, playerUuid);
            if (!(memberResult instanceof Result.Success(var member))) return memberResult.propagate();
            if (member == null) return new Result.Failure<>("You are not part of a faction!");
            if (member.role() == Role.OWNER) return new Result.Failure<>("The owner can't leave the faction. Transfer ownership or delete the faction instead.");

            return membershipRepository.deleteByPlayerUuid(conn, playerUuid);
        });
    }

    public Result<FactionMembership> kickFactionMember(UUID playerUuid, String userName) {
        return db.transaction(conn -> {
            Result<FactionMembership> membershipResult = membershipRepository.getByPlayerUuid(conn, playerUuid);
            if (!(membershipResult instanceof Result.Success(var membership))) return membershipResult.propagate();
            if (membership == null || membership.role() != Role.OWNER) return new Result.Failure<>("You must be the owner to kick somebody from the faction!");

            Result<FactionMembership> kickMembershipResult = membershipRepository.getByPlayerName(conn, userName);
            if (!(kickMembershipResult instanceof Result.Success(var kickMembership))) return kickMembershipResult.propagate();
            if (kickMembership == null || !kickMembership.factionUuid().equals(membership.factionUuid())) return new Result.Failure<>(userName + " is not part of your faction!");
            if (kickMembership.playerUuid().equals(playerUuid)) return new Result.Failure<>("You can't kick yourself. Transfer ownership or delete the faction instead.");

            return membershipRepository.deleteByPlayerUuid(conn, kickMembership.playerUuid());
        });
    }

    public Result<Pageable<String>> getFactionMemberNames(Pageable<String> pageable, UUID playerUuid, String contains) {
        return db.withConnection(conn -> {
            Result<FactionMembership> membershipResult = membershipRepository.getByPlayerUuid(conn, playerUuid);
            if (!(membershipResult instanceof Result.Success(var membership))) return membershipResult.propagate();
            if (membership == null) return new Result.Failure<>("You are not in a faction!");

            Result<List<String>> namesResult = membershipRepository.getFactionMemberNames(conn, pageable, membership.factionUuid(), contains);
            if (!(namesResult instanceof Result.Success(var names))) return namesResult.propagate();

            return new Result.Success<>(new Pageable<>(names, pageable.currentPage(), 1, pageable.itemsPerPage()));
        });
    }
}
