package com.fooddonation.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotificationChannelsTest {
    private final Recipient ali = new Recipient("1", "Ali", "ali@example.com", "0100", ChannelType.IN_APP);
    private final Recipient sara = new Recipient("2", "Sara", "sara@example.com", "0111", ChannelType.IN_APP);

    @Test
    void inAppMessagesAreStoredPerRecipient() {
        InAppInbox inbox = new InAppInbox();
        InAppNotification inApp = new InAppNotification(inbox);

        inApp.send(ali, "Approved", "Your donation was approved");

        List<InboxMessage> aliMessages = inbox.getMessages("1");
        assertEquals(1, aliMessages.size());
        assertEquals("Approved", aliMessages.get(0).getSubject());
        assertEquals("Your donation was approved", aliMessages.get(0).getMessage());
        assertFalse(aliMessages.get(0).isRead());
        assertTrue(inbox.getMessages("2").isEmpty());
    }

    @Test
    void emailIsWrittenToLogFile(@TempDir Path dir) throws Exception {
        Path log = dir.resolve("logs/email.log");
        new EmailNotification(log).send(ali, "Approved", "Your donation was approved");

        String content = Files.readString(log);
        assertTrue(content.contains("ali@example.com"));
        assertTrue(content.contains("Approved"));
        assertTrue(content.contains("Your donation was approved"));
    }

    @Test
    void smsIsWrittenToLogFile(@TempDir Path dir) throws Exception {
        Path log = dir.resolve("logs/sms.log");
        SmsNotification sms = new SmsNotification(log);
        sms.send(ali, "Approved", "First");
        sms.send(sara, "Rejected", "Second");

        List<String> lines = Files.readAllLines(log);
        assertEquals(2, lines.size());
        assertTrue(lines.get(0).contains("0100") && lines.get(0).contains("First"));
        assertTrue(lines.get(1).contains("0111") && lines.get(1).contains("Second"));
    }
}
