package com.fooddonation.notification;

import java.util.Map;

/**
 * Strategy pattern: the Context. It sends every message through a {@link NotificationStrategy}
 * and never knows which concrete channel it is using.
 */
public class NotificationService {
    private final Map<ChannelType, NotificationStrategy> strategies;
    private NotificationStrategy strategy;

    /**
     * @param strategies one strategy per channel
     * @throws IllegalArgumentException if there is no {@code IN_APP} strategy (it is the fallback)
     */
    public NotificationService(Map<ChannelType, NotificationStrategy> strategies) {
        if (!strategies.containsKey(ChannelType.IN_APP)) {
            throw new IllegalArgumentException("An IN_APP strategy is required as the fallback");
        }
        this.strategies = Map.copyOf(strategies);
    }

    /** Changes the strategy used by {@link #notify}. */
    public void setStrategy(NotificationStrategy strategy) {
        this.strategy = strategy;
    }

    /**
     * Sends using the current strategy.
     *
     * @throws IllegalStateException if no strategy has been set
     */
    public void notify(Recipient recipient, String subject, String message) {
        if (strategy == null) {
            throw new IllegalStateException("No notification strategy set");
        }
        strategy.send(recipient, subject, message);
    }

    /**
     * Sends using the recipient's preferred channel. Falls back to {@code IN_APP} if the
     * preference is missing or has no strategy. Does not change the current strategy.
     */
    public void notifyByPreference(Recipient recipient, String subject, String message) {
        ChannelType channel = recipient.getPreferredChannel();
        if (channel == null || !strategies.containsKey(channel)) {
            channel = ChannelType.IN_APP;
        }
        strategies.get(channel).send(recipient, subject, message);
    }
}
