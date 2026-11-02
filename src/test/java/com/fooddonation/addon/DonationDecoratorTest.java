package com.fooddonation.addon;

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
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DonationDecoratorTest {
    DonationFactory factory = new DonationFactory();
    Donor donor = new Donor();

    /** A plain donation with an easy-to-check description and priority. */
    static class PlainDonation extends Donation {
        PlainDonation() {
            super(null, DonationType.PACKAGED, LocalDate.of(2026, 11, 10));
        }

        @Override
        public String getDescription() {
            return "Plain";
        }

        @Override
        public int getPriority() {
            return 10;
        }
    }

    @Test
    void wrappedFoodDonationStillGivesItsFoodFields() {
        DonationRequest request = new DonationRequest();
        request.setType(DonationType.COOKED);
        request.setDonor(donor);
        request.setFoodName("Rice and chicken");
        request.setQuantity(5.0);
        request.setUnit(Unit.KG);
        request.setCategory(new FoodCategory(1, "Meals", false));
        request.setStorageType(new StorageType(1, "Frozen", false));
        request.setExpiry(LocalDate.of(2026, 11, 3));
        request.setPickupStart(LocalDateTime.of(2026, 11, 2, 10, 0));
        request.setPickupEnd(LocalDateTime.of(2026, 11, 2, 12, 0));
        request.setPickupAddress("12 Nile St, Cairo");
        Donation plain = factory.createDonation(request);

        Donation wrapped = factory.applyAddOns(plain, EnumSet.of(AddOn.HOME_PICKUP, AddOn.URGENT_PRIORITY));

        assertFalse(wrapped instanceof FoodDonation);
        assertSame(plain, wrapped.getBaseDonation());
        FoodDonation food = assertInstanceOf(FoodDonation.class, wrapped.getBaseDonation());
        assertEquals(5.0, food.getQuantity());
        assertEquals("Rice and chicken", food.getFoodName());
    }

    @Test
    void plainDonationIsItsOwnBase() {
        DonationRequest request = new DonationRequest();
        request.setType(DonationType.MONEY);
        request.setDonor(donor);
        request.setAmount(new BigDecimal("100"));
        Donation money = factory.createDonation(request);

        assertSame(money, money.getBaseDonation());
        assertInstanceOf(MoneyDonation.class, money.getBaseDonation());
    }

    // --- each wrapper on its own ---

    @Test
    void homePickupAddsTextAndOnePoint() {
        Donation donation = new HomePickup(new PlainDonation());

        assertEquals("Plain + Home pickup", donation.getDescription());
        assertEquals(11, donation.getPriority());
        assertEquals(AddOn.HOME_PICKUP, ((DonationDecorator) donation).getAddOn());
    }

    @Test
    void refrigeratedTransportAddsTextAndOnePoint() {
        Donation donation = new RefrigeratedTransport(new PlainDonation());

        assertEquals("Plain + Refrigerated transport", donation.getDescription());
        assertEquals(11, donation.getPriority());
        assertEquals(AddOn.REFRIGERATED_TRANSPORT, ((DonationDecorator) donation).getAddOn());
    }

    @Test
    void urgentPriorityAddsTextAndFivePoints() {
        Donation donation = new UrgentPriority(new PlainDonation());

        assertEquals("Plain + Urgent priority", donation.getDescription());
        assertEquals(15, donation.getPriority());
        assertEquals(AddOn.URGENT_PRIORITY, ((DonationDecorator) donation).getAddOn());
    }

    @Test
    void specialPackagingAddsTextButNoPoints() {
        Donation donation = new SpecialPackaging(new PlainDonation());

        assertEquals("Plain + Special packaging", donation.getDescription());
        assertEquals(10, donation.getPriority());
        assertEquals(AddOn.SPECIAL_PACKAGING, ((DonationDecorator) donation).getAddOn());
    }

    @Test
    void wrappingNothingIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new HomePickup(null));
    }

    // --- stacking ---

    @Test
    void stackedWrappersAddUpInWrappingOrder() {
        Donation donation = new SpecialPackaging(new UrgentPriority(new HomePickup(
                new RefrigeratedTransport(new PlainDonation()))));

        assertEquals("Plain + Refrigerated transport + Home pickup + Urgent priority + Special packaging",
                donation.getDescription());
        assertEquals(10 + 1 + 1 + 5 + 0, donation.getPriority());
    }

    @Test
    void unwrapRemovesEveryLayer() {
        PlainDonation plain = new PlainDonation();
        Donation donation = new UrgentPriority(new HomePickup(plain));

        assertSame(plain, DonationDecorator.unwrap(donation));
        assertSame(plain, donation.getBaseDonation());
    }

    @Test
    void unwrapOfAPlainDonationReturnsItself() {
        PlainDonation plain = new PlainDonation();

        assertSame(plain, DonationDecorator.unwrap(plain));
    }

    @Test
    void collectAddOnsFindsEveryLayer() {
        Donation donation = new UrgentPriority(new HomePickup(new SpecialPackaging(new PlainDonation())));

        assertEquals(Set.of(AddOn.URGENT_PRIORITY, AddOn.HOME_PICKUP, AddOn.SPECIAL_PACKAGING),
                DonationDecorator.collectAddOns(donation));
    }

    @Test
    void collectAddOnsOfAPlainDonationIsEmpty() {
        assertTrue(DonationDecorator.collectAddOns(new PlainDonation()).isEmpty());
    }

    // --- data lives only in the wrapped donation ---

    @Test
    void settersOnTheWrapperChangeTheWrappedDonation() {
        PlainDonation plain = new PlainDonation();
        Donation donation = new UrgentPriority(new HomePickup(plain));
        DonationStatus approved = new DonationStatus(2, DonationStatus.APPROVED, true);
        LocalDateTime created = LocalDateTime.of(2026, 11, 2, 9, 0);

        donation.setId(42);
        donation.setDonor(donor);
        donation.setStatus(approved);
        donation.setCreatedAt(created);
        donation.setRejectionReason("none");
        donation.setExpiry(LocalDate.of(2026, 11, 20));

        assertEquals(42, plain.getId());
        assertSame(donor, plain.getDonor());
        assertSame(approved, plain.getStatus());
        assertEquals(created, plain.getCreatedAt());
        assertEquals("none", plain.getRejectionReason());
        assertEquals(LocalDate.of(2026, 11, 20), plain.getExpiry());
        assertEquals(DonationType.PACKAGED, donation.getType());
    }

    @Test
    void changesToTheWrappedDonationShowThroughTheWrapper() {
        PlainDonation plain = new PlainDonation();
        Donation donation = new HomePickup(plain);

        plain.setId(7);
        plain.setRejectionReason("spoiled");

        assertEquals(7, donation.getId());
        assertEquals("spoiled", donation.getRejectionReason());
    }
}
