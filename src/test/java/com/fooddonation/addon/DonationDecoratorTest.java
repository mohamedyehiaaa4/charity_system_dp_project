package com.fooddonation.addon;

import com.fooddonation.model.donation.Donation;
import com.fooddonation.model.donation.DonationFactory;
import com.fooddonation.model.donation.DonationRequest;
import com.fooddonation.model.donation.DonationType;
import com.fooddonation.model.donation.FoodDonation;
import com.fooddonation.model.donation.MoneyDonation;
import com.fooddonation.model.donation.Unit;
import com.fooddonation.model.lookup.FoodCategory;
import com.fooddonation.model.lookup.StorageType;
import com.fooddonation.model.user.Donor;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

class DonationDecoratorTest {
    DonationFactory factory = new DonationFactory();
    Donor donor = new Donor();

    @Test
    void wrappedFoodDonationStillGivesItsFoodFields() {
        DonationRequest request = new DonationRequest();
        request.setType(DonationType.COOKED);
        request.setDonor(donor);
        request.setQuantity(5.0);
        request.setUnit(Unit.KG);
        request.setCategory(new FoodCategory(1, "Meals", false));
        request.setStorageType(new StorageType(1, "Frozen", false));
        request.setExpiry(LocalDate.of(2026, 11, 3));
        request.setPickupStart(LocalDateTime.of(2026, 11, 2, 10, 0));
        request.setPickupEnd(LocalDateTime.of(2026, 11, 2, 12, 0));
        Donation plain = factory.createDonation(request);

        Donation wrapped = factory.applyAddOns(plain, EnumSet.of(AddOn.HOME_PICKUP, AddOn.URGENT_PRIORITY));

        assertFalse(wrapped instanceof FoodDonation);
        assertSame(plain, wrapped.getBaseDonation());
        assertEquals(5.0, assertInstanceOf(FoodDonation.class, wrapped.getBaseDonation()).getQuantity());
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
}
