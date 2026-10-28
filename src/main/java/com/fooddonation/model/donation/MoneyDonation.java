package com.fooddonation.model.donation;

import com.fooddonation.model.user.Donor;

import java.math.BigDecimal;

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
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public int getPriority() {
        throw new UnsupportedOperationException("TODO");
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
