package com.fooddonation.service;

import com.fooddonation.event.DonationEvent;
import com.fooddonation.event.DonationObserver;
import com.fooddonation.event.EventType;
import com.fooddonation.model.donation.Donation;
import com.fooddonation.model.donation.DonationFactory;
import com.fooddonation.model.donation.DonationRequest;
import com.fooddonation.model.donation.DonationType;
import com.fooddonation.model.lookup.DonationStatus;
import com.fooddonation.model.lookup.LookupType;
import com.fooddonation.model.user.Coordinator;
import com.fooddonation.model.user.Donor;
import com.fooddonation.persistence.DonationRepository;
import com.fooddonation.persistence.LookupRepository;
import com.fooddonation.persistence.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DonationServiceTest {
    static final ZoneId CAIRO = ZoneId.of("Africa/Cairo");

    DonationFactory factory;
    DonationRepository donations;
    LookupRepository lookups;
    DonationService service;
    DonationObserver observer;
    Donor donor = new Donor();
    Coordinator coordinator = new Coordinator();

    @BeforeEach
    void setUp() {
        factory = mock(DonationFactory.class);
        donations = mock(DonationRepository.class);
        lookups = mock(LookupRepository.class);
        observer = mock(DonationObserver.class);
        Clock clock = Clock.fixed(Instant.parse("2026-11-02T10:00:00Z"), CAIRO);
        service = new DonationService(factory, donations, lookups, mock(UserRepository.class), clock);
        service.attach(observer);
        donor.setId(7);
        donor.setName("Mona");
        // the five system statuses, looked up by name like the real LookupRepository
        when(lookups.findByName(eq(LookupType.DONATION_STATUS), anyString()))
                .thenAnswer(call -> Optional.of(new DonationStatus(1, call.getArgument(1), true)));
    }

    /** Small stand-in for Ezz's donation types, which can only be created through the factory. */
    static class FakeDonation extends Donation {
        FakeDonation(DonationType type, LocalDate expiry) {
            super(null, type, expiry);
        }

        @Override
        public String getDescription() {
            return "Fake " + getType();
        }

        @Override
        public int getPriority() {
            return 0;
        }
    }

    Donation stored(long id, String status) {
        Donation donation = new FakeDonation(DonationType.COOKED, LocalDate.of(2026, 11, 10));
        donation.setId(id);
        donation.setDonor(donor);
        donation.setStatus(new DonationStatus(1, status, true));
        when(donations.findById(id)).thenReturn(Optional.of(donation));
        return donation;
    }

    DonationEvent lastEvent() {
        ArgumentCaptor<DonationEvent> captor = ArgumentCaptor.forClass(DonationEvent.class);
        verify(observer).update(captor.capture());
        return captor.getValue();
    }

    DonationEvent event() {
        return new DonationEvent(EventType.STATUS_CHANGED, null, "hi", LocalDateTime.now());
    }

    @Test
    void attachedObserverGetsEvents() {
        DonationEvent event = event();
        service.notifyObservers(event);
        verify(observer).update(event);
    }

    @Test
    void attachingTwiceNotifiesOnce() {
        service.attach(observer);
        service.notifyObservers(event());
        verify(observer, times(1)).update(any());
    }

    @Test
    void detachedObserverGetsNothing() {
        service.detach(observer);
        service.notifyObservers(event());
        verify(observer, never()).update(any());
    }

    @Test
    void failingObserverDoesNotStopTheOthers() {
        DonationObserver second = mock(DonationObserver.class);
        service.attach(second);
        doThrow(new RuntimeException("boom")).when(observer).update(any());
        DonationEvent event = event();
        service.notifyObservers(event);
        verify(second).update(event);
    }

    @Test
    void nullObserverIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.attach(null));
    }

    // --- submitting ---

    @Test
    void submitSavesAsSubmittedThenFiresEvent() {
        DonationRequest request = new DonationRequest();
        Donation created = new FakeDonation(DonationType.COOKED, LocalDate.of(2026, 11, 10));
        created.setDonor(donor); // the real factory copies the donor from the request
        when(factory.createDonation(request)).thenReturn(created);
        when(factory.applyAddOns(created, Set.of())).thenReturn(created);
        when(donations.save(created)).thenAnswer(call -> {
            created.setId(42);
            return created;
        });

        Donation result = service.submitDonation(donor, request, Set.of());

        assertSame(created, result);
        assertSame(donor, request.getDonor());
        assertEquals(DonationStatus.SUBMITTED, result.getStatus().getName());
        assertEquals(LocalDateTime.of(2026, 11, 2, 12, 0), result.getCreatedAt());
        InOrder order = inOrder(donations, observer);
        order.verify(donations).save(created);
        order.verify(observer).update(any());
        assertEquals(EventType.DONATION_SUBMITTED, lastEvent().getType());
        assertEquals(42, lastEvent().getDonation().getId());
    }

    @Test
    void failedSaveFiresNothing() {
        Donation created = new FakeDonation(DonationType.COOKED, null);
        when(factory.createDonation(any())).thenReturn(created);
        when(factory.applyAddOns(any(), any())).thenReturn(created);
        when(donations.save(any())).thenThrow(new IllegalStateException("db down"));

        assertThrows(IllegalStateException.class,
                () -> service.submitDonation(donor, new DonationRequest(), Set.of()));
        verify(observer, never()).update(any());
    }

    @Test
    void coordinatorCannotSubmit() {
        assertThrows(SecurityException.class,
                () -> service.submitDonation(coordinator, new DonationRequest(), Set.of()));
    }

    // --- listing ---

    @Test
    void donorSeesOnlyOwnDonations() {
        List<Donation> mine = List.of(stored(1, DonationStatus.SUBMITTED));
        when(donations.findByDonor(7)).thenReturn(mine);
        assertEquals(mine, service.getMyDonations(donor));
    }

    @Test
    void onlyCoordinatorSeesAllDonations() {
        assertThrows(SecurityException.class, () -> service.getAllDonations(donor));
        service.getAllDonations(coordinator);
        verify(donations).findAll();
    }

    // --- lifecycle ---

    @Test
    void approveMovesSubmittedToApprovedAndTellsObservers() {
        Donation donation = stored(1, DonationStatus.SUBMITTED);

        service.approve(coordinator, 1);

        verify(donations).updateStatus(eq(1L), any(), eq(null));
        assertEquals(DonationStatus.APPROVED, donation.getStatus().getName());
        assertEquals(EventType.STATUS_CHANGED, lastEvent().getType());
    }

    @Test
    void fullLifecycleWorks() {
        Donation donation = stored(1, DonationStatus.SUBMITTED);
        service.approve(coordinator, 1);
        service.markPickedUp(coordinator, 1);
        service.markDelivered(coordinator, 1);
        assertEquals(DonationStatus.DELIVERED, donation.getStatus().getName());
        verify(observer, times(3)).update(any());
    }

    @Test
    void rejectStoresReasonAndFiresRejection() {
        Donation donation = stored(1, DonationStatus.SUBMITTED);

        service.reject(coordinator, 1, "  Food looked spoiled ");

        verify(donations).updateStatus(eq(1L), any(), eq("Food looked spoiled"));
        assertEquals("Food looked spoiled", donation.getRejectionReason());
        assertEquals(EventType.DONATION_REJECTED, lastEvent().getType());
    }

    @Test
    void rejectNeedsAReason() {
        stored(1, DonationStatus.SUBMITTED);
        assertThrows(IllegalArgumentException.class, () -> service.reject(coordinator, 1, " "));
        assertThrows(IllegalArgumentException.class, () -> service.reject(coordinator, 1, null));
        verify(donations, never()).updateStatus(anyLong(), any(), any());
    }

    @Test
    void donorCannotChangeStatus() {
        stored(1, DonationStatus.SUBMITTED);
        assertThrows(SecurityException.class, () -> service.approve(donor, 1));
        assertThrows(SecurityException.class, () -> service.approve(null, 1));
    }

    @Test
    void skippingAStepIsRefused() {
        stored(1, DonationStatus.SUBMITTED);
        assertThrows(IllegalStateException.class, () -> service.markPickedUp(coordinator, 1));
        assertThrows(IllegalStateException.class, () -> service.markDelivered(coordinator, 1));
    }

    @Test
    void finalStatusesCannotChange() {
        stored(1, DonationStatus.REJECTED);
        stored(2, DonationStatus.DELIVERED);
        assertThrows(IllegalStateException.class, () -> service.approve(coordinator, 1));
        assertThrows(IllegalStateException.class, () -> service.reject(coordinator, 2, "late"));
        verify(donations, never()).updateStatus(anyLong(), any(), any());
        verify(observer, never()).update(any());
    }

    @Test
    void customStatusIsNotPartOfTheLifecycle() {
        stored(1, "On hold");
        assertThrows(IllegalStateException.class, () -> service.approve(coordinator, 1));
    }

    @Test
    void unknownDonationIsRejected() {
        when(donations.findById(99)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.approve(coordinator, 99));
    }

    @Test
    void failedUpdateFiresNothing() {
        stored(1, DonationStatus.SUBMITTED);
        doThrow(new IllegalStateException("db down")).when(donations).updateStatus(anyLong(), any(), any());
        assertThrows(IllegalStateException.class, () -> service.approve(coordinator, 1));
        verify(observer, never()).update(any());
    }
}
