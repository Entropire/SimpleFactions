package com.entropire.simplefactions.faction.membership;

import com.entropire.simplefactions.faction.membership.exception.FactionMembershipException;

public class FactionMembershipService {
    private final FactionMembershipRepository repository;

    public FactionMembershipService(FactionMembershipRepository repository) {
        this.repository = repository;
    }

    public void save(FactionMembership factionMembership) throws FactionMembershipException {
        repository.save(factionMembership);
    }
}
