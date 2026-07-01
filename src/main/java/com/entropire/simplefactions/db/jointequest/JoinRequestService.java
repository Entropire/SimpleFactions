package com.entropire.simplefactions.db.jointequest;

import com.entropire.simplefactions.db.jointequest.exception.JoinRequestException;

public class JoinRequestService {
    private final JoinRequestRepository repository;

    public JoinRequestService(JoinRequestRepository repository) {
        this.repository = repository;
    }

    public void save(JoinRequest joinRequest) throws JoinRequestException {
        repository.save(joinRequest);
    }
}
