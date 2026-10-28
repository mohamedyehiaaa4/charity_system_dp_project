package com.fooddonation.notification;

import java.util.Map;

public class NotificationService {
    private final Map<ChannelType, NotificationStrategy> strategies;
    private NotificationStrategy strategy;

    public NotificationService(Map<ChannelType, NotificationStrategy> strategies) {
        this.strategies = strategies;
    }

    public void setStrategy(NotificationStrategy strategy) {
        throw new UnsupportedOperationException("TODO");
    }

    /** Sends using the current strategy. */
    public void notify(Recipient recipient, String subject, String message) {
        throw new UnsupportedOperationException("TODO");
    }

    /** Sends using the recipient's preferred channel. */
    public void notifyByPreference(Recipient recipient, String subject, String message) {
        throw new UnsupportedOperationException("TODO");
    }
}
