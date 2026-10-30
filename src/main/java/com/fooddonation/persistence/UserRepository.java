package com.fooddonation.persistence;

import com.fooddonation.model.user.Coordinator;
import com.fooddonation.model.user.Donor;
import com.fooddonation.model.user.DonorType;
import com.fooddonation.model.user.Role;
import com.fooddonation.model.user.User;
import com.fooddonation.notification.ChannelType;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class UserRepository {
    private final DatabaseConnection db;

    public UserRepository(DatabaseConnection db) {
        this.db = db;
    }

    public User save(User user) {
        String sql = "INSERT INTO users (name, email, phone, address, role, donor_type, preferred_channel) "
        + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        synchronized (db) {
            try (PreparedStatement statement = db.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, user.getName());
                statement.setString(2, user.getEmail());
                statement.setString(3, user.getPhone());
                statement.setString(4, user.getAddress());
                statement.setString(5, user.getRole().name());
                statement.setString(6, donorTypeName(user));
                statement.setString(7, user.getPreferredChannel().name());
                statement.executeUpdate();

                try (ResultSet keys = statement.getGeneratedKeys()) {
                    keys.next();
                    user.setId(keys.getLong(1));
                }
                return user;   
            } catch (SQLException e) {
                throw new RuntimeException("Could not save user " + user.getEmail(), e);
            }
        }
    }

    public Optional<User> findById(long id) {
        synchronized (db) {
            try (PreparedStatement statement = db.getConnection().prepareStatement("SELECT * FROM users WHERE id = ?")) {
                statement.setLong(1, id);
                try (ResultSet row = statement.executeQuery()) {
                    return row.next() ? Optional.of(toUser(row)) : Optional.empty();
                }
            } catch (SQLException e) {
                throw new RuntimeException("Could not load user" + id, e);
            }
        }
    }

    public Optional<User> findByEmail(String email) {
        synchronized (db) {
            try (PreparedStatement statement = db.getConnection().prepareStatement("SELECT * FROM users WHERE email = ?")) {
                statement.setString(1, email);
                try (ResultSet row = statement.executeQuery()) {
                    return row.next() ? Optional.of(toUser(row)) : Optional.empty();
                }
            } catch (SQLException e) {
                throw new RuntimeException("Could not load user" + email, e);
            }
        }
    }

    public void update(User user) {
        String sql = "UPDATE users SET name = ?, email = ?, phone = ?, address = ?, donor_type = ?, "
        + "preferred_channel = ? WHERE id = ?";

        synchronized (db) {
            try (PreparedStatement statement = db.getConnection().prepareStatement(sql)) {
                statement.setString(1, user.getName());
                statement.setString(2, user.getEmail());
                statement.setString(3, user.getPhone());
                statement.setString(4, user.getAddress());
                statement.setString(5, donorTypeName(user));
                statement.setString(6, user.getPreferredChannel().name());
                statement.setLong(7, user.getId());
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Could not update user " + user.getId(), e);
            }
        }
    }

    private String donorTypeName(User user) {
        return user instanceof Donor donor ? donor.getDonorType().name() : null;
    }

    private User toUser(ResultSet row) throws SQLException {
        User user;

        if (Role.valueOf(row.getString("role")) == Role.COORDINATOR) {
            user = new Coordinator();
        } else {
            Donor donor = new Donor();
            donor.setDonorType(DonorType.valueOf(row.getString("donor_type")));
            user = donor;
        }
        user.setId(row.getLong("id"));
        user.setName(row.getString("name"));
        user.setEmail(row.getString("email"));
        user.setPhone(row.getString("phone"));
        user.setAddress(row.getString("address"));
        user.setPreferredChannel(ChannelType.valueOf(row.getString("preferred_channel")));

        return user;
    }
}
