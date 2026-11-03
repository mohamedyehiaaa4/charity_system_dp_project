package com.fooddonation.service.donationevent;

import com.fooddonation.notification.NotificationService;

/** Tells the donor when their donation's status changes or it is rejected (with the reason). */
public class DonorNotificationObserver implements DonationObserver {
    private final NotificationService notifications;

    public DonorNotificationObserver(NotificationService notifications) {
        this.notifications = notifications;
    }

    @Override
    public void update(DonationEvent event) {
        if (event.getType() == EventType.STATUS_CHANGED || event.getType() == EventType.DONATION_REJECTED) {
            notifications.notifyByPreference(event.getDonation().getDonor().toRecipient(),
                    MessageTemplates.subject(event.getType()), event.getMessage());
        }
    }
}
