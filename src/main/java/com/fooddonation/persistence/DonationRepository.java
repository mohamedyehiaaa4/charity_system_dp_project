package com.fooddonation.persistence;

import com.fooddonation.addon.AddOn;
import com.fooddonation.addon.DonationDecorator;
import com.fooddonation.model.donation.Donation;
import com.fooddonation.model.donation.DonationFactory;
import com.fooddonation.model.donation.DonationRequest;
import com.fooddonation.model.donation.DonationType;
import com.fooddonation.model.donation.FoodDonation;
import com.fooddonation.model.donation.MoneyDonation;
import com.fooddonation.model.donation.Unit;
import com.fooddonation.model.lookup.DonationStatus;
import com.fooddonation.model.lookup.FoodCategory;
import com.fooddonation.model.lookup.StorageType;
import com.fooddonation.model.user.Donor;
import com.fooddonation.model.user.DonorType;
import com.fooddonation.notification.ChannelType;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class DonationRepository {
    // Joins the donor and lookup tables so a loaded donation has their names, not just their IDs
    private static final String SELECT = "SELECT d.*, "
            + "u.name AS donor_name, u.email AS donor_email, u.phone AS donor_phone, u.address AS donor_address, "
            + "u.donor_type AS donor_type, u.preferred_channel AS donor_channel, "
            + "s.name AS status_name, s.is_system AS status_system, "
            + "c.name AS category_name, c.is_system AS category_system, "
            + "t.name AS storage_name, t.is_system AS storage_system "
            + "FROM donations d "
            + "JOIN users u ON u.id = d.donor_id "
            + "JOIN donation_statuses s ON s.id = d.status_id "
            + "LEFT JOIN food_categories c ON c.id = d.category_id "
            + "LEFT JOIN storage_types t ON t.id = d.storage_type_id ";

    private static final String INSERT = "INSERT INTO donations (donor_id, type, status_id, created_at, "
            + "rejection_reason, expiry, quantity, unit, category_id, storage_type_id, pickup_start, pickup_end, "
            + "amount, add_ons) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private final DatabaseConnection db;
    private final DonationFactory factory;

    public DonationRepository(DatabaseConnection db, DonationFactory factory) {
        this.db = db;
        this.factory = factory;
    }

    /** Returns the donation with its new ID. */
    public Donation save(Donation donation) {
        // add-on wrappers don't have the food fields, so read them from the real donation inside
        Donation base = DonationDecorator.unwrap(donation);
        if (base.getDonor() == null || base.getStatus() == null || base.getCreatedAt() == null) {
            throw new IllegalArgumentException("A donation needs a donor, a status and a creation time");
        }

        Double quantity = null;
        String unit = null;
        Long categoryId = null;
        Long storageTypeId = null;
        String pickupStart = null;
        String pickupEnd = null;
        if (base instanceof FoodDonation food) {
            quantity = food.getQuantity();
            unit = nameOf(food.getUnit());
            categoryId = food.getCategory() == null ? null : food.getCategory().getId();
            storageTypeId = food.getStorageType() == null ? null : food.getStorageType().getId();
            pickupStart = textOf(food.getPickupStart());
            pickupEnd = textOf(food.getPickupEnd());
        }
        String amount = null;
        if (base instanceof MoneyDonation money && money.getAmount() != null) {
            amount = money.getAmount().toPlainString();
        }

        synchronized (db) {
            try (PreparedStatement st = db.getConnection().prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
                st.setLong(1, base.getDonor().getId());
                st.setString(2, base.getType().name());
                st.setLong(3, base.getStatus().getId());
                st.setString(4, base.getCreatedAt().toString());
                st.setObject(5, base.getRejectionReason());
                st.setObject(6, textOf(base.getExpiry()));
                st.setObject(7, quantity);
                st.setObject(8, unit);
                st.setObject(9, categoryId);
                st.setObject(10, storageTypeId);
                st.setObject(11, pickupStart);
                st.setObject(12, pickupEnd);
                st.setObject(13, amount);
                st.setString(14, addOnsText(DonationDecorator.collectAddOns(donation)));
                st.executeUpdate();
                try (ResultSet keys = st.getGeneratedKeys()) {
                    keys.next();
                    donation.setId(keys.getLong(1));
                }
                return donation;
            } catch (SQLException e) {
                throw new IllegalStateException("Could not save donation", e);
            }
        }
    }

    public Optional<Donation> findById(long id) {
        return query(SELECT + "WHERE d.id = ?", id).stream().findFirst();
    }

    public List<Donation> findAll() {
        return query(SELECT + "ORDER BY d.id");
    }

    public List<Donation> findByDonor(long donorId) {
        return query(SELECT + "WHERE d.donor_id = ? ORDER BY d.id", donorId);
    }

    public List<Donation> findByStatus(DonationStatus status) {
        return query(SELECT + "WHERE d.status_id = ? ORDER BY d.id", status.getId());
    }

    public void updateStatus(long donationId, DonationStatus status, String rejectionReason) {
        synchronized (db) {
            try (PreparedStatement st = db.getConnection().prepareStatement(
                    "UPDATE donations SET status_id = ?, rejection_reason = ? WHERE id = ?")) {
                st.setLong(1, status.getId());
                st.setObject(2, rejectionReason);
                st.setLong(3, donationId);
                if (st.executeUpdate() == 0) {
                    throw new IllegalArgumentException("No donation with id " + donationId);
                }
            } catch (SQLException e) {
                throw new IllegalStateException("Could not update donation " + donationId, e);
            }
        }
    }

    private List<Donation> query(String sql, Object... params) {
        synchronized (db) {
            try (PreparedStatement st = db.getConnection().prepareStatement(sql)) {
                for (int i = 0; i < params.length; i++) {
                    st.setObject(i + 1, params[i]);
                }
                try (ResultSet rs = st.executeQuery()) {
                    List<Donation> result = new ArrayList<>();
                    while (rs.next()) {
                        result.add(fromRow(rs));
                    }
                    return result;
                }
            } catch (SQLException e) {
                throw new IllegalStateException("Could not load donations", e);
            }
        }
    }

    /** Turns one row back into a donation through the factory, so ID, status and date are kept. */
    private Donation fromRow(ResultSet rs) throws SQLException {
        Donor donor = new Donor(rs.getString("donor_name"), rs.getString("donor_email"),
                rs.getString("donor_phone"), rs.getString("donor_address"),
                enumOf(DonorType.class, rs.getString("donor_type")),
                enumOf(ChannelType.class, rs.getString("donor_channel")));
        donor.setId(rs.getLong("donor_id"));

        DonationRequest data = new DonationRequest();
        data.setType(DonationType.valueOf(rs.getString("type")));
        data.setDonor(donor);
        String expiry = rs.getString("expiry");
        data.setExpiry(expiry == null ? null : LocalDate.parse(expiry));
        double quantity = rs.getDouble("quantity");
        data.setQuantity(rs.wasNull() ? null : quantity);
        data.setUnit(enumOf(Unit.class, rs.getString("unit")));
        long categoryId = rs.getLong("category_id");
        if (!rs.wasNull()) {
            data.setCategory(new FoodCategory(categoryId, rs.getString("category_name"),
                    rs.getBoolean("category_system")));
        }
        long storageTypeId = rs.getLong("storage_type_id");
        if (!rs.wasNull()) {
            data.setStorageType(new StorageType(storageTypeId, rs.getString("storage_name"),
                    rs.getBoolean("storage_system")));
        }
        String pickupStart = rs.getString("pickup_start");
        data.setPickupStart(pickupStart == null ? null : LocalDateTime.parse(pickupStart));
        String pickupEnd = rs.getString("pickup_end");
        data.setPickupEnd(pickupEnd == null ? null : LocalDateTime.parse(pickupEnd));
        String amount = rs.getString("amount");
        data.setAmount(amount == null ? null : new BigDecimal(amount));

        DonationStatus status = new DonationStatus(rs.getLong("status_id"), rs.getString("status_name"),
                rs.getBoolean("status_system"));
        return factory.restore(data, rs.getLong("id"), status, LocalDateTime.parse(rs.getString("created_at")),
                rs.getString("rejection_reason"), parseAddOns(rs.getString("add_ons")));
    }

    /** Add-ons are stored as enum names in enum order, e.g. "HOME_PICKUP,URGENT_PRIORITY". */
    private static String addOnsText(Set<AddOn> addOns) {
        return addOns.stream().sorted().map(Enum::name).collect(Collectors.joining(","));
    }

    private static Set<AddOn> parseAddOns(String text) {
        Set<AddOn> addOns = EnumSet.noneOf(AddOn.class);
        if (text != null && !text.isBlank()) {
            for (String name : text.split(",")) {
                addOns.add(AddOn.valueOf(name.trim()));
            }
        }
        return addOns;
    }

    private static <E extends Enum<E>> E enumOf(Class<E> type, String name) {
        return name == null ? null : Enum.valueOf(type, name);
    }

    private static String nameOf(Enum<?> value) {
        return value == null ? null : value.name();
    }

    private static String textOf(Object value) {
        return value == null ? null : value.toString();
    }
}
