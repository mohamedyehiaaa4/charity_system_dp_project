package com.fooddonation.addon;

import com.fooddonation.model.donation.Donation;

/**
 * DECORATOR PATTERN - Concrete Decorator.
 * Adds "home pickup": a volunteer collects the food from the donor's address.
 * Priority bonus: +1
 */
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
        // the wrapped donation's text, plus our service
        return wrapped.getDescription() + " + Home pickup";
    }

    @Override
    public int getPriority() {
        // the wrapped donation's priority, plus our bonus
        return wrapped.getPriority() + 1;
    }
}
