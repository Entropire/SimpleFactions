package com.entropire.simplefactions.faction.membership;

import com.entropire.simplefactions.objects.Result;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FactionMembershipRepository {
    public Result<Boolean> save(Connection connection, FactionMembership factionMembership) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO faction_memberships (player_uuid, faction_uuid, role) VALUES (?, ?, ?)")) {
            preparedStatement.setString(1, factionMembership.playerUuid().toString());
            preparedStatement.setString(2, factionMembership.factionUuid().toString());
            preparedStatement.setString(3, factionMembership.role().toString());
            preparedStatement.execute();
        }
        catch (SQLException e) {
            return new Result.Error<>("Failed to save faction membership", e);
        }

        return new Result.Success<>(true);
    }

    public Result<FactionMembership> getByPlayerUuid(Connection connection, UUID uuid) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM faction_memberships Where player_uuid = ?")) {
            preparedStatement.setString(1, uuid.toString());
            ResultSet rs = preparedStatement.executeQuery();

            FactionMembership factionMembership = null;

            while (rs.next()) {
                factionMembership = new FactionMembership(
                    UUID.fromString(rs.getString("player_uuid")), 
                    UUID.fromString(rs.getString("faction_uuid")), 
                    Role.valueOf(rs.getString("role")),
                    rs.getTimestamp("joined_at").toInstant() 
                );
            }

            return new Result.Success<>(factionMembership);
        }
        catch (SQLException e) {
            return new Result.Error<>("Failed to retrieve faction membership", e);
        }
    }

    public Result<List<FactionMembership>> getAllByFactionUuid(Connection connection, UUID uuid) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM faction_memberships Where faction_uuid = ?")) {
            preparedStatement.setString(1, uuid.toString());
            ResultSet rs = preparedStatement.executeQuery();

            List<FactionMembership> factionMembership = new ArrayList<>();
 
            while (rs.next()) {
                factionMembership.add(
                    new FactionMembership(
                    UUID.fromString(rs.getString("player_uuid")), 
                    UUID.fromString(rs.getString("faction_uuid")), 
                    Role.valueOf(rs.getString("role")),
                    rs.getTimestamp("joined_at").toInstant())
                );
            }

            return new Result.Success<>(factionMembership);
        }
        catch (SQLException e) {
            return new Result.Error<>("Failed to retrieve faction membership", e);
        }
    }
}
