package com.entropire.simplefactions.database;

import com.entropire.simplefactions.objects.Result;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantLock;

public class DataBaseContext {

    public static final int SCHEMA_VERSION = 1;

    private static final int BUSY_TIMEOUT_MILLIS = 5000;

    private final String path;

    private final ReentrantLock lock = new ReentrantLock(true);

    public DataBaseContext(String path) {
        this.path = path;
    }

    public interface SQLAction<T> {
        Result<T> execute(Connection conn) throws Exception;
    }

    public <T> Result<T> withConnection(SQLAction<T> action) {
        lock.lock();
        try (Connection conn = openConnection()) {
            return action.execute(conn);
        } catch (Exception e) {
            return new Result.Error<>("Database operation failed", e);
        } finally {
            lock.unlock();
        }
    }

    public <T> Result<T> transaction(SQLAction<T> action) {
        lock.lock();
        try (Connection conn = openConnection()) {
            conn.setAutoCommit(false);
            try {
                Result<T> result = action.execute(conn);
                if (result.isSuccess()) conn.commit();
                else conn.rollback();
                return result;
            } catch (Exception e) {
                conn.rollback();
                return new Result.Error<>("Database operation failed", e);
            }
        } catch (Exception e) {
            return new Result.Error<>("Database operation failed", e);
        } finally {
            lock.unlock();
        }
    }

    private Connection openConnection() throws Exception {
        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + path);

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
            stmt.execute("PRAGMA busy_timeout = " + BUSY_TIMEOUT_MILLIS);
        }

        return conn;
    }

    public Result<SchemaStatus> initSchema(String pluginVersion) {
        return transaction(conn -> {
            boolean hasMeta = tableExists(conn, "plugin_meta");
            boolean hasData = tableExists(conn, "factions");

            if (hasMeta || hasData) {
                String storedSchema = hasMeta ? readMeta(conn, "schema_version") : null;
                int foundSchemaVersion = storedSchema == null ? 0 : Integer.parseInt(storedSchema);

                if (foundSchemaVersion != SCHEMA_VERSION) {
                    String createdBy = hasMeta ? readMeta(conn, "plugin_version") : null;
                    return new Result.Success<>(new SchemaStatus.Incompatible(foundSchemaVersion, createdBy));
                }
            }

            String sql = loadSql();
            try (Statement stmt = conn.createStatement()) {
                for (String raw : sql.split(";")) {
                    String statement = raw.trim();
                    if (statement.isEmpty()) continue;

                    stmt.execute(statement);
                }
            }

            writeMeta(conn, "schema_version", String.valueOf(SCHEMA_VERSION));
            writeMeta(conn, "plugin_version", pluginVersion);
            return new Result.Success<>(new SchemaStatus.Ready());
        });
    }
    
    public Result<Path> resetDatabase() {
        Path database = Path.of(path);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        Path backup = database.resolveSibling(database.getFileName() + ".backup-" + timestamp);

        // Hold the lock so the file isn't moved while another thread has it open.
        lock.lock();
        try {
            Files.move(database, backup);
            return new Result.Success<>(backup);
        } catch (IOException e) {
            return new Result.Error<>("Could not move " + database + " to " + backup, e);
        } finally {
            lock.unlock();
        }
    }

    private boolean tableExists(Connection conn, String table) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = ?")) {
            stmt.setString(1, table);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private String readMeta(Connection conn, String key) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT value FROM plugin_meta WHERE key = ?")) {
            stmt.setString(1, key);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    private void writeMeta(Connection conn, String key, String value) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO plugin_meta (key, value) VALUES (?, ?) ON CONFLICT(key) DO UPDATE SET value = excluded.value")) {
            stmt.setString(1, key);
            stmt.setString(2, value);
            stmt.execute();
        }
    }

    private String loadSql() {
        InputStream is = getClass().getClassLoader().getResourceAsStream("db/schema.sql");

        if (is == null) {
            throw new RuntimeException("SQL file schema.sql not found");
        }

        try (Scanner scanner = new Scanner(is, StandardCharsets.UTF_8)) {
            return scanner.useDelimiter("\\A").next();
        }
    }
}