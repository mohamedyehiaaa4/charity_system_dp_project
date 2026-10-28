package com.fooddonation.service;

import com.fooddonation.addon.AddOn;
import com.fooddonation.event.DonationEvent;
import com.fooddonation.event.DonationObserver;
import com.fooddonation.event.DonationSubject;
import com.fooddonation.model.donation.Donation;
import com.fooddonation.model.donation.DonationFactory;
import com.fooddonation.model.donation.DonationRequest;
import com.fooddonation.model.user.User;
import com.fooddonation.persistence.DonationRepository;
import com.fooddonation.persistence.LookupRepository;
import com.fooddonation.persistence.UserRepository;

import java.time.Clock;
import java.util.List;
import java.util.Set;

public class DonationService implements DonationSubject {
    private final DonationFactory factory;
    private final DonationRepository donations;
    private final LookupRepository lookups;
    private final UserRepository users;
    private final Clock clock;

    public DonationService(DonationFactory factory, DonationRepository donations, LookupRepository lookups,
                           UserRepository users, Clock clock) {
        this.factory = factory;
        this.donations = donations;
        this.lookups = lookups;
        this.users = users;
        this.clock = clock;
    }

    @Override
    public void attach(DonationObserver observer) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void detach(DonationObserver observer) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void notifyObservers(DonationEvent event) {
        throw new UnsupportedOperationException("TODO");
    }

    public Donation submitDonation(User caller, DonationRequest request, Set<AddOn> addOns) {
        throw new UnsupportedOperationException("TODO");
    }

    public List<Donation> getMyDonations(User caller) {
        throw new UnsupportedOperationException("TODO");
    }

    public List<Donation> getAllDonations(User caller) {
        throw new UnsupportedOperationException("TODO");
    }

    public void approve(User caller, long id) {
        throw new UnsupportedOperationException("TODO");
    }

    public void reject(User caller, long id, String reason) {
        throw new UnsupportedOperationException("TODO");
    }

    public void markPickedUp(User caller, long id) {
        throw new UnsupportedOperationException("TODO");
    }

    public void markDelivered(User caller, long id) {
        throw new UnsupportedOperationException("TODO");
    }

    public List<Donation> checkExpiringDonations() {
        throw new UnsupportedOperationException("TODO");
    }
}
