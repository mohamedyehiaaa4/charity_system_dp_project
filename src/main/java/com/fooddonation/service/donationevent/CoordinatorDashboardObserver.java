package com.fooddonation.service.donationevent;

import com.fooddonation.model.user.User;
import com.fooddonation.notification.NotificationService;
import com.fooddonation.persistence.UserRepository;

/** Tells the coordinator about new donations and donations near expiry. */
public class CoordinatorDashboardObserver implements DonationObserver {
    private final NotificationService notifications;
    private final UserRepository users;
    private final String coordinatorEmail;

    public CoordinatorDashboardObserver(NotificationService notifications, UserRepository users,
                                        String coordinatorEmail) {
        this.notifications = notifications;
        this.users = users;
        this.coordinatorEmail = coordinatorEmail;
    }

    @Override
    public void update(DonationEvent event) {
        if (event.getType() != EventType.DONATION_SUBMITTED && event.getType() != EventType.NEAR_EXPIRY) {
            return;
        }
        // looked up each time so a changed notification preference is used straight away
        User coordinator = users.findByEmail(coordinatorEmail)
                .orElseThrow(() -> new IllegalStateException("No coordinator with email " + coordinatorEmail));
        notifications.notifyByPreference(coordinator.toRecipient(),
                MessageTemplates.subject(event.getType()), event.getMessage());
    }
}
