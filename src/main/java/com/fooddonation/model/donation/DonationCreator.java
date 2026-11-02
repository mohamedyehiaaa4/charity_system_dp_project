package com.fooddonation.model.donation;

/** Factory Method creator: subclasses choose the product used by the shared build operation. */
abstract class DonationCreator {

    final Donation buildDonation(DonationRequest data) {
        Donation donation = createDonation(data);
        donation.setDonor(data.getDonor());
        donation.setExpiry(data.getExpiry());
        if (donation instanceof FoodDonation food) {
            food.setFoodName(data.getFoodName());
            food.setQuantity(data.getQuantity() == null ? 0 : data.getQuantity());
            food.setUnit(data.getUnit());
            food.setCategory(data.getCategory());
            food.setStorageType(data.getStorageType());
            food.setPickupStart(data.getPickupStart());
            food.setPickupEnd(data.getPickupEnd());
            food.setPickupAddress(data.getPickupAddress());
        }
        return donation;
    }

    /** The factory method: concrete creators decide which Donation subclass to instantiate. */
    protected abstract Donation createDonation(DonationRequest data);
}
