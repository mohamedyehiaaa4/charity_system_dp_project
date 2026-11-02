package com.fooddonation.persistence;

import com.fooddonation.model.user.Coordinator;
import com.fooddonation.model.user.Donor;
import com.fooddonation.model.user.DonorType;
import com.fooddonation.model.user.Role;
import com.fooddonation.model.user.User;
import com.fooddonation.notification.ChannelType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserRepositoryTest {
    @TempDir
    Path tempDir;

    private Path file;
    private DatabaseConnection db;
    private UserRepository users;

    @BeforeEach
    void setUp() throws Exception {
        file = tempDir.resolve("test.db");
        db = TestDatabase.open(file);
        users = new UserRepository(db);
    }

    @AfterEach
    void tearDown() throws Exception {
        db.getConnection().close();
    }

    private Donor sampleDonor() {
        return new Donor("Sara", "sara@mail.com", "0100", "Cairo", DonorType.RESTAURANT, ChannelType.SMS);
    }

    @Test
    void saveGivesTheUserAnId() {
        assertTrue(users.save(sampleDonor()).getId() > 0);
    }

    @Test
    void savedDonorLoadsWithAllFields() {
        long id = users.save(sampleDonor()).getId();

        Donor loaded = assertInstanceOf(Donor.class, users.findById(id).orElseThrow());

        assertEquals("Sara", loaded.getName());
        assertEquals("sara@mail.com", loaded.getEmail());
        assertEquals("0100", loaded.getPhone());
        assertEquals("Cairo", loaded.getAddress());
        assertEquals(Role.DONOR, loaded.getRole());
        assertEquals(DonorType.RESTAURANT, loaded.getDonorType());
        assertEquals(ChannelType.SMS, loaded.getPreferredChannel());
    }

    @Test
    void seededCoordinatorLoadsAsCoordinator() {
        User loaded = users.findByEmail("coordinator@fooddonation.org").orElseThrow();

        assertInstanceOf(Coordinator.class, loaded);
        assertEquals(Role.COORDINATOR, loaded.getRole());
    }

    @Test
    void unknownUserIsEmpty() {
        assertTrue(users.findById(9999).isEmpty());
        assertTrue(users.findByEmail("nobody@mail.com").isEmpty());
    }

    @Test
    void updateChangesProfileButNotRole() {
        Donor donor = (Donor) users.save(sampleDonor());
        donor.setName("Sara Ali");
        donor.setPreferredChannel(ChannelType.EMAIL);
        donor.setRole(Role.COORDINATOR);

        users.update(donor);

        User loaded = users.findById(donor.getId()).orElseThrow();
        assertEquals("Sara Ali", loaded.getName());
        assertEquals(ChannelType.EMAIL, loaded.getPreferredChannel());
        assertEquals(Role.DONOR, loaded.getRole());
    }

    @Test
    void dataIsStillThereAfterReopeningTheDatabase() throws Exception {
        long id = users.save(sampleDonor()).getId();
        db.getConnection().close();

        db = TestDatabase.open(file);
        users = new UserRepository(db);

        assertEquals("Sara", users.findById(id).orElseThrow().getName());
    }
}
