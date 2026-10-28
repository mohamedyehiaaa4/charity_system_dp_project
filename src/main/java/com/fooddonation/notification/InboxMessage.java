package com.fooddonation.notification;

import java.time.LocalDateTime;

public class InboxMessage {
    private String recipientId;
    private String subject;
    private String message;
    private LocalDateTime sentAt;
    private boolean read;

    public InboxMessage() {
    }

    public InboxMessage(String recipientId, String subject, String message, LocalDateTime sentAt) {
        this.recipientId = recipientId;
        this.subject = subject;
        this.message = message;
        this.sentAt = sentAt;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}
