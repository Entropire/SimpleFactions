package com.entropire.simplefactions.faction.membership;

import java.sql.Connection;
import java.util.UUID;

import com.entropire.simplefactions.faction.membership.exception.FactionMembershipException;

public class FactionMembershipService {
    private final FactionMembershipRepository repository = new FactionMembershipRepository();

    public void save(Connection connection, FactionMembership factionMembership) throws FactionMembershipException {
        repository.save(connection, factionMembership);
    }

    public FactionMembership get(Connection connection, UUID playerUUID) throws FactionMembershipException{
        return repository.getByPlayerUUID(connection, playerUUID);
    }
}
