package com.fooddonation.model.donation;

import com.fooddonation.model.donation.addon.AddOn;
import com.fooddonation.model.donation.addon.DonationDecorator;
import com.fooddonation.model.donation.addon.HomePickup;
import com.fooddonation.model.donation.addon.RefrigeratedTransport;
import com.fooddonation.model.donation.addon.SpecialPackaging;
import com.fooddonation.model.donation.addon.UrgentPriority;
import com.fooddonation.model.lookup.DonationStatus;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;

/** Shared entry point that selects a Factory Method creator for each donation type. */
public class DonationFactory {

    public Donation createDonation(DonationRequest request) {
        requireRequest(request);
        require(request.getDonor() != null, "Donor is required");
        if (request.getType() == DonationType.MONEY) {
            require(request.getAmount() != null, "Amount is required");
            require(request.getAmount().signum() > 0, "Amount must be greater than zero");
        } else {
            require(!isBlank(request.getFoodName()), "Food name is required");
            require(request.getQuantity() != null, "Quantity is required");
            require(Double.isFinite(request.getQuantity()) && request.getQuantity() > 0,
                    "Quantity must be finite and greater than zero");
            require(request.getUnit() != null, "Unit is required");
            require(request.getCategory() != null, "Food category is required");
            require(request.getStorageType() != null, "Storage type is required");
            require(request.getExpiry() != null, "Expiry is required");
            require(request.getPickupStart() != null, "Pickup start is required");
            require(request.getPickupEnd() != null, "Pickup end is required");
            require(request.getPickupStart().isBefore(request.getPickupEnd()),
                    "Pickup start must be before pickup end");
            // most donors give food from home, so the pickup address defaults to the donor's address
            if (isBlank(request.getPickupAddress())) {
                request.setPickupAddress(request.getDonor().getAddress());
            }
            require(!isBlank(request.getPickupAddress()), "Pickup address is required");
            request.setFoodName(request.getFoodName().trim());
            request.setPickupAddress(request.getPickupAddress().trim());
        }
        return buildDonation(request);
    }

    /** Null or empty add-ons mean no additional services; existing services are retained. */
    public Donation applyAddOns(Donation donation, Set<AddOn> addOns) {
        require(donation != null, "Donation is required");
        Set<AddOn> combined = EnumSet.noneOf(AddOn.class);
        if (addOns != null) {
            for (AddOn addOn : addOns) {
                require(addOn != null, "Add-ons must not contain null");
            }
            combined.addAll(addOns);
        }
        if (!combined.isEmpty() && DonationDecorator.unwrap(donation) instanceof MoneyDonation) {
            throw new IllegalArgumentException("Add-ons are only available for food donations");
        }
        if (donation instanceof DonationDecorator) {
            combined.addAll(DonationDecorator.collectAddOns(donation));
            donation = DonationDecorator.unwrap(donation);
        }
        for (AddOn addOn : AddOn.values()) {
            if (combined.contains(addOn)) {
                donation = switch (addOn) {
                    case HOME_PICKUP -> new HomePickup(donation);
                    case REFRIGERATED_TRANSPORT -> new RefrigeratedTransport(donation);
                    case URGENT_PRIORITY -> new UrgentPriority(donation);
                    case SPECIAL_PACKAGING -> new SpecialPackaging(donation);
                };
            }
        }
        return donation;
    }

    /** Rebuilds a saved donation from the database without new-donation checks. */
    public Donation restore(DonationRequest data, long id, DonationStatus status, LocalDateTime createdAt,
                            String rejectionReason, Set<AddOn> addOns) {
        requireRequest(data);
        Donation donation = buildDonation(data);
        donation.setId(id);
        donation.setStatus(status);
        donation.setCreatedAt(createdAt);
        donation.setRejectionReason(rejectionReason);
        return applyAddOns(donation, addOns);
    }

    private Donation buildDonation(DonationRequest data) {
        DonationCreator creator = switch (data.getType()) {
            case COOKED -> new CookedFoodCreator();
            case REFRIGERATED -> new RefrigeratedFoodCreator();
            case FRESH -> new FreshProduceCreator();
            case PACKAGED -> new PackagedFoodCreator();
            case MONEY -> new MoneyDonationCreator();
        };
        return creator.buildDonation(data);
    }

    private void requireRequest(DonationRequest request) {
        require(request != null, "Donation request is required");
        require(request.getType() != null, "Donation type is required");
    }

    private static boolean isBlank(String text) {
        return text == null || text.isBlank();
    }

    private void require(boolean valid, String message) {
        if (!valid) {
            throw new IllegalArgumentException(message);
        }
    }
}
