package com.entropire.simplefactions.player;

import com.entropire.simplefactions.objects.Result;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;

public class FactionPlayerRepository {
    public Result<Boolean> saveIfNotExists(Connection connection, FactionPlayer player) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO players (uuid, username) VALUES (?, ?) on CONFLICT(uuid) DO UPDATE SET username = excluded.username")) {
            preparedStatement.setString(1, player.uuid().toString());
            preparedStatement.setString(2, player.username());
            preparedStatement.execute();
        }
        catch (SQLException e) {
            return new Result.Error<>("Failed to save player", e);
        }

        return new Result.Success<>(true);
    }

    public Result<Boolean> updatePlayerLastSeen(Connection connection, UUID uuid, Instant lastSeen){
        try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE players SET last_seen = ? WHERE uuid = ?")) {
            preparedStatement.setLong(1, lastSeen.toEpochMilli());
            preparedStatement.setString(2, uuid.toString());
            preparedStatement.execute();
        }
        catch (SQLException e) {
            return new Result.Error<>("Failed to update lastseen value of player: " + uuid, e);
        }

        return new Result.Success<>(true);
    }
}
