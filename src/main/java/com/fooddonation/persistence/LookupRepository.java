package com.fooddonation.persistence;

import com.fooddonation.model.lookup.DonationStatus;
import com.fooddonation.model.lookup.FoodCategory;
import com.fooddonation.model.lookup.LookupEntry;
import com.fooddonation.model.lookup.LookupType;
import com.fooddonation.model.lookup.StorageType;
import com.fooddonation.model.user.Role;
import com.fooddonation.model.user.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LookupRepository {
    private final DatabaseConnection db;

    public LookupRepository(DatabaseConnection db) {
        this.db = db;
    }

    public List<LookupEntry> readAll(LookupType type) {
        String sql = "SELECT id, name, is_system FROM " + tableFor(type) + " ORDER BY id";
        synchronized (db) {
            try (PreparedStatement statement = db.getConnection().prepareStatement(sql);
                 ResultSet row = statement.executeQuery()) {
                List<LookupEntry> entries = new ArrayList<>();
                while (row.next()) {
                    entries.add(toEntry(type, row));
                }
                return entries;
            } catch (SQLException e) {
                throw new RuntimeException("Could not read " + type, e);
            }
        }
    }

    public Optional<LookupEntry> findByName(LookupType type, String name) {
        String sql = "SELECT id, name, is_system FROM " + tableFor(type) + " WHERE name = ? COLLATE NOCASE";
        synchronized (db) {
            try (PreparedStatement statement = db.getConnection().prepareStatement(sql)) {
                statement.setString(1, name);
                try (ResultSet row = statement.executeQuery()) {
                    return row.next() ? Optional.of(toEntry(type, row)) : Optional.empty();
                }
            } catch (SQLException e) {
                throw new RuntimeException("Could not read " + type, e);
            }
        }
    }

    public LookupEntry create(User caller, LookupType type, String name) {
        requireCoordinator(caller);
        String cleanName = cleanName(name);
        String sql = "INSERT INTO " + tableFor(type) + " (name) VALUES (?)";
        synchronized (db) {
            if (findByName(type, cleanName).isPresent()) {
                throw new IllegalArgumentException("Name already exists: " + cleanName);
            }
            try (PreparedStatement statement =
                         db.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, cleanName);
                statement.executeUpdate();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    keys.next();
                    return newEntry(type, keys.getLong(1), cleanName, false);
                }
            } catch (SQLException e) {
                throw new RuntimeException("Could not create " + cleanName, e);
            }
        }
    }

    public void update(User caller, LookupType type, long id, String newName) {
        requireCoordinator(caller);
        String cleanName = cleanName(newName);
        String sql = "UPDATE " + tableFor(type) + " SET name = ? WHERE id = ?";
        synchronized (db) {
            requireEditable(type, id);
            Optional<LookupEntry> sameName = findByName(type, cleanName);
            if (sameName.isPresent() && sameName.get().getId() != id) {
                throw new IllegalArgumentException("Name already exists: " + cleanName);
            }
            try (PreparedStatement statement = db.getConnection().prepareStatement(sql)) {
                statement.setString(1, cleanName);
                statement.setLong(2, id);
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Could not update entry " + id, e);
            }
        }
    }

    public void delete(User caller, LookupType type, long id) {
        requireCoordinator(caller);
        String sql = "DELETE FROM " + tableFor(type) + " WHERE id = ?";
        synchronized (db) {
            requireEditable(type, id);
            if (isUsedByDonation(type, id)) {
                throw new IllegalArgumentException("Entry is used by a donation and can't be deleted");
            }
            try (PreparedStatement statement = db.getConnection().prepareStatement(sql)) {
                statement.setLong(1, id);
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Could not delete entry " + id, e);
            }
        }
    }

    private void requireCoordinator(User caller) {
        if (caller == null || caller.getRole() != Role.COORDINATOR) {
            throw new SecurityException("Only coordinators can manage lookups");
        }
    }

    private String cleanName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
        return name.trim();
    }

    /** Throws unless the row exists and isn't one of the protected system rows. */
    private void requireEditable(LookupType type, long id) {
        String sql = "SELECT is_system FROM " + tableFor(type) + " WHERE id = ?";
        try (PreparedStatement statement = db.getConnection().prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet row = statement.executeQuery()) {
                if (!row.next()) {
                    throw new IllegalArgumentException("Entry not found: " + id);
                }
                if (row.getBoolean("is_system")) {
                    throw new IllegalArgumentException("System entries can't be changed");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not read entry " + id, e);
        }
    }

    private boolean isUsedByDonation(LookupType type, long id) {
        String sql = "SELECT 1 FROM donations WHERE " + donationColumnFor(type) + " = ? LIMIT 1";
        try (PreparedStatement statement = db.getConnection().prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet row = statement.executeQuery()) {
                return row.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not check entry " + id, e);
        }
    }

    private LookupEntry toEntry(LookupType type, ResultSet row) throws SQLException {
        return newEntry(type, row.getLong("id"), row.getString("name"), row.getBoolean("is_system"));
    }

    // Returns the real subclass, so callers can cast to FoodCategory, StorageType or DonationStatus.
    private LookupEntry newEntry(LookupType type, long id, String name, boolean system) {
        return switch (type) {
            case FOOD_CATEGORY -> new FoodCategory(id, name, system);
            case STORAGE_TYPE -> new StorageType(id, name, system);
            case DONATION_STATUS -> new DonationStatus(id, name, system);
        };
    }

    // Table and column names can't be SQL parameters. These come from our enum, never from user input.
    private String tableFor(LookupType type) {
        return switch (type) {
            case FOOD_CATEGORY -> "food_categories";
            case STORAGE_TYPE -> "storage_types";
            case DONATION_STATUS -> "donation_statuses";
        };
    }

    private String donationColumnFor(LookupType type) {
        return switch (type) {
            case FOOD_CATEGORY -> "category_id";
            case STORAGE_TYPE -> "storage_type_id";
            case DONATION_STATUS -> "status_id";
        };
    }
}
