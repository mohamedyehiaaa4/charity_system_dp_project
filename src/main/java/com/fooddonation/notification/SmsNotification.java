package com.fooddonation.notification;

import java.nio.file.Path;

public class SmsNotification implements NotificationStrategy {
    private final Path logFile;

    public SmsNotification(Path logFile) {
        this.logFile = logFile;
    }

    @Override
    public void send(Recipient recipient, String subject, String message) {
        throw new UnsupportedOperationException("TODO");
    }
}
