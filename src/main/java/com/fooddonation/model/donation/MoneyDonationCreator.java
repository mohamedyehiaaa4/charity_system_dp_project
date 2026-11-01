package com.fooddonation.model.donation;

final class MoneyDonationCreator extends DonationCreator {
    @Override
    protected Donation createDonation(DonationRequest data) {
        return new MoneyDonation(data.getDonor(), data.getAmount());
    }
}
