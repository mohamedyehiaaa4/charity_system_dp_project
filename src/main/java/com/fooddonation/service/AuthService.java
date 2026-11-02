package com.fooddonation.service;

import com.fooddonation.model.user.Donor;
import com.fooddonation.model.user.DonorType;
import com.fooddonation.model.user.User;
import com.fooddonation.notification.ChannelType;
import com.fooddonation.persistence.UserRepository;

import java.util.Locale;
import java.util.Optional;

public class AuthService {
    private final UserRepository users;

    public AuthService(UserRepository users) {
        this.users = users;
    }

    public Donor register(String name, String email, String phone, String address, DonorType type,
                          ChannelType channel) {
        requireText(name, "Name");
        requireText(phone, "Phone");
        requireText(address, "Address");

        if (type == null) {
            throw new IllegalArgumentException("Donor type is required");
        }
        if (channel == null) {
            throw new IllegalArgumentException("Notification channel is required");
        }

        String cleanEmail = cleanEmail(email);
        if (users.findByEmail(cleanEmail).isPresent()) {
            throw new IllegalArgumentException("Email is already registered: " + cleanEmail);
        }

        return (Donor) users.save(new Donor(name.trim(), cleanEmail, phone.trim(), address.trim(), type, channel));
    }

    public Optional<User> login(String email) {
        if (email == null || email.isBlank()) {
            return  Optional.empty();
        }
        return users.findByEmail(email.trim().toLowerCase(Locale.ROOT));
    }

    public void updateProfile(User user) {
        User stored = users.findById(user.getId())
        .orElseThrow(() -> new IllegalArgumentException("User not found: " + user.getId()));

        requireText(user.getName(), "Name");
        String cleanEmail = cleanEmail(user.getEmail());
        Optional<User> emailOwner = users.findByEmail(cleanEmail);
        if (emailOwner.isPresent() && emailOwner.get().getId() != user.getId()) {
            throw new IllegalArgumentException("Email is already registered: " + cleanEmail);
        }

        if (user instanceof Donor donor) {
            requireText(donor.getPhone(), "Phone");
            requireText(donor.getAddress(), "Address");
            if (donor.getDonorType() == null) {
                throw new IllegalArgumentException("Donor type is required");
            }
        }

        user.setName(user.getName().trim());
        user.setEmail(cleanEmail);
        user.setRole(stored.getRole());
        if (user.getPreferredChannel() == null) {
            user.setPreferredChannel(stored.getPreferredChannel());
        }
        users.update(user);
    }

    private void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }

    private String cleanEmail(String email) {
        requireText(email, "Email");
        String clean = email.trim().toLowerCase(Locale.ROOT);
        if (!clean.matches("[^@\\s]+@[^@\\s]+")) {
            throw new IllegalArgumentException("Email is not valid: " + email);
        }
        return clean;
    }
}
