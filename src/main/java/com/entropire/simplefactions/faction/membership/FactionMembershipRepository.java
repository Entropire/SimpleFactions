package com.entropire.simplefactions.faction.membership;

import com.entropire.simplefactions.objects.Pageable;
import com.entropire.simplefactions.objects.Result;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
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
                        Instant.ofEpochMilli(rs.getLong("joined_at"))
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
                        Instant.ofEpochMilli(rs.getLong("joined_at"))
                    ));
            }

            return new Result.Success<>(factionMembership);
        }
        catch (SQLException e) {
            return new Result.Error<>("Failed to retrieve faction membership", e);
        }
    }

    public Result<FactionMembership> deleteByPlayerUuid(Connection connection, UUID playerUuid) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM faction_memberships WHERE player_uuid = ? RETURNING *")) {
            preparedStatement.setString(1, playerUuid.toString());

            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (!rs.next()) {
                    return new Result.Success<>(null); // nothing was deleted
                }

                return new Result.Success<>(new FactionMembership(
                        UUID.fromString(rs.getString("player_uuid")),
                        UUID.fromString(rs.getString("faction_uuid")),
                        Role.valueOf(rs.getString("role")),
                        Instant.ofEpochMilli(rs.getLong("joined_at"))
                ));
            }
        }
        catch (SQLException e) {
            return new Result.Error<>("Failed to delete faction membership", e);
        }
    }

    public Result<List<String>> getFactionMemberNames(Connection connection, Pageable<String> pageable, UUID factionUuid, String contains) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT p.username FROM faction_memberships m JOIN players p ON p.uuid = m.player_uuid WHERE m.faction_uuid = ? AND p.username LIKE ? || '%' ORDER BY p.username LIMIT ? OFFSET ?")) {
            preparedStatement.setString(1, factionUuid.toString());
            preparedStatement.setString(2, contains);
            preparedStatement.setInt(3, pageable.itemsPerPage());
            preparedStatement.setInt(4, pageable.currentPage() * pageable.itemsPerPage());

            try (ResultSet rs = preparedStatement.executeQuery()) {
                List<String> factionMemberNames = new ArrayList<>();

                while (rs.next()) {
                    factionMemberNames.add(rs.getString("username"));
                }

                return new Result.Success<>(factionMemberNames);
            }
        }
        catch (SQLException e) {
            return new Result.Error<>("Failed to retrieve faction member names", e);
        }
    }

    public Result<FactionMembership> getByPlayerName(Connection connection, String playerName) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT m.* FROM faction_memberships m JOIN players p ON p.uuid = m.player_uuid WHERE p.username = ? COLLATE NOCASE")){
            preparedStatement.setString(1, playerName);

            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (!rs.next()) {
                    return new Result.Success<>(null);
                }

                return new Result.Success<>(new FactionMembership(
                        UUID.fromString(rs.getString("player_uuid")),
                        UUID.fromString(rs.getString("faction_uuid")),
                        Role.valueOf(rs.getString("role")),
                        Instant.ofEpochMilli(rs.getLong("joined_at"))
                ));
            }
        }
        catch (SQLException e) {
            return new Result.Error<>("Failed to retrieve faction member", e);
        }
    }
}
