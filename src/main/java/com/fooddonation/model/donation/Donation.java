package com.fooddonation.model.donation;

import com.fooddonation.model.lookup.DonationStatus;
import com.fooddonation.model.user.Donor;

import java.time.LocalDate;
import java.time.LocalDateTime;

public abstract class Donation {
    private long id;
    private Donor donor;
    private DonationStatus status;
    private LocalDateTime createdAt;
    private String rejectionReason;
    private DonationType type;
    private LocalDate expiry;

    protected Donation() {
    }

    protected Donation(DonationType type) {
        this.type = type;
    }

    protected Donation(Donor donor, DonationType type, LocalDate expiry) {
        this.donor = donor;
        this.type = type;
        this.expiry = expiry;
    }

    public abstract String getDescription();

    public abstract int getPriority();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Donor getDonor() {
        return donor;
    }

    public void setDonor(Donor donor) {
        this.donor = donor;
    }

    public DonationStatus getStatus() {
        return status;
    }

    public void setStatus(DonationStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public DonationType getType() {
        return type;
    }

    public void setType(DonationType type) {
        this.type = type;
    }

    public LocalDate getExpiry() {
        return expiry;
    }

    public void setExpiry(LocalDate expiry) {
        this.expiry = expiry;
    }
}
