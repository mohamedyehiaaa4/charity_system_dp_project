package com.fooddonation.event;

import com.fooddonation.notification.NotificationService;
import com.fooddonation.notification.Recipient;

import java.util.List;

/** Tells every volunteer about a new donation, so someone can plan the pickup. */
public class VolunteerNotificationObserver implements DonationObserver {
    private final NotificationService notifications;
    private final List<Recipient> volunteers;

    public VolunteerNotificationObserver(NotificationService notifications, List<Recipient> volunteers) {
        this.notifications = notifications;
        this.volunteers = List.copyOf(volunteers);
    }

    @Override
    public void update(DonationEvent event) {
        if (event.getType() != EventType.DONATION_SUBMITTED) {
            return;
        }
        for (Recipient volunteer : volunteers) {
            notifications.notifyByPreference(volunteer, MessageTemplates.subject(event.getType()), event.getMessage());
        }
    }
}
