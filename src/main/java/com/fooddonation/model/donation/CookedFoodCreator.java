package com.fooddonation.model.donation;

final class CookedFoodCreator extends DonationCreator {
    @Override
    protected Donation createDonation(DonationRequest data) {
        return new CookedFoodDonation();
    }
}
