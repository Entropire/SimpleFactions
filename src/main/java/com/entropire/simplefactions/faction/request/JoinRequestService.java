package com.entropire.simplefactions.faction.request;

import java.sql.Connection;

import com.entropire.simplefactions.faction.request.exception.JoinRequestException;

public class JoinRequestService {
    private final JoinRequestRepository repository = new JoinRequestRepository();

    public void save(Connection connection, JoinRequest joinRequest) throws JoinRequestException {
        repository.save(connection, joinRequest);
    }
}
