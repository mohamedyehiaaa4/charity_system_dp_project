package com.fooddonation.persistence;

import com.fooddonation.model.user.User;

import java.util.Optional;

public class UserRepository {
    private final DatabaseConnection db;

    public UserRepository(DatabaseConnection db) {
        this.db = db;
    }

    public User save(User user) {
        throw new UnsupportedOperationException("TODO");
    }

    public Optional<User> findById(long id) {
        throw new UnsupportedOperationException("TODO");
    }

    public Optional<User> findByEmail(String email) {
        throw new UnsupportedOperationException("TODO");
    }

    public void update(User user) {
        throw new UnsupportedOperationException("TODO");
    }
}
