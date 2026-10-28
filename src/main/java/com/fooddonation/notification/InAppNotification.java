package com.fooddonation.notification;

public class InAppNotification implements NotificationStrategy {
    private final InAppInbox inbox;

    public InAppNotification(InAppInbox inbox) {
        this.inbox = inbox;
    }

    @Override
    public void send(Recipient recipient, String subject, String message) {
        throw new UnsupportedOperationException("TODO");
    }
}
