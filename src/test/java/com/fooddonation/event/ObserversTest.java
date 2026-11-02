package com.fooddonation.event;

import com.fooddonation.model.donation.Donation;
import com.fooddonation.model.user.Coordinator;
import com.fooddonation.model.user.Donor;
import com.fooddonation.notification.ChannelType;
import com.fooddonation.notification.NotificationService;
import com.fooddonation.notification.Recipient;
import com.fooddonation.persistence.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ObserversTest {
    static final String COORDINATOR_EMAIL = "coordinator@fooddonation.org";

    NotificationService notifications = mock(NotificationService.class);
    UserRepository users = mock(UserRepository.class);
    Donation donation = mock(Donation.class);
    Recipient donorRecipient = new Recipient("7", "Mona", "mona@x.com", "010", ChannelType.SMS);
    Recipient coordinatorRecipient = new Recipient("1", "Coordinator", COORDINATOR_EMAIL, null, ChannelType.IN_APP);
    Recipient volunteerA = new Recipient("volunteer-1", "Ali", "ali@x.com", "011", ChannelType.EMAIL);
    Recipient volunteerB = new Recipient("volunteer-2", "Sara", "sara@x.com", "012", ChannelType.SMS);

    DonationObserver donorObserver = new DonorNotificationObserver(notifications);
    DonationObserver volunteerObserver = new VolunteerNotificationObserver(notifications, List.of(volunteerA, volunteerB));
    DonationObserver coordinatorObserver = new CoordinatorDashboardObserver(notifications, users, COORDINATOR_EMAIL);

    @BeforeEach
    void setUp() {
        Donor donor = mock(Donor.class);
        when(donor.toRecipient()).thenReturn(donorRecipient);
        when(donation.getDonor()).thenReturn(donor);
        Coordinator coordinator = mock(Coordinator.class);
        when(coordinator.toRecipient()).thenReturn(coordinatorRecipient);
        when(users.findByEmail(COORDINATOR_EMAIL)).thenReturn(Optional.of(coordinator));
    }

    void sendToAll(EventType type, String message) {
        DonationEvent event = new DonationEvent(type, donation, message, LocalDateTime.now());
        donorObserver.update(event);
        volunteerObserver.update(event);
        coordinatorObserver.update(event);
    }

    @Test
    void newDonationGoesToCoordinatorAndVolunteers() {
        sendToAll(EventType.DONATION_SUBMITTED, "new one");

        verify(notifications).notifyByPreference(coordinatorRecipient, "New donation submitted", "new one");
        verify(notifications).notifyByPreference(volunteerA, "New donation submitted", "new one");
        verify(notifications).notifyByPreference(volunteerB, "New donation submitted", "new one");
        verify(notifications, never()).notifyByPreference(donorRecipient, "New donation submitted", "new one");
    }

    @Test
    void statusChangeGoesToDonorOnly() {
        sendToAll(EventType.STATUS_CHANGED, "now Approved");

        verify(notifications).notifyByPreference(donorRecipient, "Donation status changed", "now Approved");
        verify(notifications, times(1)).notifyByPreference(any(), any(), any());
    }

    @Test
    void rejectionGoesToDonorWithReason() {
        sendToAll(EventType.DONATION_REJECTED, "rejected. Reason: spoiled");

        verify(notifications).notifyByPreference(donorRecipient, "Donation rejected", "rejected. Reason: spoiled");
        verify(notifications, times(1)).notifyByPreference(any(), any(), any());
    }

    @Test
    void nearExpiryGoesToCoordinatorOnly() {
        sendToAll(EventType.NEAR_EXPIRY, "expires soon");

        verify(notifications).notifyByPreference(coordinatorRecipient, "Donation near expiry", "expires soon");
        verify(notifications, times(1)).notifyByPreference(any(), any(), any());
    }

    @Test
    void oneFailedVolunteerDoesNotStopTheOthers() {
        doThrow(new IllegalStateException("log file locked"))
                .when(notifications).notifyByPreference(eq(volunteerA), any(), any());

        volunteerObserver.update(new DonationEvent(EventType.DONATION_SUBMITTED, donation, "new one", LocalDateTime.now()));

        verify(notifications).notifyByPreference(volunteerB, "New donation submitted", "new one");
    }
}
