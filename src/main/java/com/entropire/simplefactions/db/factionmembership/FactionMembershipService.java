package com.entropire.simplefactions.db.factionmembership;

import com.entropire.simplefactions.db.factionmembership.exception.FactionMembershipException;

public class FactionMembershipService {
    private final FactionMembershipRepository repository;

    public FactionMembershipService(FactionMembershipRepository repository) {
        this.repository = repository;
    }

    public void save(FactionMembership factionMembership) throws FactionMembershipException {
        repository.save(factionMembership);
    }
}
