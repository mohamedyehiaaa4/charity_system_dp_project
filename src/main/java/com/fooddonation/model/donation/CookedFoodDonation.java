package com.fooddonation.model.donation;

import com.fooddonation.model.lookup.FoodCategory;
import com.fooddonation.model.lookup.StorageType;
import com.fooddonation.model.user.Donor;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class CookedFoodDonation extends FoodDonation {

    CookedFoodDonation() {
        super(DonationType.COOKED);
    }

    CookedFoodDonation(Donor donor, LocalDate expiry, double quantity, Unit unit, FoodCategory category,
                       StorageType storageType, LocalDateTime pickupStart, LocalDateTime pickupEnd) {
        super(donor, DonationType.COOKED, expiry, quantity, unit, category, storageType, pickupStart, pickupEnd);
    }

    @Override
    public String getDescription() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public int getPriority() {
        throw new UnsupportedOperationException("TODO");
    }
}
