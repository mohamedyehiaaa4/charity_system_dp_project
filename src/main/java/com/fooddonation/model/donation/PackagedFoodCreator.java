package com.fooddonation.model.donation;

final class PackagedFoodCreator extends DonationCreator {
    @Override
    protected Donation createDonation(DonationRequest data) {
        return new PackagedFoodDonation();
    }
}
