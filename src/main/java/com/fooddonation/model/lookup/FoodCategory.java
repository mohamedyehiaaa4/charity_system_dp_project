package com.fooddonation.model.lookup;

public class FoodCategory extends LookupEntry {

    public FoodCategory() {
        super(LookupType.FOOD_CATEGORY);
    }

    public FoodCategory(long id, String name, boolean system) {
        super(id, LookupType.FOOD_CATEGORY, name, system);
    }
}
