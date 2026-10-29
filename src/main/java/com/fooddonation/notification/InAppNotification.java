package com.fooddonation.notification;

import java.time.LocalDateTime;

/** Concrete strategy: puts the message in the recipient's in-app inbox. */
public class InAppNotification implements NotificationStrategy {
    private final InAppInbox inbox;

    public InAppNotification(InAppInbox inbox) {
        this.inbox = inbox;
    }

    @Override
    public void send(Recipient recipient, String subject, String message) {
        inbox.add(new InboxMessage(recipient.getId(), subject, message, LocalDateTime.now()));
    }
}
