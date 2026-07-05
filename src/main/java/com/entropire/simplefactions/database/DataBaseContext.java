package com.entropire.simplefactions.database;

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
        initSchema();
    }
    
    public interface SQLAction<T> {
        T execute(Connection conn) throws Exception;
    }

    public <T> T withConnection(SQLAction<T> action) {
        try (Connection conn = openConnection()) {
            return action.execute(conn);
        } catch (Exception e) {
            throw new RuntimeException("Database operation failed", e);
        }
    }

    public <T> T transaction(SQLAction<T> action) {
        try (Connection conn = openConnection()) {
            conn.setAutoCommit(false);

            try {
                T result = action.execute(conn);
                conn.commit();
                return result;

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }

        } catch (Exception e) {
            throw new RuntimeException("Database operation failed", e);
        }
    }

    private Connection openConnection() throws Exception {
        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + path);

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
        }

        return conn;
    }

    public void initSchema() {
        String sql = loadSql();

        withConnection(conn -> {
            try (Statement stmt = conn.createStatement()) {

                for (String raw : sql.split(";")) {
                    String statement = raw.trim();
                    if (statement.isEmpty()) continue;

                    stmt.execute(statement);
                }
            }

            return null;
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