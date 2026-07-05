package com.entropire.simplefactions.faction.membership;

import java.sql.Connection;
import java.util.List;
import java.util.UUID;

import com.entropire.simplefactions.faction.membership.exception.FactionMembershipException;

public class FactionMembershipService {
    private final FactionMembershipRepository repository = new FactionMembershipRepository();

    public void save(Connection connection, FactionMembership factionMembership) throws FactionMembershipException {
        repository.save(connection, factionMembership);
    }

    public FactionMembership getByPlayerUuid(Connection connection, UUID uuid) throws FactionMembershipException{
        return repository.getByPlayerUuid(connection, uuid);
    }

    public List<FactionMembership> getAllByFactionUuid(Connection connection, UUID uuid) throws FactionMembershipException{
        return repository.getAllByFactionUuid(connection, uuid);
    }
}
