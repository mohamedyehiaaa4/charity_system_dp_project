package com.fooddonation.notification;

import java.nio.file.Path;

public class EmailNotification implements NotificationStrategy {
    private final Path logFile;

    public EmailNotification(Path logFile) {
        this.logFile = logFile;
    }

    @Override
    public void send(Recipient recipient, String subject, String message) {
        throw new UnsupportedOperationException("TODO");
    }
}
