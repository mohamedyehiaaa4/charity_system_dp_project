package com.fooddonation.model.donation;

import com.fooddonation.model.lookup.FoodCategory;
import com.fooddonation.model.lookup.StorageType;
import com.fooddonation.model.user.Donor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Input data for creating a donation. Fields that don't apply to the type stay null. */
public class DonationRequest {
    private DonationType type;
    private Donor donor;
    private LocalDate expiry;
    private Double quantity;
    private Unit unit;
    private FoodCategory category;
    private StorageType storageType;
    private LocalDateTime pickupStart;
    private LocalDateTime pickupEnd;
    private BigDecimal amount;

    public DonationRequest() {
    }

    public DonationType getType() {
        return type;
    }

    public void setType(DonationType type) {
        this.type = type;
    }

    public Donor getDonor() {
        return donor;
    }

    public void setDonor(Donor donor) {
        this.donor = donor;
    }

    public LocalDate getExpiry() {
        return expiry;
    }

    public void setExpiry(LocalDate expiry) {
        this.expiry = expiry;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
