package com.fooddonation.model.donation;

import com.fooddonation.addon.AddOn;
import com.fooddonation.addon.DonationDecorator;
import com.fooddonation.addon.RefrigeratedTransport;
import com.fooddonation.addon.SpecialPackaging;
import com.fooddonation.addon.UrgentPriority;
import com.fooddonation.model.lookup.DonationStatus;
import com.fooddonation.model.lookup.FoodCategory;
import com.fooddonation.model.lookup.StorageType;
import com.fooddonation.model.user.Donor;
import com.fooddonation.model.user.DonorType;
import com.fooddonation.notification.ChannelType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DonationFactoryTest {
    static final LocalDate EXPIRY = LocalDate.of(2026, 11, 5);
    static final LocalDateTime PICKUP_START = LocalDateTime.of(2026, 11, 3, 10, 0);
    static final LocalDateTime PICKUP_END = LocalDateTime.of(2026, 11, 3, 12, 0);

    DonationFactory factory = new DonationFactory();
    Donor donor;
    FoodCategory meals = new FoodCategory(3, "Meals", false);
    StorageType frozen = new StorageType(3, "Frozen", false);

    @BeforeEach
    void setUp() {
        donor = new Donor("Mona", "mona@example.com", "0100", "5 Tahrir Sq, Cairo",
                DonorType.INDIVIDUAL, ChannelType.EMAIL);
        donor.setId(7);
    }

    DonationRequest food(DonationType type) {
        DonationRequest request = new DonationRequest();
        request.setType(type);
        request.setDonor(donor);
        request.setFoodName("Rice and chicken");
        request.setQuantity(5.0);
        request.setUnit(Unit.KG);
        request.setCategory(meals);
        request.setStorageType(frozen);
        request.setExpiry(EXPIRY);
        request.setPickupStart(PICKUP_START);
        request.setPickupEnd(PICKUP_END);
        request.setPickupAddress("12 Nile St, Cairo");
        return request;
    }

    DonationRequest money(String amount) {
        DonationRequest request = new DonationRequest();
        request.setType(DonationType.MONEY);
        request.setDonor(donor);
        request.setAmount(amount == null ? null : new BigDecimal(amount));
        return request;
    }

    DonationRequest food(Consumer<DonationRequest> change) {
        DonationRequest request = food(DonationType.COOKED);
        change.accept(request);
        return request;
    }

    // --- creating each type ---

    @Test
    void cookedRequestGivesCookedFood() {
        Donation donation = factory.createDonation(food(DonationType.COOKED));

        assertInstanceOf(CookedFoodDonation.class, donation);
        assertEquals("Cooked food donation", donation.getDescription());
        assertEquals(8, donation.getPriority());
    }

    @Test
    void refrigeratedRequestGivesRefrigeratedFood() {
        Donation donation = factory.createDonation(food(DonationType.REFRIGERATED));

        assertInstanceOf(RefrigeratedFoodDonation.class, donation);
        assertEquals("Refrigerated food donation", donation.getDescription());
        assertEquals(7, donation.getPriority());
    }

    @Test
    void freshRequestGivesFreshProduce() {
        Donation donation = factory.createDonation(food(DonationType.FRESH));

        assertInstanceOf(FreshProduceDonation.class, donation);
        assertEquals("Fresh produce donation", donation.getDescription());
        assertEquals(6, donation.getPriority());
    }

    @Test
    void packagedRequestGivesPackagedFood() {
        Donation donation = factory.createDonation(food(DonationType.PACKAGED));

        assertInstanceOf(PackagedFoodDonation.class, donation);
        assertEquals("Packaged food donation", donation.getDescription());
        assertEquals(3, donation.getPriority());
    }

    @Test
    void moneyRequestGivesMoneyDonation() {
        Donation donation = factory.createDonation(money("250.50"));

        MoneyDonation money = assertInstanceOf(MoneyDonation.class, donation);
        assertEquals(new BigDecimal("250.50"), money.getAmount());
        assertEquals("Money donation", donation.getDescription());
        assertEquals(1, donation.getPriority());
        assertSame(donor, donation.getDonor());
        assertNull(donation.getExpiry());
    }

    @ParameterizedTest
    @EnumSource(value = DonationType.class, names = "MONEY", mode = EnumSource.Mode.EXCLUDE)
    void everyFoodTypeCopiesAllFoodFields(DonationType type) {
        Donation donation = factory.createDonation(food(type));

        FoodDonation food = assertInstanceOf(FoodDonation.class, donation);
        assertEquals(type, food.getType());
        assertSame(donor, food.getDonor());
        assertEquals("Rice and chicken", food.getFoodName());
        assertEquals(5.0, food.getQuantity());
        assertEquals(Unit.KG, food.getUnit());
        assertSame(meals, food.getCategory());
        assertSame(frozen, food.getStorageType());
        assertEquals(EXPIRY, food.getExpiry());
        assertEquals(PICKUP_START, food.getPickupStart());
        assertEquals(PICKUP_END, food.getPickupEnd());
        assertEquals("12 Nile St, Cairo", food.getPickupAddress());
    }

    @Test
    void moneyNeverKeepsAnExpiry() {
        DonationRequest request = money("10");
        request.setExpiry(EXPIRY);

        Donation donation = factory.createDonation(request);
        donation.setExpiry(EXPIRY);

        assertNull(donation.getExpiry());
    }

    // --- DON-3: food name and pickup address ---

    @Test
    void foodNameAndPickupAddressAreTrimmed() {
        FoodDonation food = (FoodDonation) factory.createDonation(food(r -> {
            r.setFoodName("  Bread  ");
            r.setPickupAddress("  1 Main St  ");
        }));

        assertEquals("Bread", food.getFoodName());
        assertEquals("1 Main St", food.getPickupAddress());
    }

    @Test
    void missingPickupAddressUsesTheDonorsAddress() {
        FoodDonation food = (FoodDonation) factory.createDonation(food(r -> r.setPickupAddress(" ")));

        assertEquals("5 Tahrir Sq, Cairo", food.getPickupAddress());
    }

    @Test
    void noPickupAddressAnywhereIsRejected() {
        donor.setAddress(null);

        assertRejected(food(r -> r.setPickupAddress(null)), "Pickup address is required");
    }

    // --- rejected input ---

    @Test
    void nullRequestIsRejected() {
        assertRejected(null, "Donation request is required");
    }

    @Test
    void missingTypeIsRejected() {
        assertRejected(food(r -> r.setType(null)), "Donation type is required");
    }

    @Test
    void missingDonorIsRejected() {
        assertRejected(food(r -> r.setDonor(null)), "Donor is required");
    }

    @Test
    void missingFoodNameIsRejected() {
        assertRejected(food(r -> r.setFoodName(null)), "Food name is required");
        assertRejected(food(r -> r.setFoodName("  ")), "Food name is required");
    }

    @Test
    void missingOrBadQuantityIsRejected() {
        assertRejected(food(r -> r.setQuantity(null)), "Quantity is required");
        assertRejected(food(r -> r.setQuantity(0.0)), "Quantity must be finite and greater than zero");
        assertRejected(food(r -> r.setQuantity(-2.0)), "Quantity must be finite and greater than zero");
        assertRejected(food(r -> r.setQuantity(Double.NaN)), "Quantity must be finite and greater than zero");
        assertRejected(food(r -> r.setQuantity(Double.POSITIVE_INFINITY)),
                "Quantity must be finite and greater than zero");
    }

    @Test
    void missingFoodDetailsAreRejected() {
        assertRejected(food(r -> r.setUnit(null)), "Unit is required");
        assertRejected(food(r -> r.setCategory(null)), "Food category is required");
        assertRejected(food(r -> r.setStorageType(null)), "Storage type is required");
        assertRejected(food(r -> r.setExpiry(null)), "Expiry is required");
        assertRejected(food(r -> r.setPickupStart(null)), "Pickup start is required");
        assertRejected(food(r -> r.setPickupEnd(null)), "Pickup end is required");
    }

    @Test
    void pickupWindowMustStartBeforeItEnds() {
        assertRejected(food(r -> r.setPickupEnd(PICKUP_START)), "Pickup start must be before pickup end");
        assertRejected(food(r -> r.setPickupEnd(PICKUP_START.minusHours(1))),
                "Pickup start must be before pickup end");
    }

    @Test
    void missingOrBadAmountIsRejected() {
        assertRejected(money(null), "Amount is required");
        assertRejected(money("0"), "Amount must be greater than zero");
        assertRejected(money("-5"), "Amount must be greater than zero");
    }

    // --- add-ons ---

    @Test
    void addOnsAreAlwaysWrappedInEnumOrder() {
        Donation plain = factory.createDonation(food(DonationType.COOKED));

        Donation wrapped = factory.applyAddOns(plain,
                new HashSet<>(Arrays.asList(AddOn.SPECIAL_PACKAGING, AddOn.HOME_PICKUP, AddOn.URGENT_PRIORITY)));

        // the last add-on in the enum is the outermost wrapper
        SpecialPackaging packaging = assertInstanceOf(SpecialPackaging.class, wrapped);
        assertEquals("Cooked food donation + Home pickup + Urgent priority + Special packaging",
                packaging.getDescription());
        assertEquals(8 + 1 + 5, wrapped.getPriority());
        assertSame(plain, wrapped.getBaseDonation());
    }

    @Test
    void allFourAddOnsStack() {
        Donation wrapped = factory.applyAddOns(factory.createDonation(food(DonationType.COOKED)),
                EnumSet.allOf(AddOn.class));

        assertEquals(EnumSet.allOf(AddOn.class), DonationDecorator.collectAddOns(wrapped));
        assertEquals("Cooked food donation + Home pickup + Refrigerated transport + Urgent priority"
                + " + Special packaging", wrapped.getDescription());
    }

    @Test
    void noAddOnsLeavesTheDonationAsItIs() {
        Donation plain = factory.createDonation(food(DonationType.COOKED));

        assertSame(plain, factory.applyAddOns(plain, null));
        assertSame(plain, factory.applyAddOns(plain, Set.of()));
    }

    @Test
    void addingAnAddOnKeepsTheOnesAlreadyThereWithoutDuplicates() {
        Donation plain = factory.createDonation(food(DonationType.COOKED));
        Donation once = factory.applyAddOns(plain, Set.of(AddOn.URGENT_PRIORITY));

        Donation twice = factory.applyAddOns(once, Set.of(AddOn.HOME_PICKUP, AddOn.URGENT_PRIORITY));

        assertEquals(EnumSet.of(AddOn.HOME_PICKUP, AddOn.URGENT_PRIORITY), DonationDecorator.collectAddOns(twice));
        // rebuilt from scratch, so Home pickup now sits inside Urgent priority (enum order)
        assertInstanceOf(UrgentPriority.class, twice);
        assertEquals("Cooked food donation + Home pickup + Urgent priority", twice.getDescription());
        assertEquals(8 + 1 + 5, twice.getPriority());
        assertSame(plain, twice.getBaseDonation());
    }

    @Test
    void nullAddOnIsRejected() {
        Donation plain = factory.createDonation(food(DonationType.COOKED));

        assertThrows(IllegalArgumentException.class,
                () -> factory.applyAddOns(plain, new HashSet<>(Arrays.asList(AddOn.HOME_PICKUP, null))));
        assertThrows(IllegalArgumentException.class, () -> factory.applyAddOns(null, Set.of()));
    }

    // --- restore ---

    @Test
    void restoreKeepsSavedIdStatusDateReasonAndAddOns() {
        DonationStatus rejected = new DonationStatus(3, DonationStatus.REJECTED, true);
        LocalDateTime created = LocalDateTime.of(2026, 1, 15, 9, 30);

        Donation donation = factory.restore(food(DonationType.FRESH), 42, rejected, created, "Spoiled",
                Set.of(AddOn.REFRIGERATED_TRANSPORT));

        assertInstanceOf(RefrigeratedTransport.class, donation);
        assertInstanceOf(FreshProduceDonation.class, donation.getBaseDonation());
        assertEquals(42, donation.getId());
        assertSame(rejected, donation.getStatus());
        assertEquals(created, donation.getCreatedAt());
        assertEquals("Spoiled", donation.getRejectionReason());
    }

    @Test
    void restoreSkipsNewDonationChecks() {
        // an old row may break today's rules (e.g. saved before food name was required)
        DonationRequest old = food(r -> {
            r.setFoodName(null);
            r.setPickupAddress(null);
            r.setPickupEnd(PICKUP_START.minusHours(1));
        });

        Donation donation = factory.restore(old, 5, null, PICKUP_START, null, null);

        FoodDonation food = assertInstanceOf(CookedFoodDonation.class, donation);
        assertNull(food.getFoodName());
        assertNull(food.getPickupAddress());
    }

    @Test
    void restoreStillNeedsAType() {
        assertThrows(IllegalArgumentException.class,
                () -> factory.restore(food(r -> r.setType(null)), 1, null, PICKUP_START, null, null));
    }

    private void assertRejected(DonationRequest request, String message) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> factory.createDonation(request));
        assertEquals(message, e.getMessage());
    }
}
