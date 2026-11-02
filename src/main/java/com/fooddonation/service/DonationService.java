package com.fooddonation.service;

import com.fooddonation.addon.AddOn;
import com.fooddonation.event.DonationEvent;
import com.fooddonation.event.DonationObserver;
import com.fooddonation.event.DonationSubject;
import com.fooddonation.event.EventType;
import com.fooddonation.event.MessageTemplates;
import com.fooddonation.model.donation.Donation;
import com.fooddonation.model.donation.DonationFactory;
import com.fooddonation.model.donation.DonationRequest;
import com.fooddonation.model.donation.DonationType;
import com.fooddonation.model.lookup.DonationStatus;
import com.fooddonation.model.lookup.LookupType;
import com.fooddonation.model.user.Donor;
import com.fooddonation.model.user.Role;
import com.fooddonation.model.user.User;
import com.fooddonation.persistence.DonationRepository;
import com.fooddonation.persistence.LookupRepository;
import com.fooddonation.persistence.UserRepository;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Observer pattern: the Subject. Runs the donation lifecycle and tells every attached
 * observer about it, without knowing who they are or how they notify people.
 */
public class DonationService implements DonationSubject {
    private final DonationFactory factory;
    private final DonationRepository donations;
    private final LookupRepository lookups;
    private final UserRepository users;
    private final Clock clock;
    private final Duration warningWindow;
    // donations already warned about in this program run, so each one is warned only once
    private final Set<Long> warned = ConcurrentHashMap.newKeySet();
    // copy-on-write so the expiry scheduler thread can notify while observers are attached
    private final List<DonationObserver> observers = new CopyOnWriteArrayList<>();

    public DonationService(DonationFactory factory, DonationRepository donations, LookupRepository lookups,
                           UserRepository users, Clock clock) {
        this(factory, donations, lookups, users, clock, Duration.ofHours(48));
    }

    /** @param warningWindow how long before expiry a donation counts as near expiry */
    public DonationService(DonationFactory factory, DonationRepository donations, LookupRepository lookups,
                           UserRepository users, Clock clock, Duration warningWindow) {
        this.warningWindow = warningWindow;
        this.factory = factory;
        this.donations = donations;
        this.lookups = lookups;
        this.users = users;
        this.clock = clock;
    }

    @Override
    public void attach(DonationObserver observer) {
        if (observer == null) {
            throw new IllegalArgumentException("Observer is required");
        }
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void detach(DonationObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(DonationEvent event) {
        for (DonationObserver observer : observers) {
            // one failing observer must not stop the others from hearing about the event
            try {
                observer.update(event);
            } catch (RuntimeException e) {
                System.err.println("Observer " + observer.getClass().getSimpleName() + " failed: " + e.getMessage());
            }
        }
    }

    /** Creates the donation as the caller's own, saves it as Submitted, then fires DONATION_SUBMITTED. */
    public Donation submitDonation(User caller, DonationRequest request, Set<AddOn> addOns) {
        if (!(caller instanceof Donor donor)) {
            throw new SecurityException("Only donors can submit donations");
        }
        if (request == null) {
            throw new IllegalArgumentException("Donation request is required");
        }
        // a donor can only donate as themselves
        request.setDonor(donor);
        Donation donation = factory.applyAddOns(factory.createDonation(request), addOns);
        donation.setStatus(status(DonationStatus.SUBMITTED));
        donation.setCreatedAt(LocalDateTime.now(clock));
        Donation saved = donations.save(donation);
        fire(EventType.DONATION_SUBMITTED, saved);
        return saved;
    }

    public List<Donation> getMyDonations(User caller) {
        if (caller == null) {
            throw new SecurityException("Login required");
        }
        return donations.findByDonor(caller.getId());
    }

    public List<Donation> getAllDonations(User caller) {
        requireCoordinator(caller);
        return donations.findAll();
    }

    public void approve(User caller, long id) {
        changeStatus(caller, id, DonationStatus.SUBMITTED, DonationStatus.APPROVED, null);
    }

    public void reject(User caller, long id, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("A reason is required to reject a donation");
        }
        changeStatus(caller, id, DonationStatus.SUBMITTED, DonationStatus.REJECTED, reason.trim());
    }

    public void markPickedUp(User caller, long id) {
        changeStatus(caller, id, DonationStatus.APPROVED, DonationStatus.PICKED_UP, null);
    }

    public void markDelivered(User caller, long id) {
        changeStatus(caller, id, DonationStatus.PICKED_UP, DonationStatus.DELIVERED, null);
    }

    /**
     * Fires NEAR_EXPIRY for active food donations that expire within the warning window
     * and haven't been warned about yet. Returns the donations warned about in this call.
     * The clock's time zone decides when a day ends.
     */
    public List<Donation> checkExpiringDonations() {
        LocalDateTime now = LocalDateTime.now(clock);
        List<Donation> newlyWarned = new ArrayList<>();
        for (Donation donation : donations.findAll()) {
            if (isNearExpiry(donation, now) && warned.add(donation.getId())) {
                newlyWarned.add(donation);
                fire(EventType.NEAR_EXPIRY, donation);
            }
        }
        return newlyWarned;
    }

    // The whole lifecycle: each move is only allowed from one status, so final statuses can't change.
    private void changeStatus(User caller, long id, String from, String to, String reason) {
        requireCoordinator(caller);
        Donation donation = donations.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No donation with id " + id));
        String current = donation.getStatus() == null ? null : donation.getStatus().getName();
        if (!from.equals(current)) {
            throw new IllegalStateException("Can't move donation " + id + " from " + current + " to " + to);
        }
        DonationStatus next = status(to);
        donations.updateStatus(id, next, reason);
        // the repository only updates the database, so keep our copy in step before telling anyone
        donation.setStatus(next);
        donation.setRejectionReason(reason);
        fire(to.equals(DonationStatus.REJECTED) ? EventType.DONATION_REJECTED : EventType.STATUS_CHANGED, donation);
    }

    private boolean isNearExpiry(Donation donation, LocalDateTime now) {
        if (donation.getType() == DonationType.MONEY || donation.getExpiry() == null || donation.getStatus() == null) {
            return false;
        }
        String status = donation.getStatus().getName();
        if (DonationStatus.REJECTED.equals(status) || DonationStatus.DELIVERED.equals(status)) {
            return false;
        }
        // food is good until the end of its expiry day
        LocalDateTime expiresAt = donation.getExpiry().plusDays(1).atStartOfDay();
        return now.isBefore(expiresAt) && !expiresAt.isAfter(now.plus(warningWindow));
    }

    private DonationStatus status(String name) {
        return (DonationStatus) lookups.findByName(LookupType.DONATION_STATUS, name)
                .orElseThrow(() -> new IllegalStateException("Missing system status " + name));
    }

    private void fire(EventType type, Donation donation) {
        notifyObservers(new DonationEvent(type, donation, MessageTemplates.message(type, donation),
                LocalDateTime.now(clock)));
    }

    private static void requireCoordinator(User caller) {
        if (caller == null || caller.getRole() != Role.COORDINATOR) {
            throw new SecurityException("Only coordinators can do this");
        }
    }
}
