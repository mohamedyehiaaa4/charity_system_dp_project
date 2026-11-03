package com.fooddonation.service.donationevent;

import com.fooddonation.model.donation.Donation;

/** The subject and text of the message sent for each event, kept in one place. */
public final class MessageTemplates {

    private MessageTemplates() {
    }

    public static String subject(EventType type) {
        return switch (type) {
            case DONATION_SUBMITTED -> "New donation submitted";
            case STATUS_CHANGED -> "Donation status changed";
            case DONATION_REJECTED -> "Donation rejected";
            case NEAR_EXPIRY -> "Donation near expiry";
        };
    }

    public static String message(EventType type, Donation donation) {
        String what = "Donation #" + donation.getId() + " (" + donation.getDescription() + ")";
        return switch (type) {
            case DONATION_SUBMITTED -> what + " was submitted by " + donation.getDonor().getName() + ".";
            case STATUS_CHANGED -> what + " is now " + donation.getStatus().getName() + ".";
            case DONATION_REJECTED -> what + " was rejected. Reason: " + donation.getRejectionReason();
            case NEAR_EXPIRY -> what + " expires on " + donation.getExpiry() + ".";
        };
    }
}
