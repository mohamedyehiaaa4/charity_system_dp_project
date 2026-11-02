package com.fooddonation.model.donation;

public class CookedFoodDonation extends FoodDonation {

    CookedFoodDonation() {
        super(DonationType.COOKED);
    }

    @Override
    public String getDescription() {
        return "Cooked food donation";
    }

    @Override
    public int getPriority() {
        return 8;
    }
}
