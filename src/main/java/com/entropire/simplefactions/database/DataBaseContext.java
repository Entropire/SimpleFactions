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
    private final Connection connection;

    public DataBaseContext(String path) {
        this.path = path;

        this.connection = createConnection();
        initSchema();
    }

    public Connection getConnection() {
        return connection;
    }

    private Connection createConnection() {
        try {
            return DriverManager.getConnection("jdbc:sqlite:" + path);
        } catch (SQLException e) {
            SimpleFactions.getPluginLogger().severe("Failed to connect to database: " + e.getMessage());
            throw new IllegalStateException("Database connection failed", e);
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            SimpleFactions.getPluginLogger().severe("Failed to close database: " + e.getMessage());
        }
    }

    public void initSchema() {
        String sql = loadSql();

        try (Statement stmt = connection.createStatement()) {

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