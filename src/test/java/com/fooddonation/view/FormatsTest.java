package com.fooddonation.view;

import com.fooddonation.model.donation.addon.AddOn;
import com.fooddonation.model.donation.Donation;
import com.fooddonation.model.donation.DonationFactory;
import com.fooddonation.model.donation.DonationRequest;
import com.fooddonation.model.donation.DonationType;
import com.fooddonation.model.donation.Unit;
import com.fooddonation.model.lookup.DonationStatus;
import com.fooddonation.model.lookup.FoodCategory;
import com.fooddonation.model.lookup.StorageType;
import com.fooddonation.model.user.Donor;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FormatsTest {
    DonationFactory factory = new DonationFactory();
    Donor donor = new Donor();

    Donation food() {
        DonationRequest r = new DonationRequest();
        r.setType(DonationType.COOKED);
        r.setDonor(donor);
        r.setFoodName("Koshari");
        r.setQuantity(20.0);
        r.setUnit(Unit.BOXES);
        r.setCategory(new FoodCategory(3, "Meals", false));
        r.setStorageType(new StorageType(1, "Room temperature", false));
        r.setExpiry(LocalDate.of(2026, 11, 4));
        r.setPickupStart(LocalDateTime.of(2026, 11, 3, 10, 0));
        r.setPickupEnd(LocalDateTime.of(2026, 11, 3, 12, 30));
        r.setPickupAddress("5 Tahrir Sq");
        return factory.applyAddOns(factory.createDonation(r), Set.of(AddOn.HOME_PICKUP));
    }

    @Test
    void enumNamesBecomeReadableLabels() {
        assertEquals("Home pickup", Formats.label(AddOn.HOME_PICKUP));
        assertEquals("In app", Formats.label(com.fooddonation.notification.ChannelType.IN_APP));
        assertEquals("", Formats.label((Enum<?>) null));
    }

    @Test
    void foodDetailsLookThroughTheAddOnWrappers() {
        Donation donation = food();

        assertEquals("20 boxes of Koshari", Formats.details(donation));
        assertEquals("2026-11-03 10:00–12:30 at 5 Tahrir Sq", Formats.pickup(donation));
    }

    @Test
    void moneyShowsTheAmountAndNoPickup() {
        DonationRequest r = new DonationRequest();
        r.setType(DonationType.MONEY);
        r.setDonor(donor);
        r.setAmount(new BigDecimal("250.50"));
        Donation money = factory.createDonation(r);

        assertEquals("250.50", Formats.details(money));
        assertEquals("", Formats.pickup(money));
    }

    @Test
    void statusIsEmptyUntilSaved() {
        Donation donation = food();
        assertEquals("", Formats.status(donation));

        donation.setStatus(new DonationStatus(1, DonationStatus.SUBMITTED, true));
        assertEquals("Submitted", Formats.status(donation));
    }
}
