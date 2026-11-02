package com.fooddonation.service;

import com.fooddonation.addon.AddOn;
import com.fooddonation.model.donation.Donation;
import com.fooddonation.model.donation.DonationFactory;
import com.fooddonation.model.donation.DonationRequest;
import com.fooddonation.model.donation.DonationType;
import com.fooddonation.model.donation.Unit;
import com.fooddonation.model.lookup.DonationStatus;
import com.fooddonation.model.lookup.FoodCategory;
import com.fooddonation.model.lookup.StorageType;
import com.fooddonation.model.receipt.AnnualDonationSummary;
import com.fooddonation.model.receipt.DonationReceipt;
import com.fooddonation.model.user.Coordinator;
import com.fooddonation.model.user.Donor;
import com.fooddonation.model.user.DonorType;
import com.fooddonation.notification.ChannelType;
import com.fooddonation.persistence.DonationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReceiptServiceTest {
    static final ZoneId CAIRO = ZoneId.of("Africa/Cairo");

    DonationFactory factory = new DonationFactory();
    DonationRepository donations;
    ReceiptService service;
    Donor mona;
    Coordinator coordinator = new Coordinator();

    @BeforeEach
    void setUp() {
        donations = mock(DonationRepository.class);
        Clock clock = Clock.fixed(Instant.parse("2026-11-02T10:00:00Z"), CAIRO);
        service = new ReceiptService(donations, clock);
        mona = new Donor("Mona", "mona@example.com", null, "5 Tahrir Sq", DonorType.RESTAURANT, ChannelType.EMAIL);
        mona.setId(7);
        coordinator.setId(1);
    }

    Donation food(long id, String status, LocalDateTime created, Set<AddOn> addOns) {
        DonationRequest data = new DonationRequest();
        data.setType(DonationType.COOKED);
        data.setDonor(mona);
        data.setFoodName("Rice and chicken");
        data.setQuantity(5.0);
        data.setUnit(Unit.KG);
        data.setCategory(new FoodCategory(3, "Meals", false));
        data.setStorageType(new StorageType(1, "Room temperature", false));
        data.setExpiry(LocalDate.of(2026, 11, 5));
        data.setPickupStart(created.plusHours(1));
        data.setPickupEnd(created.plusHours(3));
        data.setPickupAddress("5 Tahrir Sq");
        return factory.restore(data, id, new DonationStatus(1, status, true), created, null, addOns);
    }

    Donation money(long id, String status, LocalDateTime created, String amount) {
        DonationRequest data = new DonationRequest();
        data.setType(DonationType.MONEY);
        data.setDonor(mona);
        data.setAmount(new BigDecimal(amount));
        return factory.restore(data, id, new DonationStatus(1, status, true), created, null, Set.of());
    }

    // --- one receipt ---

    @Test
    void foodReceiptListsWhatWasGiven() {
        Donation donation = food(42, DonationStatus.APPROVED, LocalDateTime.of(2026, 11, 1, 9, 0),
                Set.of(AddOn.HOME_PICKUP));
        when(donations.findById(42)).thenReturn(Optional.of(donation));

        DonationReceipt receipt = service.getReceipt(mona, 42);

        assertEquals("R-2026-000042", receipt.getReceiptNumber());
        assertEquals(42, receipt.getDonationId());
        assertEquals("5 kg of Rice and chicken", receipt.getItem());
        assertNull(receipt.getAmount());
        assertEquals("Cooked food donation + Home pickup", receipt.getDescription());
        assertEquals(DonationStatus.APPROVED, receipt.getStatus());
        assertEquals(LocalDateTime.of(2026, 11, 2, 12, 0), receipt.getIssuedAt());
        String text = receipt.toText();
        assertTrue(text.contains("Donation receipt R-2026-000042"), text);
        assertTrue(text.contains("Donor: Mona (mona@example.com), restaurant"), text);
        assertTrue(text.contains("Item: 5 kg of Rice and chicken"), text);
    }

    @Test
    void moneyReceiptShowsTheAmount() {
        when(donations.findById(3)).thenReturn(Optional.of(
                money(3, DonationStatus.DELIVERED, LocalDateTime.of(2026, 2, 1, 9, 0), "250.50")));

        DonationReceipt receipt = service.getReceipt(coordinator, 3);

        assertEquals(new BigDecimal("250.50"), receipt.getAmount());
        assertNull(receipt.getItem());
        assertTrue(receipt.toText().contains("Amount: 250.50"));
    }

    @Test
    void sameDonationAlwaysGetsTheSameNumber() {
        Donation donation = food(9, DonationStatus.PICKED_UP, LocalDateTime.of(2026, 3, 1, 9, 0), Set.of());

        assertEquals(service.receiptFor(donation).getReceiptNumber(), service.receiptFor(donation).getReceiptNumber());
    }

    @Test
    void submittedOrRejectedDonationsHaveNoReceipt() {
        when(donations.findById(1)).thenReturn(Optional.of(
                food(1, DonationStatus.SUBMITTED, LocalDateTime.of(2026, 11, 1, 9, 0), Set.of())));
        when(donations.findById(2)).thenReturn(Optional.of(
                food(2, DonationStatus.REJECTED, LocalDateTime.of(2026, 11, 1, 9, 0), Set.of())));

        assertThrows(IllegalStateException.class, () -> service.getReceipt(mona, 1));
        assertThrows(IllegalStateException.class, () -> service.getReceipt(mona, 2));
    }

    @Test
    void unknownDonationFails() {
        when(donations.findById(99)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.getReceipt(mona, 99));
    }

    @Test
    void donorCantSeeAnotherDonorsReceipt() {
        Donor other = new Donor();
        other.setId(8);
        when(donations.findById(42)).thenReturn(Optional.of(
                food(42, DonationStatus.APPROVED, LocalDateTime.of(2026, 11, 1, 9, 0), Set.of())));

        assertThrows(SecurityException.class, () -> service.getReceipt(other, 42));
        assertThrows(SecurityException.class, () -> service.getReceipt(null, 42));
    }

    // --- annual summary ---

    @Test
    void annualSummaryCountsOnlyAcceptedDonationsInThatYear() {
        when(donations.findByDonor(7)).thenReturn(List.of(
                money(4, DonationStatus.APPROVED, LocalDateTime.of(2026, 6, 1, 9, 0), "100"),
                money(1, DonationStatus.DELIVERED, LocalDateTime.of(2026, 1, 10, 9, 0), "250.50"),
                food(2, DonationStatus.PICKED_UP, LocalDateTime.of(2026, 3, 5, 9, 0), Set.of()),
                money(3, DonationStatus.REJECTED, LocalDateTime.of(2026, 4, 1, 9, 0), "999"),
                food(5, DonationStatus.SUBMITTED, LocalDateTime.of(2026, 7, 1, 9, 0), Set.of()),
                money(6, DonationStatus.APPROVED, LocalDateTime.of(2025, 12, 31, 23, 0), "50")));

        AnnualDonationSummary summary = service.getAnnualSummary(mona, 7, 2026);

        assertEquals(2026, summary.getYear());
        assertEquals(List.of("R-2026-000001", "R-2026-000002", "R-2026-000004"),
                summary.getReceipts().stream().map(DonationReceipt::getReceiptNumber).toList());
        assertEquals(new BigDecimal("350.50"), summary.getTotalMoney());
        assertEquals(1, summary.getFoodDonationCount());
        String text = summary.toText();
        assertTrue(text.contains("Annual donation summary 2026 (for tax purposes)"), text);
        assertTrue(text.contains("Total money donated: 350.50"), text);
        assertTrue(text.contains("5 kg of Rice and chicken"), text);
    }

    @Test
    void yearWithNothingAcceptedFails() {
        when(donations.findByDonor(7)).thenReturn(List.of(
                money(3, DonationStatus.REJECTED, LocalDateTime.of(2026, 4, 1, 9, 0), "999")));

        assertThrows(IllegalStateException.class, () -> service.getAnnualSummary(mona, 7, 2026));
    }

    @Test
    void coordinatorCanGetAnyDonorsSummaryButDonorsOnlyTheirOwn() {
        when(donations.findByDonor(7)).thenReturn(List.of(
                money(1, DonationStatus.APPROVED, LocalDateTime.of(2026, 1, 10, 9, 0), "10")));
        Donor other = new Donor();
        other.setId(8);

        assertEquals(1, service.getAnnualSummary(coordinator, 7, 2026).getReceipts().size());
        assertThrows(SecurityException.class, () -> service.getAnnualSummary(other, 7, 2026));
    }
}
