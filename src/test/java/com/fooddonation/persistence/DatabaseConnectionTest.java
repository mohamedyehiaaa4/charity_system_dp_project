package com.fooddonation.persistence;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

class DatabaseConnectionTest {

    @TempDir
    Path tempDir;

    private DatabaseConnection db;

    // a separate connection to a temporary file, so the tests never wipe the real fooddonation.db
    @BeforeEach
    void setUp() {
        db = new DatabaseConnection(tempDir.resolve("test.db").toString());
        db.reset();
    }

    @AfterEach
    void tearDown() {
        db.close();
    }

    @Test
    void getInstanceAlwaysReturnsSameObject() {
        assertSame(DatabaseConnection.getInstance(), DatabaseConnection.getInstance());
    }

    @Test
    void initializeAddsSeedData() throws SQLException {
        assertEquals(5, count("donation_statuses"));
        assertEquals(1, count("users"));
        assertEquals(4, count("food_categories"));
        assertEquals(3, count("storage_types"));
    }

    @Test
    void initializeTwiceAddsNoDuplicates() throws SQLException {
        db.initialize();
        assertEquals(5, count("donation_statuses"));
        assertEquals(1, count("users"));
    }

    @Test
    void foreignKeysAreOn() throws SQLException {
        try (Statement st = db.getConnection().createStatement();
             ResultSet rs = st.executeQuery("PRAGMA foreign_keys")) {
            assertEquals(1, rs.getInt(1));
        }
    }

    @Test
    void resetRemovesExtraRows() throws SQLException {
        try (Statement st = db.getConnection().createStatement()) {
            st.execute("INSERT INTO food_categories (name) VALUES ('Extra')");
        }
        assertEquals(5, count("food_categories"));

        db.reset();

        assertEquals(4, count("food_categories"));
    }

    @Test
    void getConnectionReopensAfterClose() throws SQLException {
        db.close();
        assertFalse(db.getConnection().isClosed());
    }

    @Test
    void initializeAddsNewColumnsToAnOldDatabase() throws SQLException {
        Path oldFile = tempDir.resolve("old.db");
        try (Connection old = DriverManager.getConnection("jdbc:sqlite:" + oldFile);
             Statement st = old.createStatement()) {
            // the donations table as it was before food_name and pickup_address were added
            st.execute("CREATE TABLE donations (id INTEGER PRIMARY KEY AUTOINCREMENT, donor_id INTEGER NOT NULL, "
                    + "type TEXT NOT NULL, status_id INTEGER NOT NULL, created_at TEXT NOT NULL, "
                    + "rejection_reason TEXT, expiry TEXT, quantity REAL, unit TEXT, category_id INTEGER, "
                    + "storage_type_id INTEGER, pickup_start TEXT, pickup_end TEXT, amount TEXT, add_ons TEXT)");
        }
        DatabaseConnection oldDb = new DatabaseConnection(oldFile.toString());

        oldDb.initialize();

        try (Statement st = oldDb.getConnection().createStatement();
             ResultSet rs = st.executeQuery("SELECT food_name, pickup_address FROM donations")) {
            assertFalse(rs.next());
        } finally {
            oldDb.close();
        }
    }

    private int count(String table) throws SQLException {
        try (Statement st = db.getConnection().createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + table)) {
            return rs.getInt(1);
        }
    }
}
