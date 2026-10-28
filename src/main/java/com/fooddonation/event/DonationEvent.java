package com.fooddonation.event;

import com.fooddonation.model.donation.Donation;

import java.time.LocalDateTime;

public class DonationEvent {
    private final EventType type;
    private final Donation donation;
    private final String message;
    private final LocalDateTime time;

    public DonationEvent(EventType type, Donation donation, String message, LocalDateTime time) {
        this.type = type;
        this.donation = donation;
        this.message = message;
        this.time = time;
    }

    public EventType getType() {
        return type;
    }

    public Donation getDonation() {
        return donation;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTime() {
        return time;
    }
}
