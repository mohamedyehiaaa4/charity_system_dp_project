package com.fooddonation.persistence;

import com.fooddonation.model.lookup.LookupEntry;
import com.fooddonation.model.lookup.LookupType;
import com.fooddonation.model.user.User;

import java.util.List;
import java.util.Optional;

public class LookupRepository {
    private final DatabaseConnection db;

    public LookupRepository(DatabaseConnection db) {
        this.db = db;
    }

    public List<LookupEntry> readAll(LookupType type) {
        throw new UnsupportedOperationException("TODO");
    }

    public Optional<LookupEntry> findByName(LookupType type, String name) {
        throw new UnsupportedOperationException("TODO");
    }

    public LookupEntry create(User caller, LookupType type, String name) {
        throw new UnsupportedOperationException("TODO");
    }

    public void update(User caller, long id, String newName) {
        throw new UnsupportedOperationException("TODO");
    }

    public void delete(User caller, long id) {
        throw new UnsupportedOperationException("TODO");
    }
}
