package com.fooddonation.model.donation;

public class RefrigeratedFoodDonation extends FoodDonation {

    RefrigeratedFoodDonation() {
        super(DonationType.REFRIGERATED);
    }

    @Override
    public String getDescription() {
        return "Refrigerated food donation";
    }

    @Override
    public int getPriority() {
        return 7;
    }
}
