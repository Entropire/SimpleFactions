package com.entropire.simplefactions.db.player;

import com.entropire.simplefactions.db.DataBaseContext;
import com.entropire.simplefactions.db.player.exception.PlayerException;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PlayerRepository {
    private DataBaseContext db;

    public PlayerRepository(DataBaseContext db) {
        this.db = db;
    }

    public void save(Player player) throws PlayerException {
        try (PreparedStatement preparedStatement = db.getConnection().prepareStatement("INSERT INTO Factions (uuid, username) VALUES (?, ?)")) {
            preparedStatement.setString(1, player.uuid().toString());
            preparedStatement.setString(2, player.username());
            preparedStatement.execute();
        }
        catch (SQLException e) {
            throw new PlayerException("Failed to save player", e);
        }
    }
}
