package com.entropire.simplefactions.faction.membership;

import com.entropire.simplefactions.database.DataBaseContext;
import com.entropire.simplefactions.faction.membership.exception.FactionMembershipException;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class FactionMembershipRepository {
    private final DataBaseContext db;

    public FactionMembershipRepository(DataBaseContext db) {
        this.db = db;
    }

    public void save(FactionMembership factionMembership) throws FactionMembershipException {
        try (PreparedStatement preparedStatement = db.getConnection().prepareStatement("INSERT INTO faction_memberships (player_uuid, faction_uuid, role) VALUES (?, ?, ?)")) {
            preparedStatement.setString(1, factionMembership.playerUuid().toString());
            preparedStatement.setString(2, factionMembership.factionUuid().toString());
            preparedStatement.setString(3, factionMembership.role().toString());
            preparedStatement.execute();
        }
        catch (SQLException e) {
            throw new FactionMembershipException("Failed to save faction membership", e);
        }
    }
}
