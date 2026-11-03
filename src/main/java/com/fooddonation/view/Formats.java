package com.fooddonation.ui;

import com.fooddonation.model.donation.Donation;
import com.fooddonation.model.donation.FoodDonation;
import com.fooddonation.model.donation.MoneyDonation;
import com.fooddonation.model.donation.addon.DonationDecorator;
import com.fooddonation.model.lookup.DonationStatus;
import com.fooddonation.model.lookup.LookupEntry;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** How model values are shown on screen. Display rules live here, not in the model or the controllers. */
public final class Formats {
    public static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    public static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    private Formats() {
    }

    /** HOME_PICKUP becomes "Home pickup". */
    public static String label(Enum<?> value) {
        if (value == null) {
            return "";
        }
        String text = value.name().replace('_', ' ').toLowerCase();
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    public static String label(LookupEntry entry) {
        return entry == null ? "" : entry.getName();
    }

    public static String date(LocalDate date) {
        return date == null ? "" : date.format(DATE);
    }

    public static String dateTime(LocalDateTime time) {
        return time == null ? "" : time.format(DATE_TIME);
    }

    /** "5 kg of Bread" for food, the amount for money. */
    public static String details(Donation donation) {
        Donation base = donation.getBaseDonation();
        if (base instanceof FoodDonation food) {
            String quantity = BigDecimal.valueOf(food.getQuantity()).stripTrailingZeros().toPlainString();
            String unit = food.getUnit() == null ? "" : food.getUnit().name().toLowerCase();
            return quantity + " " + unit + " of " + (food.getFoodName() == null ? "food" : food.getFoodName());
        }
        if (base instanceof MoneyDonation money && money.getAmount() != null) {
            return money.getAmount().toPlainString();
        }
        return "";
    }

    /** Pickup window and address for food; empty for money. */
    public static String pickup(Donation donation) {
        if (!(donation.getBaseDonation() instanceof FoodDonation food) || food.getPickupStart() == null) {
            return "";
        }
        String end = food.getPickupEnd() == null ? "" : food.getPickupEnd().format(TIME);
        String address = food.getPickupAddress() == null ? "" : " at " + food.getPickupAddress();
        return food.getPickupStart().format(DATE_TIME) + "–" + end + address;
    }

    public static String status(Donation donation) {
        return donation.getStatus() == null ? "" : donation.getStatus().getName();
    }

    /** "Rice and chicken · 5 kg" for food, "Money · 250" for money. */
    public static String what(Donation donation) {
        Donation base = donation.getBaseDonation();
        if (base instanceof FoodDonation food) {
            String quantity = BigDecimal.valueOf(food.getQuantity()).stripTrailingZeros().toPlainString();
            String unit = food.getUnit() == null ? "" : " " + food.getUnit().name().toLowerCase();
            return (food.getFoodName() == null ? "Food" : food.getFoodName()) + " · " + quantity + unit;
        }
        if (base instanceof MoneyDonation money && money.getAmount() != null) {
            return "Money · " + money.getAmount().toPlainString();
        }
        return "";
    }

    /** The add-ons on a donation as screen labels, e.g. ["Home pickup", "Urgent priority"]. */
    public static List<String> addOns(Donation donation) {
        return DonationDecorator.collectAddOns(donation).stream().map(Formats::label).toList();
    }

    /** Style class for the status badge, so each lifecycle status has its own colour. */
    public static String statusStyle(Donation donation) {
        return switch (status(donation)) {
            case DonationStatus.SUBMITTED -> "s-submitted";
            case DonationStatus.APPROVED -> "s-approved";
            case DonationStatus.PICKED_UP -> "s-picked";
            case DonationStatus.DELIVERED -> "s-delivered";
            case DonationStatus.REJECTED -> "s-rejected";
            default -> "s-other";
        };
    }

    /** An active donation whose expiry day is today, tomorrow or the day after (the 48-hour warning window). */
    public static boolean nearExpiry(Donation donation, LocalDate today) {
        String status = status(donation);
        LocalDate expiry = donation.getExpiry();
        return expiry != null && !expiry.isBefore(today) && !expiry.isAfter(today.plusDays(2))
                && !DonationStatus.REJECTED.equals(status) && !DonationStatus.DELIVERED.equals(status);
    }
}
