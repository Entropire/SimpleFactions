package com.entropire.simplefactions.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.entropire.simplefactions.objects.Result;

class DataBaseContextTest {

    @TempDir
    Path tempDir;

    private Path dbFile() {
        return tempDir.resolve("Simple-Faction.db");
    }

    private SchemaStatus init(DataBaseContext db, String pluginVersion) {
        Result<SchemaStatus> result = db.initSchema(pluginVersion);
        Result.Success<SchemaStatus> success = assertInstanceOf(Result.Success.class, result, () -> "initSchema failed: " + result);
        return success.value();
    }

    private void execute(String sql) throws Exception {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbFile());
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    @Test
    void freshDatabaseIsReadyAndStaysReadyOnRestart() {
        DataBaseContext db = new DataBaseContext(dbFile().toString());

        assertInstanceOf(SchemaStatus.Ready.class, init(db, "3.0"));
        assertInstanceOf(SchemaStatus.Ready.class, init(db, "3.0"));
    }

    @Test
    void databaseWithoutVersionInfoIsIncompatible() throws Exception {
        execute("CREATE TABLE factions (uuid TEXT PRIMARY KEY)");
        DataBaseContext db = new DataBaseContext(dbFile().toString());

        SchemaStatus.Incompatible status = assertInstanceOf(SchemaStatus.Incompatible.class, init(db, "3.0"));
        assertEquals(0, status.foundSchemaVersion());
        assertEquals(null, status.createdByPluginVersion());
    }

    @Test
    void databaseFromOtherSchemaVersionReportsItsPluginVersion() throws Exception {
        DataBaseContext db = new DataBaseContext(dbFile().toString());
        init(db, "3.0");
        execute("UPDATE plugin_meta SET value = '" + (DataBaseContext.SCHEMA_VERSION + 1) + "' WHERE key = 'schema_version'");
        execute("UPDATE plugin_meta SET value = '4.2' WHERE key = 'plugin_version'");

        SchemaStatus.Incompatible status = assertInstanceOf(SchemaStatus.Incompatible.class, init(db, "3.0"));
        assertEquals(DataBaseContext.SCHEMA_VERSION + 1, status.foundSchemaVersion());
        assertEquals("4.2", status.createdByPluginVersion());
    }

    @Test
    void resetKeepsBackupAndGivesAnEmptyCompatibleDatabase() throws Exception {
        execute("CREATE TABLE factions (uuid TEXT PRIMARY KEY)");
        DataBaseContext db = new DataBaseContext(dbFile().toString());
        assertInstanceOf(SchemaStatus.Incompatible.class, init(db, "3.0"));

        Result<Path> reset = db.resetDatabase();
        if (!(reset instanceof Result.Success<Path>(Path backup))) throw new AssertionError("reset failed: " + reset);

        assertTrue(Files.exists(backup), "backup file should exist");
        assertFalse(Files.exists(dbFile()), "old database file should be moved away");
        assertInstanceOf(SchemaStatus.Ready.class, init(db, "3.0"));
    }
}
