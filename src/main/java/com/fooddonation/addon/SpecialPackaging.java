package com.fooddonation.addon;

import com.fooddonation.model.donation.Donation;

/**
 * DECORATOR PATTERN - Concrete Decorator.
 * Adds "special packaging": special containers or labels are needed.
 * Priority bonus: +0 (it changes the description only)
 */
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
        // the wrapped donation's text, plus our service
        return wrapped.getDescription() + " + Special packaging";
    }

    @Override
    public int getPriority() {
        // no bonus: packaging does not make a donation more urgent
        return wrapped.getPriority() + 0;
    }
}
