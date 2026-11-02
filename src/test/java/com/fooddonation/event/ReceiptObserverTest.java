package com.fooddonation.event;

import com.fooddonation.model.donation.Donation;
import com.fooddonation.model.lookup.DonationStatus;
import com.fooddonation.model.receipt.DonationReceipt;
import com.fooddonation.model.user.Donor;
import com.fooddonation.notification.NotificationService;
import com.fooddonation.notification.Recipient;
import com.fooddonation.service.ReceiptService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReceiptObserverTest {
    ReceiptService receipts;
    NotificationService notifications;
    ReceiptObserver observer;
    Donation donation;
    Recipient recipient;

    @BeforeEach
    void setUp() {
        receipts = mock(ReceiptService.class);
        notifications = mock(NotificationService.class);
        observer = new ReceiptObserver(receipts, notifications);
        donation = mock(Donation.class);
        Donor donor = mock(Donor.class);
        recipient = mock(Recipient.class);
        when(donor.toRecipient()).thenReturn(recipient);
        when(donation.getDonor()).thenReturn(donor);
    }

    DonationEvent event(EventType type, String status) {
        when(donation.getStatus()).thenReturn(new DonationStatus(1, status, true));
        return new DonationEvent(type, donation, "message", LocalDateTime.of(2026, 11, 2, 12, 0));
    }

    @Test
    void approvalSendsTheReceiptToTheDonor() {
        DonationReceipt receipt = mock(DonationReceipt.class);
        when(receipt.getReceiptNumber()).thenReturn("R-2026-000042");
        when(receipt.toText()).thenReturn("receipt text");
        when(receipts.receiptFor(donation)).thenReturn(receipt);

        observer.update(event(EventType.STATUS_CHANGED, DonationStatus.APPROVED));

        verify(notifications).notifyByPreference(recipient, "Donation receipt R-2026-000042", "receipt text");
    }

    @Test
    void otherStatusChangesSendNoReceipt() {
        observer.update(event(EventType.STATUS_CHANGED, DonationStatus.PICKED_UP));
        observer.update(event(EventType.STATUS_CHANGED, DonationStatus.DELIVERED));

        verify(receipts, never()).receiptFor(any());
        verify(notifications, never()).notifyByPreference(any(), anyString(), anyString());
    }

    @Test
    void otherEventsSendNoReceipt() {
        observer.update(event(EventType.DONATION_SUBMITTED, DonationStatus.SUBMITTED));
        observer.update(event(EventType.DONATION_REJECTED, DonationStatus.REJECTED));
        observer.update(event(EventType.NEAR_EXPIRY, DonationStatus.APPROVED));

        verify(notifications, never()).notifyByPreference(any(), eq("Donation receipt"), anyString());
        verify(receipts, never()).receiptFor(any());
    }
}
