package com.fooddonation.model.donation;

final class FreshProduceCreator extends DonationCreator {
    @Override
    protected Donation createDonation(DonationRequest data) {
        return new FreshProduceDonation();
    }
}
