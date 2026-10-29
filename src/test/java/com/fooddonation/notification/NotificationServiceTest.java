package com.fooddonation.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class NotificationServiceTest {
    private NotificationStrategy inApp;
    private NotificationStrategy email;
    private NotificationStrategy sms;
    private NotificationService service;
    private Recipient recipient;

    @BeforeEach
    void setUp() {
        inApp = mock(NotificationStrategy.class);
        email = mock(NotificationStrategy.class);
        sms = mock(NotificationStrategy.class);
        service = new NotificationService(Map.of(
                ChannelType.IN_APP, inApp, ChannelType.EMAIL, email, ChannelType.SMS, sms));
        recipient = new Recipient("1", "Ali", "ali@example.com", "0100", ChannelType.EMAIL);
    }

    @Test
    void notifyUsesTheStrategyThatIsSet() {
        service.setStrategy(sms);
        service.notify(recipient, "Hi", "Hello");
        verify(sms).send(recipient, "Hi", "Hello");
    }

    @Test
    void strategyCanBeChangedAtRuntime() {
        service.setStrategy(sms);
        service.notify(recipient, "First", "1");
        service.setStrategy(inApp);
        service.notify(recipient, "Second", "2");

        verify(sms).send(recipient, "First", "1");
        verify(inApp).send(recipient, "Second", "2");
    }

    @Test
    void notifyWithoutStrategyThrows() {
        assertThrows(IllegalStateException.class, () -> service.notify(recipient, "Hi", "Hello"));
    }

    @Test
    void notifyByPreferenceUsesPreferredChannel() {
        service.notifyByPreference(recipient, "Hi", "Hello");
        verify(email).send(recipient, "Hi", "Hello");
    }

    @Test
    void changingPreferenceChangesNextChannel() {
        service.notifyByPreference(recipient, "First", "1");
        recipient.setPreferredChannel(ChannelType.SMS);
        service.notifyByPreference(recipient, "Second", "2");

        verify(email).send(recipient, "First", "1");
        verify(sms).send(recipient, "Second", "2");
    }

    @Test
    void missingPreferenceFallsBackToInApp() {
        recipient.setPreferredChannel(null);
        service.notifyByPreference(recipient, "Hi", "Hello");
        verify(inApp).send(recipient, "Hi", "Hello");
    }

    @Test
    void preferenceWithNoStrategyFallsBackToInApp() {
        NotificationService inAppOnly = new NotificationService(Map.of(ChannelType.IN_APP, inApp));
        inAppOnly.notifyByPreference(recipient, "Hi", "Hello");
        verify(inApp).send(recipient, "Hi", "Hello");
        verify(email, never()).send(recipient, "Hi", "Hello");
    }

    @Test
    void constructorRequiresInAppStrategy() {
        assertThrows(IllegalArgumentException.class,
                () -> new NotificationService(Map.of(ChannelType.EMAIL, email)));
    }
}
