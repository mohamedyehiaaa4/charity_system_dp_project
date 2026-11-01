package com.fooddonation.model.donation;

import com.fooddonation.addon.AddOn;
import com.fooddonation.addon.DonationDecorator;
import com.fooddonation.addon.HomePickup;
import com.fooddonation.addon.RefrigeratedTransport;
import com.fooddonation.addon.SpecialPackaging;
import com.fooddonation.addon.UrgentPriority;
import com.fooddonation.model.lookup.DonationStatus;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;

public class DonationFactory {

    public Donation createDonation(DonationRequest request) {
        requireRequest(request);
        require(request.getDonor() != null, "Donor is required");
        if (request.getType() == DonationType.MONEY) {
            require(request.getAmount() != null, "Amount is required");
            require(request.getAmount().signum() > 0, "Amount must be greater than zero");
        } else {
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
        if (data.getType() == DonationType.MONEY) {
            return new MoneyDonation(data.getDonor(), data.getAmount());
        }
        FoodDonation food = switch (data.getType()) {
            case COOKED -> new CookedFoodDonation();
            case REFRIGERATED -> new RefrigeratedFoodDonation();
            case FRESH -> new FreshProduceDonation();
            case PACKAGED -> new PackagedFoodDonation();
            case MONEY -> throw new IllegalArgumentException("Expected a food donation type");
        };
        food.setDonor(data.getDonor());
        food.setExpiry(data.getExpiry());
        food.setQuantity(data.getQuantity() == null ? 0 : data.getQuantity());
        food.setUnit(data.getUnit());
        food.setCategory(data.getCategory());
        food.setStorageType(data.getStorageType());
        food.setPickupStart(data.getPickupStart());
        food.setPickupEnd(data.getPickupEnd());
        return food;
    }

    private void requireRequest(DonationRequest request) {
        require(request != null, "Donation request is required");
        require(request.getType() != null, "Donation type is required");
    }

    private void require(boolean valid, String message) {
        if (!valid) {
            throw new IllegalArgumentException(message);
        }
    }
}
