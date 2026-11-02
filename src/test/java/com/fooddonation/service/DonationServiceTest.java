package com.fooddonation.service;

import com.fooddonation.event.DonationEvent;
import com.fooddonation.event.DonationObserver;
import com.fooddonation.event.EventType;
import com.fooddonation.model.donation.DonationFactory;
import com.fooddonation.persistence.DonationRepository;
import com.fooddonation.persistence.LookupRepository;
import com.fooddonation.persistence.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class DonationServiceTest {
    static final ZoneId CAIRO = ZoneId.of("Africa/Cairo");

    DonationFactory factory;
    DonationRepository donations;
    LookupRepository lookups;
    DonationService service;
    DonationObserver observer;

    @BeforeEach
    void setUp() {
        factory = mock(DonationFactory.class);
        donations = mock(DonationRepository.class);
        lookups = mock(LookupRepository.class);
        observer = mock(DonationObserver.class);
        Clock clock = Clock.fixed(Instant.parse("2026-11-02T10:00:00Z"), CAIRO);
        service = new DonationService(factory, donations, lookups, mock(UserRepository.class), clock);
        service.attach(observer);
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
}
