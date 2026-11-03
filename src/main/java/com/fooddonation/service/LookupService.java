package com.fooddonation.service;

import com.fooddonation.model.lookup.LookupEntry;
import com.fooddonation.model.lookup.LookupType;
import com.fooddonation.model.user.User;
import com.fooddonation.persistence.LookupRepository;

import java.util.List;

/**
 * DON-7: the lookup tables (food categories, storage types, donation statuses).
 * Controllers go through this instead of the repository, the same as every other screen.
 * The repository checks that the caller is a coordinator and that system rows are left alone.
 */
public class LookupService {
    private final LookupRepository lookups;

    public LookupService(LookupRepository lookups) {
        this.lookups = lookups;
    }

    public List<LookupEntry> getAll(LookupType type) {
        return lookups.readAll(type);
    }

    public LookupEntry create(User caller, LookupType type, String name) {
        return lookups.create(caller, type, name);
    }

    public void rename(User caller, LookupType type, long id, String newName) {
        lookups.update(caller, type, id, newName);
    }

    public void delete(User caller, LookupType type, long id) {
        lookups.delete(caller, type, id);
    }
}
