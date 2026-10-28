package com.fooddonation.service;

import com.fooddonation.model.user.Donor;
import com.fooddonation.model.user.DonorType;
import com.fooddonation.model.user.User;
import com.fooddonation.notification.ChannelType;
import com.fooddonation.persistence.UserRepository;

import java.util.Optional;

public class AuthService {
    private final UserRepository users;

    public AuthService(UserRepository users) {
        this.users = users;
    }

    public Donor register(String name, String email, String phone, String address, DonorType type,
                          ChannelType channel) {
        throw new UnsupportedOperationException("TODO");
    }

    public Optional<User> login(String email) {
        throw new UnsupportedOperationException("TODO");
    }

    public void updateProfile(User user) {
        throw new UnsupportedOperationException("TODO");
    }
}
