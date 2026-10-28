package com.fooddonation.addon;

import com.fooddonation.model.donation.Donation;

public class HomePickup extends DonationDecorator {

    public HomePickup(Donation wrapped) {
        super(wrapped);
    }

    @Override
    public AddOn getAddOn() {
        return AddOn.HOME_PICKUP;
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
