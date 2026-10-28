package com.fooddonation.addon;

import com.fooddonation.model.donation.Donation;

public class UrgentPriority extends DonationDecorator {

    public UrgentPriority(Donation wrapped) {
        super(wrapped);
    }

    @Override
    public AddOn getAddOn() {
        return AddOn.URGENT_PRIORITY;
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
