package com.fooddonation.model.donation;

import com.fooddonation.model.lookup.FoodCategory;
import com.fooddonation.model.lookup.StorageType;
import com.fooddonation.model.user.Donor;

import java.time.LocalDate;
import java.time.LocalDateTime;

public abstract class FoodDonation extends Donation {
    private double quantity;
    private Unit unit;
    private FoodCategory category;
    private StorageType storageType;
    private LocalDateTime pickupStart;
    private LocalDateTime pickupEnd;

    protected FoodDonation(DonationType type) {
        super(type);
    }

    protected FoodDonation(Donor donor, DonationType type, LocalDate expiry, double quantity, Unit unit,
                           FoodCategory category, StorageType storageType,
                           LocalDateTime pickupStart, LocalDateTime pickupEnd) {
        super(donor, type, expiry);
        this.quantity = quantity;
        this.unit = unit;
        this.category = category;
        this.storageType = storageType;
        this.pickupStart = pickupStart;
        this.pickupEnd = pickupEnd;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public FoodCategory getCategory() {
        return category;
    }

    public void setCategory(FoodCategory category) {
        this.category = category;
    }

    public StorageType getStorageType() {
        return storageType;
    }

    public void setStorageType(StorageType storageType) {
        this.storageType = storageType;
    }

    public LocalDateTime getPickupStart() {
        return pickupStart;
    }

    public void setPickupStart(LocalDateTime pickupStart) {
        this.pickupStart = pickupStart;
    }

    public LocalDateTime getPickupEnd() {
        return pickupEnd;
    }

    public void setPickupEnd(LocalDateTime pickupEnd) {
        this.pickupEnd = pickupEnd;
    }
}
