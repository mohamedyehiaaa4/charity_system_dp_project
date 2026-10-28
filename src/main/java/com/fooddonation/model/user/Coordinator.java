package com.fooddonation.model.user;

import com.fooddonation.notification.ChannelType;

public class Coordinator extends User {

    public Coordinator() {
        super(Role.COORDINATOR);
    }

    public Coordinator(String name, String email, String phone, String address, ChannelType preferredChannel) {
        super(name, email, phone, address, Role.COORDINATOR, preferredChannel);
    }
}
