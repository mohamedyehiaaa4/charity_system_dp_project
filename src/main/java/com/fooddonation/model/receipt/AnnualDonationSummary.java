package com.fooddonation.model.receipt;

import com.fooddonation.model.user.Donor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * A donor's accepted donations in one calendar year, for tax purposes (DON-10).
 * Only money has a value; food is listed in kind. Getters only (it is immutable).
 */
public class AnnualDonationSummary {
    private final Donor donor;
    private final int year;
    private final List<DonationReceipt> receipts;
    private final LocalDateTime issuedAt;

    public AnnualDonationSummary(Donor donor, int year, List<DonationReceipt> receipts, LocalDateTime issuedAt) {
        this.donor = donor;
        this.year = year;
        this.receipts = List.copyOf(receipts);
        this.issuedAt = issuedAt;
    }

    /** Sum of all money donations in the year. */
    public BigDecimal getTotalMoney() {
        return receipts.stream()
                .map(DonationReceipt::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public long getFoodDonationCount() {
        return receipts.stream().filter(receipt -> receipt.getAmount() == null).count();
    }

    /** The summary as plain text, one line per receipt. */
    public String toText() {
        StringBuilder text = new StringBuilder();
        text.append("Annual donation summary ").append(year).append(" (for tax purposes)").append('\n');
        text.append("Donor: ").append(DonationReceipt.donorLine(donor)).append('\n');
        text.append("Issued: ").append(issuedAt.format(DonationReceipt.DATE_TIME)).append('\n');
        text.append("Accepted donations: ").append(receipts.size()).append('\n');
        text.append("Total money donated: ").append(getTotalMoney().toPlainString()).append('\n');
        text.append("Food donations (in kind, no money value): ").append(getFoodDonationCount()).append('\n');
        for (DonationReceipt receipt : receipts) {
            text.append("  ").append(receipt.getReceiptNumber())
                    .append("  ").append(receipt.getDonatedAt().format(DonationReceipt.DATE))
                    .append("  ").append(receipt.getDescription())
                    .append("  ").append(receipt.getAmount() != null
                            ? receipt.getAmount().toPlainString() : receipt.getItem())
                    .append('\n');
        }
        text.append("Rejected donations and donations still waiting for review are not included.");
        return text.toString();
    }

    public Donor getDonor() {
        return donor;
    }

    public int getYear() {
        return year;
    }

    public List<DonationReceipt> getReceipts() {
        return receipts;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }
}
