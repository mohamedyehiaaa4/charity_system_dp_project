package com.fooddonation;

import com.fooddonation.model.donation.DonationFactory;
import com.fooddonation.persistence.DatabaseConnection;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

/** Checks that JUnit, Mockito and SQLite all work. Delete once real tests exist. */
class SetupCheckTest {

    @Test
    void mockitoCanMockSharedClasses() {
        assertNotNull(mock(DonationFactory.class));
        assertNotNull(mock(DatabaseConnection.class));
    }

    @Test
    void sqliteWorks() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:");
             ResultSet rs = connection.createStatement().executeQuery("SELECT 1 + 1")) {
            assertEquals(2, rs.getInt(1));
        }
    }
}
