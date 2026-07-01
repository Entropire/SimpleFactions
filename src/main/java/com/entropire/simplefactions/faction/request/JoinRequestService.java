package com.entropire.simplefactions.faction.request;

import com.entropire.simplefactions.faction.request.exception.JoinRequestException;

public class JoinRequestService {
    private final JoinRequestRepository repository;

    public JoinRequestService(JoinRequestRepository repository) {
        this.repository = repository;
    }

    public void save(JoinRequest joinRequest) throws JoinRequestException {
        repository.save(joinRequest);
    }
}
