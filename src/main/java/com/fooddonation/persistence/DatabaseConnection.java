package com.fooddonation.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DatabaseConnection {
    private static DatabaseConnection instance;

    private Connection connection;
    private final String dbPath;

    private DatabaseConnection() {
        dbPath = loadDbPath();
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
                try (Statement st = connection.createStatement()) {
                    st.execute("PRAGMA foreign_keys = ON");
                }
            }
            return connection;
        } catch (SQLException e) {
            throw new IllegalStateException("Could not open database " + dbPath, e);
        }
    }

    /** Runs schema.sql and seed.sql. */
    public synchronized void initialize() {
        runSqlFile("/schema.sql");
        runSqlFile("/seed.sql");
    }

    public synchronized void reset() {
        try (Statement st = getConnection().createStatement()) {
            // donations first, because it points to the other tables
            st.execute("DROP TABLE IF EXISTS donations");
            st.execute("DROP TABLE IF EXISTS users");
            st.execute("DROP TABLE IF EXISTS food_categories");
            st.execute("DROP TABLE IF EXISTS storage_types");
            st.execute("DROP TABLE IF EXISTS donation_statuses");
        } catch (SQLException e) {
            throw new IllegalStateException("Could not reset database", e);
        }
        initialize();
    }

    public synchronized void close() {
        if (connection == null) {
            return;
        }
        try {
            connection.close();
        } catch (SQLException e) {
            throw new IllegalStateException("Could not close database", e);
        } finally {
            connection = null;
        }
    }

    private void runSqlFile(String path) {
        try (InputStream in = getClass().getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException("Missing file " + path);
            }
            String sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            try (Statement st = getConnection().createStatement()) {
                for (String command : sql.split(";")) {
                    if (!command.isBlank()) {
                        st.execute(command);
                    }
                }
            }
        } catch (IOException | SQLException e) {
            throw new IllegalStateException("Could not run " + path, e);
        }
    }

    private static String loadDbPath() {
        Properties props = new Properties();
        try (InputStream in = DatabaseConnection.class.getResourceAsStream("/app.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not read app.properties", e);
        }
        return props.getProperty("db.path", "fooddonation.db");
    }
}
