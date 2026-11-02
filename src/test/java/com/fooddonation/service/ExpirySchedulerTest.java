package com.fooddonation.service;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ExpirySchedulerTest {
    DonationService service = mock(DonationService.class);
    ExpiryScheduler scheduler = new ExpiryScheduler(service, Duration.ofMillis(20));

    @Test
    void checksRepeatedlyUntilStopped() throws InterruptedException {
        scheduler.start();
        scheduler.start(); // second start is ignored
        verify(service, timeout(1000).atLeast(3)).checkExpiringDonations();

        scheduler.stop();
        clearInvocations(service);
        Thread.sleep(100);
        verifyNoInteractions(service);
    }

    @Test
    void keepsRunningAfterAFailedCheck() {
        when(service.checkExpiringDonations()).thenThrow(new IllegalStateException("db down"));
        scheduler.start();
        verify(service, timeout(1000).atLeast(3)).checkExpiringDonations();
        scheduler.stop();
    }

    @Test
    void runNowChecksOnceAndStopWithoutStartIsSafe() {
        scheduler.runNow();
        verify(service).checkExpiringDonations();
        scheduler.stop();
    }
}
