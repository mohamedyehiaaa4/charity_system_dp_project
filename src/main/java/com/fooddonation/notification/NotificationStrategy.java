package com.fooddonation.notification;

public interface NotificationStrategy {

    void send(Recipient recipient, String subject, String message);
}
