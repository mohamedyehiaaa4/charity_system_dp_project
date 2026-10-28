package com.fooddonation.addon;

import com.fooddonation.model.donation.Donation;

/**
 * DECORATOR PATTERN - Concrete Decorator.
 * Adds "refrigerated transport": the food is kept cold while it is moved (cold chain).
 * Priority bonus: +1
 */
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
        // the wrapped donation's text, plus our service
        return wrapped.getDescription() + " + Refrigerated transport";
    }

    @Override
    public int getPriority() {
        // the wrapped donation's priority, plus our bonus
        return wrapped.getPriority() + 1;
    }
}
