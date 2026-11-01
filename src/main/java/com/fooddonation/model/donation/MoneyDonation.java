package com.fooddonation.model.donation;

import com.fooddonation.model.user.Donor;

import java.math.BigDecimal;
import java.time.LocalDate;

public class MoneyDonation extends Donation {
    private BigDecimal amount;

    MoneyDonation() {
        super(DonationType.MONEY);
    }

    MoneyDonation(Donor donor, BigDecimal amount) {
        super(donor, DonationType.MONEY, null);
        this.amount = amount;
    }

    @Override
    public String getDescription() {
        return "Money donation";
    }

    @Override
    public int getPriority() {
        return 1;
    }

    /** Money never has a food expiry, including when restored or updated through Donation. */
    @Override
    public void setExpiry(LocalDate expiry) {
        super.setExpiry(null);
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
