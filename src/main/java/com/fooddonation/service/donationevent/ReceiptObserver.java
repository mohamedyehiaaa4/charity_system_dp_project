package com.fooddonation.service.donationevent;

import com.fooddonation.model.lookup.DonationStatus;
import com.fooddonation.model.receipt.DonationReceipt;
import com.fooddonation.notification.NotificationService;
import com.fooddonation.service.ReceiptService;

/** Sends the donor their receipt (DON-10) as soon as a donation is approved. */
public class ReceiptObserver implements DonationObserver {
    private final ReceiptService receipts;
    private final NotificationService notifications;

    public ReceiptObserver(ReceiptService receipts, NotificationService notifications) {
        this.receipts = receipts;
        this.notifications = notifications;
    }

    @Override
    public void update(DonationEvent event) {
        if (event.getType() == EventType.STATUS_CHANGED
                && DonationStatus.APPROVED.equals(event.getDonation().getStatus().getName())) {
            DonationReceipt receipt = receipts.receiptFor(event.getDonation());
            notifications.notifyByPreference(event.getDonation().getDonor().toRecipient(),
                    "Donation receipt " + receipt.getReceiptNumber(), receipt.toText());
        }
    }
}
