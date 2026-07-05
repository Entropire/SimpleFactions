package com.entropire.simplefactions.database;

import com.entropire.simplefactions.SimpleFactions;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class DataBaseContext {

    private final String path;

    public DataBaseContext(String path) {
        this.path = path;
        initSchema();
    }
    
    @FunctionalInterface
    public interface Transaction {
        void execute(Connection conn) throws Exception;
    }

    public void transaction(Transaction transaction) throws Exception {
        try (Connection conn = getConnection()) {

            conn.setAutoCommit(false);

            try {
                transaction.execute(conn);
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public Connection getConnection() {
        try {
            return DriverManager.getConnection("jdbc:sqlite:" + path);
        } catch (SQLException e) {
            SimpleFactions.getPluginLogger().severe("Failed to connect to database: " + e.getMessage());
            throw new IllegalStateException("Database connection failed", e);
        }
    }

    public void initSchema() {
        String sql = loadSql();

        try (Statement stmt = getConnection().createStatement()) {

            for (String raw : sql.split(";")) {
                String statement = raw.trim();
                if (statement.isEmpty()) continue;

                stmt.execute(statement);
            }

        } catch (SQLException e) {
            SimpleFactions.getPluginLogger().severe("Failed to initialize schema: " + e.getMessage());
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