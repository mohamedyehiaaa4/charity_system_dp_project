package com.fooddonation.model.donation;

public class FreshProduceDonation extends FoodDonation {

    FreshProduceDonation() {
        super(DonationType.FRESH);
    }

    @Override
    public String getDescription() {
        return "Fresh produce donation";
    }

    @Override
    public int getPriority() {
        return 6;
    }
}
