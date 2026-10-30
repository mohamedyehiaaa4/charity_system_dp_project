package com.fooddonation.persistence;

import com.fooddonation.addon.AddOn;
import com.fooddonation.addon.DonationDecorator;
import com.fooddonation.model.donation.Donation;
import com.fooddonation.model.donation.DonationFactory;
import com.fooddonation.model.donation.DonationRequest;
import com.fooddonation.model.donation.DonationType;
import com.fooddonation.model.donation.FoodDonation;
import com.fooddonation.model.donation.MoneyDonation;
import com.fooddonation.model.donation.Unit;
import com.fooddonation.model.lookup.DonationStatus;
import com.fooddonation.model.lookup.FoodCategory;
import com.fooddonation.model.lookup.StorageType;
import com.fooddonation.model.user.Donor;
import com.fooddonation.model.user.DonorType;
import com.fooddonation.notification.ChannelType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DonationRepositoryTest {
    private static final LocalDateTime CREATED = LocalDateTime.of(2026, 10, 29, 14, 30);
    private static final LocalDateTime PICKUP_START = LocalDateTime.of(2026, 10, 30, 10, 0);
    private static final LocalDateTime PICKUP_END = LocalDateTime.of(2026, 10, 30, 12, 0);

    private DatabaseConnection db;
    private DonationFactory factory;
    private Donation restored;
    private MockedStatic<DonationDecorator> decorators;
    private DonationRepository repository;

    private Donor ahmed;
    private DonationStatus submitted;
    private DonationStatus approved;
    private DonationStatus rejected;

    @BeforeEach
    void setUp() throws SQLException {
        db = DatabaseConnection.getInstance();
        db.reset();

        factory = mock(DonationFactory.class);
        restored = mock(Donation.class);
        when(factory.restore(any(), anyLong(), any(), any(), any(), any())).thenReturn(restored);

        // DonationDecorator isn't written yet: treat every donation as having no add-on wrappers
        decorators = mockStatic(DonationDecorator.class);
        decorators.when(() -> DonationDecorator.unwrap(any())).thenAnswer(inv -> inv.getArgument(0));
        decorators.when(() -> DonationDecorator.collectAddOns(any())).thenReturn(Set.of());

        repository = new DonationRepository(db, factory);

        ahmed = insertDonor("Ahmed", "ahmed@example.com");
        submitted = status(DonationStatus.SUBMITTED);
        approved = status(DonationStatus.APPROVED);
        rejected = status(DonationStatus.REJECTED);
    }

    @AfterEach
    void tearDown() {
        decorators.close();
        db.close();
    }

    @Test
    void saveGivesTheDonationANewId() {
        Donation saved = repository.save(food(ahmed));

        assertTrue(saved.getId() > 0);
    }

    @Test
    void savedFoodDonationLoadsBackWithAllFields() throws SQLException {
        long id = repository.save(food(ahmed)).getId();

        Donation found = repository.findById(id).orElseThrow();

        assertSame(restored, found);
        ArgumentCaptor<DonationRequest> data = ArgumentCaptor.forClass(DonationRequest.class);
        ArgumentCaptor<DonationStatus> status = ArgumentCaptor.forClass(DonationStatus.class);
        verify(factory).restore(data.capture(), eq(id), status.capture(), eq(CREATED), isNull(), eq(Set.of()));

        DonationRequest d = data.getValue();
        assertEquals(DonationType.COOKED, d.getType());
        assertEquals(ahmed.getId(), d.getDonor().getId());
        assertEquals("Ahmed", d.getDonor().getName());
        assertEquals(LocalDate.of(2026, 11, 1), d.getExpiry());
        assertEquals(Double.valueOf(5.0), d.getQuantity());
        assertEquals(Unit.KG, d.getUnit());
        assertEquals("Bakery", d.getCategory().getName());
        assertEquals("Room temperature", d.getStorageType().getName());
        assertEquals(PICKUP_START, d.getPickupStart());
        assertEquals(PICKUP_END, d.getPickupEnd());
        assertNull(d.getAmount());
        assertEquals(DonationStatus.SUBMITTED, status.getValue().getName());
    }

    @Test
    void moneyDonationSavesAmountAndNoFoodFields() {
        MoneyDonation money = mock(MoneyDonation.class);
        when(money.getType()).thenReturn(DonationType.MONEY);
        when(money.getDonor()).thenReturn(ahmed);
        when(money.getStatus()).thenReturn(submitted);
        when(money.getCreatedAt()).thenReturn(CREATED);
        when(money.getAmount()).thenReturn(new BigDecimal("250.50"));
        repository.save(money);

        repository.findAll();

        ArgumentCaptor<DonationRequest> data = ArgumentCaptor.forClass(DonationRequest.class);
        verify(factory).restore(data.capture(), anyLong(), any(), any(), any(), any());
        assertEquals(new BigDecimal("250.50"), data.getValue().getAmount());
        assertNull(data.getValue().getQuantity());
        assertNull(data.getValue().getUnit());
        assertNull(data.getValue().getExpiry());
    }

    @Test
    void addOnsAreSavedInEnumOrderAndLoadedBack() throws SQLException {
        Donation donation = food(ahmed);
        decorators.when(() -> DonationDecorator.collectAddOns(donation))
                .thenReturn(Set.of(AddOn.URGENT_PRIORITY, AddOn.HOME_PICKUP));
        long id = repository.save(donation).getId();

        assertEquals("HOME_PICKUP,URGENT_PRIORITY", addOnsColumn(id));

        repository.findById(id);
        verify(factory).restore(any(), eq(id), any(), any(), any(),
                eq(Set.of(AddOn.HOME_PICKUP, AddOn.URGENT_PRIORITY)));
    }

    @Test
    void findByIdReturnsEmptyForUnknownId() {
        assertTrue(repository.findById(999).isEmpty());
    }

    @Test
    void eachDonorOnlySeesTheirOwnDonations() throws SQLException {
        Donor mona = insertDonor("Mona", "mona@example.com");
        repository.save(food(ahmed));
        repository.save(food(ahmed));
        repository.save(food(mona));

        assertEquals(2, repository.findByDonor(ahmed.getId()).size());
        assertEquals(1, repository.findByDonor(mona.getId()).size());
        assertEquals(3, repository.findAll().size());
    }

    @Test
    void findByStatusReturnsOnlyThatStatus() {
        long first = repository.save(food(ahmed)).getId();
        repository.save(food(ahmed));

        repository.updateStatus(first, approved, null);

        assertEquals(1, repository.findByStatus(approved).size());
        assertEquals(1, repository.findByStatus(submitted).size());
    }

    @Test
    void updateStatusSavesTheRejectionReason() {
        long id = repository.save(food(ahmed)).getId();

        repository.updateStatus(id, rejected, "Food was spoiled");

        repository.findById(id);
        ArgumentCaptor<DonationStatus> status = ArgumentCaptor.forClass(DonationStatus.class);
        verify(factory).restore(any(), eq(id), status.capture(), any(), eq("Food was spoiled"), any());
        assertEquals(DonationStatus.REJECTED, status.getValue().getName());
    }

    @Test
    void updateStatusOfUnknownDonationFails() {
        assertThrows(IllegalArgumentException.class, () -> repository.updateStatus(999, approved, null));
    }

    @Test
    void failedSaveLeavesNothingInTheDatabase() {
        Donor ghost = new Donor("Ghost", "ghost@example.com", null, null, DonorType.INDIVIDUAL, ChannelType.EMAIL);
        ghost.setId(999);

        assertThrows(IllegalStateException.class, () -> repository.save(food(ghost)));
        assertTrue(repository.findAll().isEmpty());
    }

    private Donation food(Donor donor) {
        FakeFoodDonation donation = new FakeFoodDonation(donor, LocalDate.of(2026, 11, 1), 5.0, Unit.KG,
                new FoodCategory(lookupId("food_categories", "Bakery"), "Bakery", false),
                new StorageType(lookupId("storage_types", "Room temperature"), "Room temperature", false),
                PICKUP_START, PICKUP_END);
        donation.setStatus(submitted);
        donation.setCreatedAt(CREATED);
        return donation;
    }

    private Donor insertDonor(String name, String email) throws SQLException {
        try (PreparedStatement st = db.getConnection().prepareStatement(
                "INSERT INTO users (name, email, role, donor_type, preferred_channel) "
                        + "VALUES (?, ?, 'DONOR', 'INDIVIDUAL', 'EMAIL')", Statement.RETURN_GENERATED_KEYS)) {
            st.setString(1, name);
            st.setString(2, email);
            st.executeUpdate();
            try (ResultSet keys = st.getGeneratedKeys()) {
                keys.next();
                Donor donor = new Donor(name, email, null, null, DonorType.INDIVIDUAL, ChannelType.EMAIL);
                donor.setId(keys.getLong(1));
                return donor;
            }
        }
    }

    private DonationStatus status(String name) throws SQLException {
        return new DonationStatus(lookupId("donation_statuses", name), name, true);
    }

    private long lookupId(String table, String name) {
        try (PreparedStatement st = db.getConnection().prepareStatement(
                "SELECT id FROM " + table + " WHERE name = ?")) {
            st.setString(1, name);
            try (ResultSet rs = st.executeQuery()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private String addOnsColumn(long id) throws SQLException {
        try (PreparedStatement st = db.getConnection().prepareStatement(
                "SELECT add_ons FROM donations WHERE id = ?")) {
            st.setLong(1, id);
            try (ResultSet rs = st.executeQuery()) {
                return rs.getString(1);
            }
        }
    }

    /** Stand-in for a real food donation, since only DonationFactory can create those. */
    private static class FakeFoodDonation extends FoodDonation {
        FakeFoodDonation(Donor donor, LocalDate expiry, double quantity, Unit unit, FoodCategory category,
                         StorageType storageType, LocalDateTime pickupStart, LocalDateTime pickupEnd) {
            super(donor, DonationType.COOKED, expiry, quantity, unit, category, storageType, pickupStart, pickupEnd);
        }

        @Override
        public String getDescription() {
            return "Fake food";
        }

        @Override
        public int getPriority() {
            return 0;
        }
    }
}
