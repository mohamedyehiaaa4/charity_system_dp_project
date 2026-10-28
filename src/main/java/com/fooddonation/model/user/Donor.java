package com.fooddonation.model.user;

import com.fooddonation.notification.ChannelType;

public class Donor extends User {
    private DonorType donorType;

    public Donor() {
        super(Role.DONOR);
    }

    public Donor(String name, String email, String phone, String address, DonorType donorType,
                 ChannelType preferredChannel) {
        super(name, email, phone, address, Role.DONOR, preferredChannel);
        this.donorType = donorType;
    }

    public DonorType getDonorType() {
        return donorType;
    }

    public void setDonorType(DonorType donorType) {
        this.donorType = donorType;
    }
}
