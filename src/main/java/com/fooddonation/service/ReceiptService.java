package com.fooddonation.service;

import com.fooddonation.model.donation.Donation;
import com.fooddonation.model.donation.FoodDonation;
import com.fooddonation.model.donation.MoneyDonation;
import com.fooddonation.model.lookup.DonationStatus;
import com.fooddonation.model.receipt.AnnualDonationSummary;
import com.fooddonation.model.receipt.DonationReceipt;
import com.fooddonation.model.user.Role;
import com.fooddonation.model.user.User;
import com.fooddonation.persistence.DonationRepository;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * DON-10: receipts for accepted donations and the annual summary for tax purposes.
 * Both are built from the saved donations, so nothing extra is stored.
 */
public class ReceiptService {
    // a donation counts as accepted once it is approved, and stays accepted while it is picked up and delivered
    private static final Set<String> ACCEPTED =
            Set.of(DonationStatus.APPROVED, DonationStatus.PICKED_UP, DonationStatus.DELIVERED);

    private final DonationRepository donations;
    private final Clock clock;

    public ReceiptService(DonationRepository donations, Clock clock) {
        this.donations = donations;
        this.clock = clock;
    }

    /** The receipt for one accepted donation. Donors can only get receipts for their own donations. */
    public DonationReceipt getReceipt(User caller, long donationId) {
        Donation donation = donations.findById(donationId)
                .orElseThrow(() -> new IllegalArgumentException("No donation with id " + donationId));
        requireOwnerOrCoordinator(caller, donation.getDonor().getId());
        return receiptFor(donation);
    }

    /** Every accepted donation the donor made in the given year, oldest first. */
    public AnnualDonationSummary getAnnualSummary(User caller, long donorId, int year) {
        requireOwnerOrCoordinator(caller, donorId);
        List<DonationReceipt> receipts = donations.findByDonor(donorId).stream()
                .filter(ReceiptService::isAccepted)
                .filter(donation -> donation.getCreatedAt().getYear() == year)
                .sorted(Comparator.comparing(Donation::getCreatedAt))
                .map(this::receiptFor)
                .toList();
        if (receipts.isEmpty()) {
            throw new IllegalStateException("No accepted donations in " + year);
        }
        return new AnnualDonationSummary(receipts.get(0).getDonor(), year, receipts, LocalDateTime.now(clock));
    }

    /** Builds the receipt without a caller check, for the system itself (see ReceiptObserver). */
    public DonationReceipt receiptFor(Donation donation) {
        if (!isAccepted(donation)) {
            throw new IllegalStateException("Donation " + donation.getId() + " has not been accepted");
        }
        Donation base = donation.getBaseDonation();
        String item = null;
        BigDecimal amount = null;
        if (base instanceof FoodDonation food) {
            item = BigDecimal.valueOf(food.getQuantity()).stripTrailingZeros().toPlainString() + " "
                    + food.getUnit().name().toLowerCase() + " of " + food.getFoodName();
        } else if (base instanceof MoneyDonation money) {
            amount = money.getAmount();
        }
        return new DonationReceipt(receiptNumber(donation), donation.getId(), donation.getDonor(),
                donation.getType(), donation.getDescription(), donation.getStatus().getName(), item, amount,
                donation.getCreatedAt(), LocalDateTime.now(clock));
    }

    public static boolean isAccepted(Donation donation) {
        return donation.getStatus() != null && ACCEPTED.contains(donation.getStatus().getName());
    }

    /** e.g. R-2026-000042: the year it was donated, then the donation ID, so the same donation always gets the same number. */
    static String receiptNumber(Donation donation) {
        return String.format("R-%d-%06d", donation.getCreatedAt().getYear(), donation.getId());
    }

    private static void requireOwnerOrCoordinator(User caller, long donorId) {
        if (caller == null) {
            throw new SecurityException("Login required");
        }
        if (caller.getRole() != Role.COORDINATOR && caller.getId() != donorId) {
            throw new SecurityException("Donors can only see their own receipts");
        }
    }
}
