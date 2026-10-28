package com.fooddonation.model.lookup;

public class DonationStatus extends LookupEntry {
    // Names of the five system statuses; the lifecycle checks status by these names.
    public static final String SUBMITTED = "Submitted";
    public static final String APPROVED = "Approved";
    public static final String REJECTED = "Rejected";
    public static final String PICKED_UP = "Picked up";
    public static final String DELIVERED = "Delivered";

    public DonationStatus() {
        super(LookupType.DONATION_STATUS);
    }

    public DonationStatus(long id, String name, boolean system) {
        super(id, LookupType.DONATION_STATUS, name, system);
    }
}
