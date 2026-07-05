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
    
    @FunctionalInterface
    public interface SQLAction {
        void execute(Connection conn) throws Exception;
    }

    private void withConnection(SQLAction action) {
        try (Connection conn = openConnection()) {
            action.execute(conn);
        } catch (Exception e) {
            throw new RuntimeException("Database operation failed", e);
        }
    }

    public void transaction(SQLAction action) {
        withConnection(conn -> {
            conn.setAutoCommit(false);

            try {
                action.execute(conn);
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        });
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