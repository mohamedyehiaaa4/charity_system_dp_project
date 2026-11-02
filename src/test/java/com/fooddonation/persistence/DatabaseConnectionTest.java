package com.fooddonation.persistence;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

class DatabaseConnectionTest {

    private DatabaseConnection db;

    @BeforeEach
    void setUp() {
        db = DatabaseConnection.getInstance();
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

    private int count(String table) throws SQLException {
        try (Statement st = db.getConnection().createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + table)) {
            return rs.getInt(1);
        }
    }
}
