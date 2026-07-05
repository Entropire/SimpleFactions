package com.entropire.simplefactions.faction.request;

import com.entropire.simplefactions.faction.request.exception.JoinRequestException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

public class JoinRequestRepository {
    public void save(Connection connection, JoinRequest joinRequest) throws JoinRequestException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO join_requests (uuid, player_uuid, faction_uuid, expires_at) VALUES (?, ?, ?, ?)")) {
            preparedStatement.setString(1, (joinRequest.uuid() == null ? UUID.randomUUID().toString() : joinRequest.uuid().toString()) );
            preparedStatement.setString(2, joinRequest.playerUuid().toString());
            preparedStatement.setString(2, joinRequest.factionUuid().toString());
            preparedStatement.setTimestamp(4, Timestamp.from(joinRequest.expiresAt()));
            preparedStatement.execute();
        }
        catch (SQLException e) {
            throw new JoinRequestException("Failed to save join request", e);
        }
    }
}
