package com.fooddonation.addon;

import com.fooddonation.model.donation.Donation;

import java.util.Set;

public abstract class DonationDecorator extends Donation {
    protected final Donation wrapped;

    protected DonationDecorator(Donation wrapped) {
        this.wrapped = wrapped;
    }

    public Donation getWrapped() {
        return wrapped;
    }

    public abstract AddOn getAddOn();

    /** Returns the innermost (undecorated) donation. */
    public static Donation unwrap(Donation donation) {
        throw new UnsupportedOperationException("TODO");
    }

    /** Returns every add-on in the wrapper stack; empty for a plain donation. */
    public static Set<AddOn> collectAddOns(Donation donation) {
        throw new UnsupportedOperationException("TODO");
    }
}
