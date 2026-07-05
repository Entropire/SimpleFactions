package com.entropire.simplefactions.faction;

import com.entropire.simplefactions.faction.exception.FactionException;
import com.entropire.simplefactions.faction.exception.FactionNameDuplicateException;
import com.entropire.simplefactions.faction.exception.FactionOwnerDuplicateException;
import com.entropire.simplefactions.objects.Pageable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
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

    public List<Faction> getFactions(Connection connection, Pageable<Faction> pageable){
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM factions ORDER BY last_activity DESC LIMIT ? OFFSET ?")) {
            preparedStatement.setInt(1, pageable.itemsPerPage());
            preparedStatement.setInt(2, pageable.currentPage() * pageable.itemsPerPage());
            ResultSet rs = preparedStatement.executeQuery();

            List<Faction> factions = new ArrayList<>();

            while(rs.next()){
                factions.add(new Faction(
                    UUID.fromString(rs.getString("uuid")),
                    rs.getString("name"),
                    rs.getString("color"),
                    UUID.fromString(rs.getString("owner_uuid")),
                    Instant.ofEpochMilli(rs.getLong("created_at")),
                    rs.getBoolean("is_active"),
                    Instant.ofEpochMilli(rs.getLong("last_activity"))
                ));
            }

            return factions;
        }
        catch (SQLException e) {
            throw new FactionException("Failed to retrieve factions", e);
        }
    }

    public int getfactionsCount(Connection connection){
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(*) FROM factions")) {
            ResultSet rs = preparedStatement.executeQuery();
            
            int count = 0;
            while (rs.next()) {
                count = rs.getInt(1);
            }

            return count;
        }
        catch (SQLException e) {
            throw new FactionException("Failed to retrieve faction count", e);
        }
    }
}
