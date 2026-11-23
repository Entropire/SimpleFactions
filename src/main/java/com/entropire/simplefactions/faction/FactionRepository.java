package com.entropire.simplefactions.faction;

import com.entropire.simplefactions.objects.Pageable;
import com.entropire.simplefactions.objects.Result;
import com.entropire.simplefactions.player.ChatMode;
import com.entropire.simplefactions.player.FactionPlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FactionRepository {
    public Result<Faction> save(Connection connection, Faction faction) {
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
                return new Result.Failure<>("Faction owner already in use");
            }
            if (message.contains("unique constraint failed: factions.name")) {
                return new Result.Failure<>("Faction name already in use");
            }
            return new Result.Error<>("Failed to save faction", e);
        }

        return new Result.Success<>(created);
    }

    public Result<Boolean> delete(Connection connection, UUID uuid) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM factions WHERE uuid IS ? ")) {
            preparedStatement.setString(1, uuid.toString());
            preparedStatement.execute();
        }
        catch (SQLException e) {
            return new Result.Error<>("Failed to delete faction with uuid: '" + uuid.toString() + "'", e);
        }

        return new Result.Success<>(true);
    }

    public Result<List<Faction>> getFactions(Connection connection, Pageable<Faction> pageable){
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

            return new Result.Success<>(factions);
        }
        catch (SQLException e) {
            return new Result.Error<>("Failed to retrieve factions", e);
        }
    }

    public Result<Integer> getFactionsCount(Connection connection){
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(*) FROM factions")) {
            ResultSet rs = preparedStatement.executeQuery();
            
            int count = 0;
            while (rs.next()) {
                count = rs.getInt(1);
            }

            return new Result.Success<>(count);
        }
        catch (SQLException e) {
            return new Result.Error<>("Failed to retrieve faction count", e);
        }
    }

    public Result<FactionPlayer> getOwner(Connection connection, String factionName){
      try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT p.* from factions f JOIN players p ON p.uuid = f.owner_uuid WHERE f.name = ?")) {
        preparedStatement.setString(1, factionName);    
        ResultSet rs = preparedStatement.executeQuery();
            
            FactionPlayer owner = null;
            while (rs.next()) {
                owner = new FactionPlayer(
                    UUID.fromString(rs.getString("uuid")), 
                    rs.getString("username"), 
                    ChatMode.valueOf(rs.getString("chat_mode")), 
                    Instant.ofEpochMilli(rs.getLong("last_seen"))
                );
            }

            return new Result.Success<>(owner);
        }
        catch (SQLException e) {
            return new Result.Error<>("Failed to retrieve owner of faction", e);
        }
    }

    public Result<List<String>> getFactionNames(Connection connection, Pageable<String> pageable, String contains){
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT name FROM factions WHERE name LIKE ? || '%' LIMIT ? OFFSET ?")) {
            preparedStatement.setString(1, contains);
            preparedStatement.setInt(2, pageable.itemsPerPage());
            preparedStatement.setInt(3, pageable.currentPage() * pageable.itemsPerPage());
            ResultSet rs = preparedStatement.executeQuery();

            List<String> factionNames = new ArrayList<>();

            while(rs.next()){
                factionNames.add(rs.getString("name"));
            }

            return new Result.Success<>(factionNames);
        }
        catch (SQLException e) {
            return new Result.Error<>("Failed to retrieve factions", e);
        }
    }
}