package com.entropire.simplefactions.db.faction;

import com.entropire.simplefactions.db.DataBaseContext;
import com.entropire.simplefactions.db.faction.exception.FactionException;
import com.entropire.simplefactions.db.faction.exception.FactionNameDuplicateException;
import com.entropire.simplefactions.db.faction.exception.FactionOwnerDuplicateException;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class FactionRepository {
    private DataBaseContext db;

    public FactionRepository(DataBaseContext db) {
        this.db = db;
    }

    public void save(Faction faction) throws FactionException {
        try (PreparedStatement preparedStatement = db.getConnection().prepareStatement("INSERT INTO Factions (uuid, name, color, owner_uuid) VALUES (?, ?, ?, ?)")) {
            preparedStatement.setString(1, (faction.uuid() == null ? UUID.randomUUID().toString() : faction.uuid().toString()) );
            preparedStatement.setString(2, faction.name());
            preparedStatement.setString(3, faction.color());
            preparedStatement.setString(4, faction.ownerUUID().toString());
            preparedStatement.execute();
        }
        catch (SQLException e) {
            String message = e.getMessage();

            if (message.contains("UNIQUE constraint failed: Factions.owner_uuid")) {
                throw new FactionOwnerDuplicateException("Faction owner already in use", e);
            }
            if (message.contains("UNIQUE constraint failed: Factions.name")) {
                throw new FactionNameDuplicateException("Faction name already in use", e);
            }
            throw new FactionException("Failed to save faction", e);
        }
    }
}
