package com.fooddonation.model.receipt;

import com.fooddonation.model.donation.DonationType;
import com.fooddonation.model.user.Donor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Receipt for one accepted donation (DON-10). It is built from the saved donation whenever it is
 * asked for, so it is never stored. Getters only (it is immutable).
 */
public class DonationReceipt {
    static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final String receiptNumber;
    private final long donationId;
    private final Donor donor;
    private final DonationType type;
    private final String description;
    private final String status;
    private final String item;
    private final BigDecimal amount;
    private final LocalDateTime donatedAt;
    private final LocalDateTime issuedAt;

    /**
     * @param item   what was given, e.g. "5 kg of Rice and chicken"; null for money
     * @param amount the money given; null for food
     */
    public DonationReceipt(String receiptNumber, long donationId, Donor donor, DonationType type,
                           String description, String status, String item, BigDecimal amount,
                           LocalDateTime donatedAt, LocalDateTime issuedAt) {
        this.receiptNumber = receiptNumber;
        this.donationId = donationId;
        this.donor = donor;
        this.type = type;
        this.description = description;
        this.status = status;
        this.item = item;
        this.amount = amount;
        this.donatedAt = donatedAt;
        this.issuedAt = issuedAt;
    }

    /** The receipt as plain text, ready to show on screen or send as a message. */
    public String toText() {
        StringBuilder text = new StringBuilder();
        text.append("Donation receipt ").append(receiptNumber).append('\n');
        text.append("Issued: ").append(issuedAt.format(DATE_TIME)).append('\n');
        text.append("Donor: ").append(donorLine(donor)).append('\n');
        text.append("Donation #").append(donationId).append(" on ").append(donatedAt.format(DATE))
                .append(": ").append(description).append('\n');
        if (amount != null) {
            text.append("Amount: ").append(amount.toPlainString()).append('\n');
        } else {
            text.append("Item: ").append(item).append('\n');
        }
        text.append("Status: ").append(status);
        return text.toString();
    }

    static String donorLine(Donor donor) {
        StringBuilder line = new StringBuilder(donor.getName());
        if (donor.getEmail() != null) {
            line.append(" (").append(donor.getEmail()).append(')');
        }
        if (donor.getDonorType() != null) {
            line.append(", ").append(donor.getDonorType().name().toLowerCase());
        }
        return line.toString();
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public long getDonationId() {
        return donationId;
    }

    public Donor getDonor() {
        return donor;
    }

    public DonationType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public String getItem() {
        return item;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getDonatedAt() {
        return donatedAt;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }
}
