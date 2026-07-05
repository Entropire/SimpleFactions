package com.entropire.simplefactions.faction.invite;

import com.entropire.simplefactions.faction.invite.exception.FactionInviteException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

public class FactionInviteRepository {
    public void save(Connection connection, FactionInvite factionInvite) throws FactionInviteException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO faction_invites (uuid, faction_uuid, player_uuid, invited_by, expires_at) VALUES (?, ?, ?, ?, ?)")) {
            preparedStatement.setString(1, (factionInvite.uuid() == null ? UUID.randomUUID().toString() : factionInvite.uuid().toString()) );
            preparedStatement.setString(2, factionInvite.factionUuid().toString());
            preparedStatement.setString(3, factionInvite.playerUuid().toString());
            preparedStatement.setString(4, factionInvite.invitedBy().toString());
            preparedStatement.setTimestamp(5, Timestamp.from(factionInvite.expiresAt()));
            preparedStatement.execute();
        }
        catch (SQLException e) {
            throw new FactionInviteException("Failed to save faction invite", e);
        }
    }
}
