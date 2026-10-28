package com.fooddonation.addon;

import com.fooddonation.model.donation.Donation;

public class RefrigeratedTransport extends DonationDecorator {

    public RefrigeratedTransport(Donation wrapped) {
        super(wrapped);
    }

    @Override
    public AddOn getAddOn() {
        return AddOn.REFRIGERATED_TRANSPORT;
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
