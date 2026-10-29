package com.fooddonation.notification;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

/** Concrete strategy: simulates an SMS by appending it to a log file. */
public class SmsNotification implements NotificationStrategy {
    private final Path logFile;

    public SmsNotification(Path logFile) {
        this.logFile = logFile;
    }

    @Override
    public synchronized void send(Recipient recipient, String subject, String message) {
        String line = LocalDateTime.now() + " | to: " + recipient.getPhone()
                + " | subject: " + subject + " | " + message + System.lineSeparator();
        try {
            if (logFile.getParent() != null) {
                Files.createDirectories(logFile.getParent());
            }
            Files.writeString(logFile, line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not write SMS log", e);
        }
    }
}
