package com.fooddonation.service;

import com.fooddonation.model.user.Coordinator;
import com.fooddonation.model.user.Donor;
import com.fooddonation.model.user.DonorType;
import com.fooddonation.model.user.Role;
import com.fooddonation.model.user.User;
import com.fooddonation.notification.ChannelType;
import com.fooddonation.persistence.DatabaseConnection;
import com.fooddonation.persistence.TestDatabase;
import com.fooddonation.persistence.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthServiceTest {
    @TempDir
    Path tempDir;

    private DatabaseConnection db;
    private UserRepository users;
    private AuthService auth;

    @BeforeEach
    void setUp() throws Exception {
        db = TestDatabase.open(tempDir.resolve("test.db"));
        users = new UserRepository(db);
        auth = new AuthService(users);
    }

    @AfterEach
    void tearDown() throws Exception {
        db.getConnection().close();
    }

    private Donor registerSara() {
        return auth.register("Sara", "sara@mail.com", "0100", "Cairo", DonorType.RESTAURANT, ChannelType.SMS);
    }

    @Test
    void registerSavesAllFields() {
        Donor donor = registerSara();

        Donor loaded = (Donor) users.findById(donor.getId()).orElseThrow();
        assertEquals("Sara", loaded.getName());
        assertEquals("0100", loaded.getPhone());
        assertEquals("sara@mail.com", loaded.getEmail());
        assertEquals("Cairo", loaded.getAddress());
        assertEquals(DonorType.RESTAURANT, loaded.getDonorType());
        assertEquals(ChannelType.SMS, loaded.getPreferredChannel());
    }

    @Test
    void registerAlwaysCreatesADonor() {
        assertEquals(Role.DONOR, registerSara().getRole());
    }

    @Test
    void duplicateEmailIsRefusedWhateverTheCase() {
        registerSara();

        assertThrows(IllegalArgumentException.class, () -> auth.register(
                "Other", "SARA@mail.com", "0111", "Giza", DonorType.INDIVIDUAL, ChannelType.EMAIL));
    }

    @Test
    void registerRefusesMissingOrBadFields() {
        assertThrows(IllegalArgumentException.class, () -> auth.register(
                " ", "a@b.com", "0100", "Cairo", DonorType.INDIVIDUAL, ChannelType.EMAIL));
        assertThrows(IllegalArgumentException.class, () -> auth.register(
                "Ali", "not-an-email", "0100", "Cairo", DonorType.INDIVIDUAL, ChannelType.EMAIL));
        assertThrows(IllegalArgumentException.class, () -> auth.register(
                "Ali", "a@b.com", "0100", "Cairo", null, ChannelType.EMAIL));
        assertThrows(IllegalArgumentException.class, () -> auth.register(
                "Ali", "a@b.com", "0100", "Cairo", DonorType.INDIVIDUAL, null));
    }

    @Test
    void loginWorksForDonorsAndTheSeededCoordinator() {
        registerSara();

        assertInstanceOf(Donor.class, auth.login("SARA@mail.com ").orElseThrow());
        assertInstanceOf(Coordinator.class, auth.login("coordinator@fooddonation.org").orElseThrow());
    }

    @Test
    void loginWithUnknownEmailIsEmpty() {
        assertTrue(auth.login("nobody@mail.com").isEmpty());
        assertTrue(auth.login(null).isEmpty());
    }

    @Test
    void updateProfileKeepsIdAndChannel() {
        Donor donor = registerSara();
        donor.setAddress("Alexandria");

        auth.updateProfile(donor);

        User loaded = users.findById(donor.getId()).orElseThrow();
        assertEquals("Alexandria", loaded.getAddress());
        assertEquals(ChannelType.SMS, loaded.getPreferredChannel());
    }

    @Test
    void updateProfileWithoutChannelKeepsTheStoredOne() {
        Donor donor = registerSara();
        donor.setPreferredChannel(null);

        auth.updateProfile(donor);

        assertEquals(ChannelType.SMS, users.findById(donor.getId()).orElseThrow().getPreferredChannel());
    }

    @Test
    void updateProfileCanChangeChannel() {
        Donor donor = registerSara();
        donor.setPreferredChannel(ChannelType.EMAIL);

        auth.updateProfile(donor);

        assertEquals(ChannelType.EMAIL, users.findById(donor.getId()).orElseThrow().getPreferredChannel());
    }

    @Test
    void updateProfileCantTakeAnotherUsersEmail() {
        Donor donor = registerSara();
        donor.setEmail("coordinator@fooddonation.org");

        assertThrows(IllegalArgumentException.class, () -> auth.updateProfile(donor));
    }

    @Test
    void coordinatorCanUpdateProfileWithoutPhoneOrAddress() {
        User coordinator = auth.login("coordinator@fooddonation.org").orElseThrow();
        coordinator.setName("Main Coordinator");

        auth.updateProfile(coordinator);

        assertEquals("Main Coordinator", users.findById(coordinator.getId()).orElseThrow().getName());
    }
}
