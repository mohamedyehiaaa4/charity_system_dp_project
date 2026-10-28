package com.fooddonation.persistence;

import com.fooddonation.model.donation.Donation;
import com.fooddonation.model.donation.DonationFactory;
import com.fooddonation.model.lookup.DonationStatus;

import java.util.List;
import java.util.Optional;

public class DonationRepository {
    private final DatabaseConnection db;
    private final DonationFactory factory;

    public DonationRepository(DatabaseConnection db, DonationFactory factory) {
        this.db = db;
        this.factory = factory;
    }

    /** Returns the donation with its new ID. */
    public Donation save(Donation donation) {
        throw new UnsupportedOperationException("TODO");
    }

    public Optional<Donation> findById(long id) {
        throw new UnsupportedOperationException("TODO");
    }

    public List<Donation> findAll() {
        throw new UnsupportedOperationException("TODO");
    }

    public List<Donation> findByDonor(long donorId) {
        throw new UnsupportedOperationException("TODO");
    }

    public List<Donation> findByStatus(DonationStatus status) {
        throw new UnsupportedOperationException("TODO");
    }

    public void updateStatus(long donationId, DonationStatus status, String rejectionReason) {
        throw new UnsupportedOperationException("TODO");
    }
}
