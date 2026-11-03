package com.fooddonation.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class DatabaseConnection {
    private static DatabaseConnection instance;

    private Connection connection;
    private final String dbPath;

    private DatabaseConnection() {
        this(loadDbPath());
    }

    /** For tests only: a separate connection to another file, so tests never touch the real database. */
    DatabaseConnection(String dbPath) {
        this.dbPath = dbPath;
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
        addMissingColumns();
        // starting rows only go into a brand-new database, so a lookup the coordinator deletes stays deleted
        if (isNewDatabase()) {
            runSqlFile("/seed.sql");
        }
    }

    /** No users yet means the database was just created; every used database has at least the coordinator. */
    private boolean isNewDatabase() {
        try (Statement st = getConnection().createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM users")) {
            return rs.getInt(1) == 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Could not check the users table", e);
        }
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

    /**
     * CREATE TABLE IF NOT EXISTS leaves an older database file as it was, so columns added
     * to schema.sql later are added here instead of making everyone delete their database.
     */
    private void addMissingColumns() {
        Set<String> columns = new HashSet<>();
        try (Statement st = getConnection().createStatement()) {
            try (ResultSet rs = st.executeQuery("PRAGMA table_info(donations)")) {
                while (rs.next()) {
                    columns.add(rs.getString("name"));
                }
            }
            for (String column : List.of("food_name", "pickup_address")) {
                if (!columns.contains(column)) {
                    st.execute("ALTER TABLE donations ADD COLUMN " + column + " TEXT");
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not update the donations table", e);
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
