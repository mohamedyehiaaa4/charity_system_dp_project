package com.fooddonation.notification;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

/** Concrete strategy: simulates an email by appending it to a log file. */
public class EmailNotification implements NotificationStrategy {
    private final Path logFile;

    public EmailNotification(Path logFile) {
        this.logFile = logFile;
    }

    @Override
    public synchronized void send(Recipient recipient, String subject, String message) {
        String line = LocalDateTime.now() + " | to: " + recipient.getEmail()
                + " | subject: " + subject + " | " + message + System.lineSeparator();
        try {
            if (logFile.getParent() != null) {
                Files.createDirectories(logFile.getParent());
            }
            Files.writeString(logFile, line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not write email log", e);
        }
    }
}
