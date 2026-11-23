package com.entropire.simplefactions.faction.membership;

import java.sql.Connection;
import java.util.List;
import java.util.UUID;

import com.entropire.simplefactions.objects.Result;

public class FactionMembershipService {
    private final FactionMembershipRepository repository = new FactionMembershipRepository();

    public Result<Boolean> save(Connection connection, FactionMembership factionMembership) {
        return repository.save(connection, factionMembership);
    }

    public Result<FactionMembership> getByPlayerUuid(Connection connection, UUID uuid) {
        return repository.getByPlayerUuid(connection, uuid);
    }

    public Result<List<FactionMembership>> getAllByFactionUuid(Connection connection, UUID uuid) {
        return repository.getAllByFactionUuid(connection, uuid);
    }
}
