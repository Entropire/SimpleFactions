package com.entropire.simplefactions.faction.membership;

import com.entropire.simplefactions.database.SqlLike;
import com.entropire.simplefactions.objects.Pageable;
import com.entropire.simplefactions.objects.Result;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

    public Result<Optional<FactionMembership>> findByPlayerUuid(Connection connection, UUID uuid) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM faction_memberships WHERE player_uuid = ?")) {
            preparedStatement.setString(1, uuid.toString());

            try (ResultSet rs = preparedStatement.executeQuery()) {
                return new Result.Success<>(rs.next() ? Optional.of(map(rs)) : Optional.empty());
            }
        }
        catch (SQLException e) {
            return new Result.Error<>("Failed to retrieve faction membership", e);
        }
    }

    public Result<Optional<FactionMembership>> findByPlayerName(Connection connection, String playerName) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT m.* FROM faction_memberships m JOIN players p ON p.uuid = m.player_uuid WHERE p.username = ? COLLATE NOCASE")) {
            preparedStatement.setString(1, playerName);

            try (ResultSet rs = preparedStatement.executeQuery()) {
                return new Result.Success<>(rs.next() ? Optional.of(map(rs)) : Optional.empty());
            }
        }
        catch (SQLException e) {
            return new Result.Error<>("Failed to retrieve faction member", e);
        }
    }

    public Result<List<FactionMembership>> getAllByFactionUuid(Connection connection, UUID uuid) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM faction_memberships WHERE faction_uuid = ?")) {
            preparedStatement.setString(1, uuid.toString());

            try (ResultSet rs = preparedStatement.executeQuery()) {
                List<FactionMembership> memberships = new ArrayList<>();

                while (rs.next()) {
                    memberships.add(map(rs));
                }

                return new Result.Success<>(memberships);
            }
        }
        catch (SQLException e) {
            return new Result.Error<>("Failed to retrieve faction membership", e);
        }
    }

    public Result<Optional<FactionMembership>> deleteByPlayerUuid(Connection connection, UUID playerUuid) {
        Result<Optional<FactionMembership>> existingResult = findByPlayerUuid(connection, playerUuid);
        if (!(existingResult instanceof Result.Success<Optional<FactionMembership>> existingSuccess)) return existingResult;
        Optional<FactionMembership> existing = existingSuccess.value();
        if (existing.isEmpty()) return existingResult;

        try (PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM faction_memberships WHERE player_uuid = ?")) {
            preparedStatement.setString(1, playerUuid.toString());
            preparedStatement.executeUpdate();
        }
        catch (SQLException e) {
            return new Result.Error<>("Failed to delete faction membership", e);
        }

        return new Result.Success<>(existing);
    }

    public Result<List<String>> getFactionMemberNames(Connection connection, Pageable<String> pageable, UUID factionUuid, String contains) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT p.username FROM faction_memberships m JOIN players p ON p.uuid = m.player_uuid WHERE m.faction_uuid = ? AND p.username LIKE ? ESCAPE '\\' ORDER BY p.username LIMIT ? OFFSET ?")) {
            preparedStatement.setString(1, factionUuid.toString());
            preparedStatement.setString(2, SqlLike.startsWith(contains));
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

    private static FactionMembership map(ResultSet rs) throws SQLException {
        return new FactionMembership(
                UUID.fromString(rs.getString("player_uuid")),
                UUID.fromString(rs.getString("faction_uuid")),
                Role.valueOf(rs.getString("role")),
                Instant.ofEpochMilli(rs.getLong("joined_at"))
        );
    }
}
