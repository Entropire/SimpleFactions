package com.entropire.simplefactions.faction;

import com.entropire.simplefactions.faction.exception.FactionException;
import com.entropire.simplefactions.faction.exception.FactionNameDuplicateException;
import com.entropire.simplefactions.faction.exception.FactionOwnerDuplicateException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class FactionRepository {
    public Faction save(Connection connection, Faction faction) throws FactionOwnerDuplicateException, FactionNameDuplicateException, FactionException {
        UUID uuid = faction.uuid() == null ? UUID.randomUUID() : faction.uuid();

        Faction created = new Faction(
            uuid, 
            faction.name(), 
            faction.color(),
            faction.ownerUUID(),
            null,
            true,
            null 
            );

        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO factions (uuid, name, color, owner_uuid) VALUES (?, ?, ?, ?)")) {
            preparedStatement.setString(1, created.uuid().toString());
            preparedStatement.setString(2, faction.name());
            preparedStatement.setString(3, faction.color());
            preparedStatement.setString(4, faction.ownerUUID().toString());
            preparedStatement.executeUpdate();
        }
        catch (SQLException e) {
            String message = e.getMessage().toLowerCase();

            if (message.contains("unique constraint failed: factions.owner_uuid")) {
                throw new FactionOwnerDuplicateException("Faction owner already in use", e);
            }
            if (message.contains("unique constraint failed: factions.name")) {
                throw new FactionNameDuplicateException("Faction name already in use", e);
            }
            throw new FactionException("Failed to save faction", e);
        }

        return created;
    }

    public void delete(Connection connection, UUID uuid) throws FactionException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM factions WHERE uuid IS ? ")) {
            preparedStatement.setString(1, uuid.toString());
            preparedStatement.execute();
        }
        catch (SQLException e) {
            throw new FactionException("Failed to delete faction with uuid: '" + uuid.toString() + "'", e);
        }
    }
}
