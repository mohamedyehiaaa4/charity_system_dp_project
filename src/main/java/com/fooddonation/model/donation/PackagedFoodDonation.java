package com.fooddonation.model.donation;

public class PackagedFoodDonation extends FoodDonation {

    PackagedFoodDonation() {
        super(DonationType.PACKAGED);
    }

    @Override
    public String getDescription() {
        return "Packaged food donation";
    }

    @Override
    public int getPriority() {
        return 3;
    }
}
