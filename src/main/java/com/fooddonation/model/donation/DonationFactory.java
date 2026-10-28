package com.fooddonation.model.donation;

import com.fooddonation.addon.AddOn;
import com.fooddonation.model.lookup.DonationStatus;

import java.time.LocalDateTime;
import java.util.Set;

public class DonationFactory {

    public Donation createDonation(DonationRequest request) {
        throw new UnsupportedOperationException("TODO");
    }

    public Donation applyAddOns(Donation donation, Set<AddOn> addOns) {
        throw new UnsupportedOperationException("TODO");
    }

    /** Rebuilds a saved donation from the database without new-donation checks. */
    public Donation restore(DonationRequest data, long id, DonationStatus status, LocalDateTime createdAt,
                            String rejectionReason, Set<AddOn> addOns) {
        throw new UnsupportedOperationException("TODO");
    }
}
