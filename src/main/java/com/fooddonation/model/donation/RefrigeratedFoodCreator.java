package com.fooddonation.model.donation;

final class RefrigeratedFoodCreator extends DonationCreator {
    @Override
    protected Donation createDonation(DonationRequest data) {
        return new RefrigeratedFoodDonation();
    }
}
