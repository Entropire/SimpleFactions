package com.entropire.simplefactions.player;

import com.entropire.simplefactions.player.exception.PlayerException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PlayerRepository {
    public void save(Connection connection, Player player) throws PlayerException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO players (uuid, username) VALUES (?, ?)")) {
            preparedStatement.setString(1, player.uuid().toString());
            preparedStatement.setString(2, player.username());
            preparedStatement.execute();
        }
        catch (SQLException e) {
            throw new PlayerException("Failed to save player", e);
        }
    }
}
