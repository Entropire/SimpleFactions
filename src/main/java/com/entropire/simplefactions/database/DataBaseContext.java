package com.entropire.simplefactions.database;

import com.entropire.simplefactions.objects.Result;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Scanner;

public class DataBaseContext {

    private final String path;

    public DataBaseContext(String path) {
        this.path = path;
    }

    public interface SQLAction<T> {
        Result<T> execute(Connection conn) throws Exception;
    }

    public <T> Result<T> withConnection(SQLAction<T> action) {
        try (Connection conn = openConnection()) {
            return action.execute(conn);
        } catch (Exception e) {
            return new Result.Error<>("Database operation failed", e);
        }
    }

    public <T> Result<T> transaction(SQLAction<T> action) {
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
        }
    }

    private Connection openConnection() throws Exception {
        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + path);

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
        }

        return conn;
    }

    /** Creates all tables and indexes. Runs in one transaction, so a broken schema leaves nothing half-created. */
    public Result<Boolean> initSchema() {
        return transaction(conn -> {
            String sql = loadSql();

            try (Statement stmt = conn.createStatement()) {
                for (String raw : sql.split(";")) {
                    String statement = raw.trim();
                    if (statement.isEmpty()) continue;

                    stmt.execute(statement);
                }
            }

            return new Result.Success<>(true);
        });
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