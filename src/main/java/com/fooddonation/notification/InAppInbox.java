package com.fooddonation.notification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores in-app messages per recipient, in memory. Methods are synchronized because the
 * expiry scheduler sends from its own thread.
 */
public class InAppInbox {
    private final Map<String, List<InboxMessage>> messages = new HashMap<>();

    public synchronized void add(InboxMessage message) {
        messages.computeIfAbsent(message.getRecipientId(), id -> new ArrayList<>()).add(message);
    }

    /** Returns a copy of this recipient's messages, oldest first; empty if there are none. */
    public synchronized List<InboxMessage> getMessages(String recipientId) {
        return new ArrayList<>(messages.getOrDefault(recipientId, List.of()));
    }
}
