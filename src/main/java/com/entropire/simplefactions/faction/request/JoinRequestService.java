package com.entropire.simplefactions.faction.request;

import java.sql.Connection;

import com.entropire.simplefactions.objects.Result;

public class JoinRequestService {
    private final JoinRequestRepository repository = new JoinRequestRepository();

    public Result<Boolean> save(Connection connection, JoinRequest joinRequest) {
        return repository.save(connection, joinRequest);
    }
}
