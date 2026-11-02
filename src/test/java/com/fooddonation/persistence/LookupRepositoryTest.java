package com.fooddonation.persistence;

import com.fooddonation.model.lookup.DonationStatus;
import com.fooddonation.model.lookup.FoodCategory;
import com.fooddonation.model.lookup.LookupEntry;
import com.fooddonation.model.lookup.LookupType;
import com.fooddonation.model.lookup.StorageType;
import com.fooddonation.model.user.Coordinator;
import com.fooddonation.model.user.Donor;
import com.fooddonation.model.user.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.nio.file.Path;
import java.sql.PreparedStatement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LookupRepositoryTest {
    @TempDir
    Path tempDir;

    private DatabaseConnection db;
    private LookupRepository lookups;
    private final User coordinator = new Coordinator();
    private final User donor = new Donor();

    @BeforeEach
    void setUp() throws Exception {
        db = TestDatabase.open(tempDir.resolve("test.db"));
        lookups = new LookupRepository(db);
    }

    @AfterEach
    void tearDown() throws Exception {
        db.getConnection().close();
    }

    private long id(LookupType type, String name) {
        return lookups.findByName(type, name).orElseThrow().getId();
    }

    private void insertDonation(long categoryId, long storageTypeId, long statusId) throws Exception {
        String sql = "INSERT INTO donations (donor_id, type, status_id, created_at, category_id, storage_type_id) "
                + "VALUES (1, 'COOKED', ?, '2026-10-29T14:30', ?, ?)";
        try (PreparedStatement statement = db.getConnection().prepareStatement(sql)) {
            statement.setLong(1, statusId);
            statement.setLong(2, categoryId);
            statement.setLong(3, storageTypeId);
            statement.executeUpdate();
        }
    }

    @ParameterizedTest
    @EnumSource(LookupType.class)
    void addViewEditAndDeleteWorkForEveryType(LookupType type) {
        LookupEntry created = lookups.create(coordinator, type, "  Custom  ");
        assertEquals("Custom", created.getName());
        assertFalse(created.isSystem());
        assertTrue(lookups.readAll(type).stream().anyMatch(entry -> entry.getName().equals("Custom")));

        lookups.update(coordinator, type, created.getId(), "Renamed");
        assertEquals(created.getId(), id(type, "Renamed"));

        lookups.delete(coordinator, type, created.getId());
        assertTrue(lookups.findByName(type, "Renamed").isEmpty());
    }

    @Test
    void entriesComeBackAsTheirRealClass() {
        assertInstanceOf(FoodCategory.class, lookups.findByName(LookupType.FOOD_CATEGORY, "Bakery").orElseThrow());
        assertInstanceOf(StorageType.class, lookups.findByName(LookupType.STORAGE_TYPE, "Frozen").orElseThrow());
        assertInstanceOf(DonationStatus.class,
                lookups.findByName(LookupType.DONATION_STATUS, DonationStatus.SUBMITTED).orElseThrow());
    }

    @Test
    void sameIdInDifferentTablesDoesNotClash() {
        long id = id(LookupType.FOOD_CATEGORY, "Bakery");
        assertEquals(id, id(LookupType.DONATION_STATUS, DonationStatus.SUBMITTED));

        lookups.update(coordinator, LookupType.FOOD_CATEGORY, id, "Bread");

        assertTrue(lookups.findByName(LookupType.FOOD_CATEGORY, "Bread").isPresent());
        assertTrue(lookups.findByName(LookupType.DONATION_STATUS, DonationStatus.SUBMITTED).isPresent());
    }

    @Test
    void blankNamesAreRejected() {
        long dairyId = id(LookupType.FOOD_CATEGORY, "Dairy");

        assertThrows(IllegalArgumentException.class,
                () -> lookups.create(coordinator, LookupType.FOOD_CATEGORY, "   "));
        assertThrows(IllegalArgumentException.class,
                () -> lookups.update(coordinator, LookupType.FOOD_CATEGORY, dairyId, null));
    }

    @Test
    void duplicateNamesAreRejectedWhateverTheCase() {
        long dairyId = id(LookupType.FOOD_CATEGORY, "Dairy");

        assertThrows(IllegalArgumentException.class,
                () -> lookups.create(coordinator, LookupType.FOOD_CATEGORY, "bakery"));
        assertThrows(IllegalArgumentException.class,
                () -> lookups.update(coordinator, LookupType.FOOD_CATEGORY, dairyId, "Bakery"));
    }

    @Test
    void anEntryCanChangeTheCaseOfItsOwnName() {
        long id = id(LookupType.FOOD_CATEGORY, "Bakery");

        lookups.update(coordinator, LookupType.FOOD_CATEGORY, id, "BAKERY");

        assertEquals("BAKERY", lookups.findByName(LookupType.FOOD_CATEGORY, "bakery").orElseThrow().getName());
    }

    @Test
    void systemStatusesCantBeRenamedOrDeleted() {
        String[] names = {DonationStatus.SUBMITTED, DonationStatus.APPROVED, DonationStatus.REJECTED,
                DonationStatus.PICKED_UP, DonationStatus.DELIVERED};

        for (String name : names) {
            long id = id(LookupType.DONATION_STATUS, name);

            assertThrows(IllegalArgumentException.class,
                    () -> lookups.update(coordinator, LookupType.DONATION_STATUS, id, "New name"));
            assertThrows(IllegalArgumentException.class,
                    () -> lookups.delete(coordinator, LookupType.DONATION_STATUS, id));
        }
        assertEquals(5, lookups.readAll(LookupType.DONATION_STATUS).size());
    }

    @Test
    void lookupsUsedByADonationCantBeDeleted() throws Exception {
        long categoryId = id(LookupType.FOOD_CATEGORY, "Meals");
        long storageTypeId = id(LookupType.STORAGE_TYPE, "Frozen");
        long statusId = lookups.create(coordinator, LookupType.DONATION_STATUS, "On hold").getId();
        insertDonation(categoryId, storageTypeId, statusId);

        assertThrows(IllegalArgumentException.class,
                () -> lookups.delete(coordinator, LookupType.FOOD_CATEGORY, categoryId));
        assertThrows(IllegalArgumentException.class,
                () -> lookups.delete(coordinator, LookupType.STORAGE_TYPE, storageTypeId));
        assertThrows(IllegalArgumentException.class,
                () -> lookups.delete(coordinator, LookupType.DONATION_STATUS, statusId));
    }

    @Test
    void missingEntriesAreReported() {
        assertThrows(IllegalArgumentException.class,
                () -> lookups.update(coordinator, LookupType.FOOD_CATEGORY, 9999, "Name"));
        assertThrows(IllegalArgumentException.class,
                () -> lookups.delete(coordinator, LookupType.FOOD_CATEGORY, 9999));
    }

    @Test
    void donorsAreRefused() {
        assertThrows(SecurityException.class, () -> lookups.create(donor, LookupType.FOOD_CATEGORY, "Fish"));
        assertThrows(SecurityException.class, () -> lookups.update(donor, LookupType.FOOD_CATEGORY, 1, "Fish"));
        assertThrows(SecurityException.class, () -> lookups.delete(donor, LookupType.FOOD_CATEGORY, 1));
        assertThrows(SecurityException.class, () -> lookups.create(null, LookupType.FOOD_CATEGORY, "Fish"));
    }

    @Test
    void anyoneCanRead() {
        assertEquals(4, lookups.readAll(LookupType.FOOD_CATEGORY).size());
    }
}
