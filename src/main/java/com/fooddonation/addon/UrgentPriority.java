package com.fooddonation.addon;

import com.fooddonation.model.donation.Donation;

/**
 * DECORATOR PATTERN - Concrete Decorator.
 * Adds "urgent priority": the donation is urgent or near expiry, so it moves to the top of the list.
 * Priority bonus: +5
 */
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
        // the wrapped donation's text, plus our service
        return wrapped.getDescription() + " + Urgent priority";
    }

    @Override
    public int getPriority() {
        // the wrapped donation's priority, plus our bonus
        return wrapped.getPriority() + 5;
    }
}
