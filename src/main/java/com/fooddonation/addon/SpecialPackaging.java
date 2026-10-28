package com.fooddonation.addon;

import com.fooddonation.model.donation.Donation;

public class SpecialPackaging extends DonationDecorator {

    public SpecialPackaging(Donation wrapped) {
        super(wrapped);
    }

    @Override
    public AddOn getAddOn() {
        return AddOn.SPECIAL_PACKAGING;
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
