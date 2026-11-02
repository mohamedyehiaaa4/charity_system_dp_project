package com.fooddonation.addon;

import com.fooddonation.model.donation.Donation;
import com.fooddonation.model.donation.DonationType;
import com.fooddonation.model.lookup.DonationStatus;
import com.fooddonation.model.user.Donor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;

/**
 * DECORATOR PATTERN - Base Decorator.
 *
 * A decorator "wraps" a donation to add an optional service (home pickup, urgent priority, ...).
 * It extends Donation, so the rest of the code can use a wrapped donation like a normal one.
 * Wrappers can be stacked: new UrgentPriority(new HomePickup(cookedMeal)).
 *
 * The real data (id, status, expiry, ...) lives only in the wrapped donation.
 * Every getter/setter below just passes the call to it, so the data never gets out of sync.
 */
public abstract class DonationDecorator extends Donation {

    // the donation we are wrapping (may itself be another decorator)
    protected Donation wrapped;

    protected DonationDecorator(Donation wrapped) {
        if (wrapped == null) {
            throw new IllegalArgumentException("Cannot add a service to a missing donation");
        }
        this.wrapped = wrapped;
    }

    /** Which add-on this wrapper is. Each concrete decorator returns its own. */
    public abstract AddOn getAddOn(); // bla bla

    // ---- The parts decorators change. Each concrete decorator must write its own. ----
    @Override
    public abstract String getDescription();

    @Override
    public abstract int getPriority();

    // ---- Helpers for DB ----

    /** Removes all wrappers and returns the real donation inside. */
    public static Donation unwrap(Donation donation) {
        while (donation instanceof DonationDecorator decorator) {
            donation = decorator.wrapped;
        }
        return donation;
    }

    /** Returns every add-on on this donation. A plain donation gives an empty set. */
    public static Set<AddOn> collectAddOns(Donation donation) {
        Set<AddOn> addOns = EnumSet.noneOf(AddOn.class);
        while (donation instanceof DonationDecorator decorator) {
            addOns.add(decorator.getAddOn());
            donation = decorator.wrapped;
        }
        return addOns;
    }

    // ---- Pass-through: all data is read from / written to the wrapped donation ----

    @Override 
    public long getId() { 
        return wrapped.getId(); }

    @Override
    public void setId(long id) {
         wrapped.setId(id); }

    @Override 
    public Donor getDonor() {
         return wrapped.getDonor(); }

    @Override 
    public void setDonor(Donor donor) {
         wrapped.setDonor(donor); }

    @Override 
    public DonationStatus getStatus() {
         return wrapped.getStatus(); }

    @Override 
    public void setStatus(DonationStatus status) { 
        wrapped.setStatus(status); }

    @Override 
    public LocalDateTime getCreatedAt() {
         return wrapped.getCreatedAt(); }

    @Override 
    public void setCreatedAt(LocalDateTime createdAt) {
         wrapped.setCreatedAt(createdAt); }

    @Override 
    public String getRejectionReason() {
         return wrapped.getRejectionReason(); }

    @Override 
    public void setRejectionReason(String reason) {
         wrapped.setRejectionReason(reason); }

    @Override 
    public DonationType getType() {
         return wrapped.getType(); }

    @Override 
    public void setType(DonationType type) {
         wrapped.setType(type); }

    @Override 
    public LocalDate getExpiry() {
         return wrapped.getExpiry(); }

    @Override 
    public void setExpiry(LocalDate expiry) {
         wrapped.setExpiry(expiry); }
}
