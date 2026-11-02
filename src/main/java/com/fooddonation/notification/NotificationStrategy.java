package com.fooddonation.notification;

/** Strategy pattern: the Strategy. Each channel (in-app, email, SMS) is one implementation. */
public interface NotificationStrategy {

    void send(Recipient recipient, String subject, String message);
}
