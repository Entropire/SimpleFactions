package com.entropire.simplefactions.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.entropire.simplefactions.faction.Faction;
import com.entropire.simplefactions.faction.FactionName;
import com.entropire.simplefactions.faction.FactionRepository;
import com.entropire.simplefactions.faction.membership.FactionMembership;
import com.entropire.simplefactions.faction.membership.FactionMembershipRepository;
import com.entropire.simplefactions.faction.membership.Role;
import com.entropire.simplefactions.objects.Result;
import com.entropire.simplefactions.player.FactionPlayer;
import com.entropire.simplefactions.player.FactionPlayerRepository;

class SqliteCompatibilityTest {

    @TempDir
    Path tempDir;

    private DataBaseContext db;
    private final FactionPlayerRepository playerRepository = new FactionPlayerRepository();
    private final FactionRepository factionRepository = new FactionRepository();
    private final FactionMembershipRepository membershipRepository = new FactionMembershipRepository();

    @BeforeEach
    void setUp() {
        db = new DataBaseContext(tempDir.resolve("Simple-Faction.db").toString());
        assertInstanceOf(Result.Success.class, db.initSchema("test"));
    }

    private static <T> T success(Result<T> result) {
        if (!(result instanceof Result.Success<T> success)) throw new AssertionError("expected success but got " + result);
        return success.value();
    }

    @Test
    void usesExpectedSqliteVersion() {
        String expected = System.getProperty("simplefactions.expectedSqliteVersion");
        assumeTrue(expected != null, "no expected SQLite version set; only checked by the testSqlite* tasks");

        String actual = success(db.withConnection(conn -> new Result.Success<>(conn.getMetaData().getDatabaseProductVersion())));
        assertEquals(expected, actual);
    }

    @Test
    void savingAnExistingPlayerUpdatesTheUsername() {
        UUID uuid = UUID.randomUUID();

        success(db.transaction(conn -> playerRepository.saveIfNotExists(conn, new FactionPlayer(uuid, "OldName", null, null))));
        success(db.transaction(conn -> playerRepository.saveIfNotExists(conn, new FactionPlayer(uuid, "NewName", null, null))));

        String username = success(db.withConnection(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement("SELECT username FROM players WHERE uuid = ?")) {
                stmt.setString(1, uuid.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    assertTrue(rs.next(), "player row should exist");
                    String name = rs.getString(1);
                    assertFalse(rs.next(), "upsert should not create a second row");
                    return new Result.Success<>(name);
                }
            }
        }));
        assertEquals("NewName", username);
    }

    @Test
    void deletingAMembershipReturnsTheDeletedRow() {
        UUID ownerUuid = UUID.randomUUID();
        UUID memberUuid = UUID.randomUUID();

        UUID factionUuid = success(db.transaction(conn -> {
            success(playerRepository.saveIfNotExists(conn, new FactionPlayer(ownerUuid, "Owner", null, null)));
            success(playerRepository.saveIfNotExists(conn, new FactionPlayer(memberUuid, "Member", null, null)));
            Faction faction = success(factionRepository.save(conn, new Faction(success(FactionName.parse("testers")), "#ffffff", ownerUuid)));
            success(membershipRepository.save(conn, new FactionMembership(ownerUuid, faction.uuid(), Role.OWNER, null)));
            success(membershipRepository.save(conn, new FactionMembership(memberUuid, faction.uuid(), Role.MEMBER, null)));
            return new Result.Success<>(faction.uuid());
        }));

        Optional<FactionMembership> deleted = success(db.transaction(conn -> membershipRepository.deleteByPlayerUuid(conn, memberUuid)));
        assertTrue(deleted.isPresent(), "DELETE ... RETURNING should return the deleted membership");
        assertEquals(memberUuid, deleted.get().playerUuid());
        assertEquals(factionUuid, deleted.get().factionUuid());
        assertEquals(Role.MEMBER, deleted.get().role());

        Optional<FactionMembership> afterDelete = success(db.withConnection(conn -> membershipRepository.findByPlayerUuid(conn, memberUuid)));
        assertTrue(afterDelete.isEmpty(), "membership should be gone after the delete");

        Optional<FactionMembership> deletedAgain = success(db.transaction(conn -> membershipRepository.deleteByPlayerUuid(conn, memberUuid)));
        assertTrue(deletedAgain.isEmpty(), "deleting a missing membership should return nothing");
    }
}
