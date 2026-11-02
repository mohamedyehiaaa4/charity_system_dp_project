package com.fooddonation.service;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** Runs the expiry check in the background every period, and on demand with {@link #runNow()}. */
public class ExpiryScheduler {
    private final DonationService service;
    private final Duration period;
    private ScheduledExecutorService executor;

    public ExpiryScheduler(DonationService service) {
        this(service, Duration.ofSeconds(60));
    }

    public ExpiryScheduler(DonationService service, Duration period) {
        this.service = service;
        this.period = period;
    }

    /** Starts checking right away, then every period. Calling it again while running does nothing. */
    public synchronized void start() {
        if (executor != null) {
            return;
        }
        executor = Executors.newSingleThreadScheduledExecutor(task -> {
            Thread thread = new Thread(task, "expiry-scheduler");
            thread.setDaemon(true);
            return thread;
        });
        executor.scheduleAtFixedRate(this::runNow, 0, period.toMillis(), TimeUnit.MILLISECONDS);
    }

    /** Stops the background checks and waits briefly for a running check to finish. */
    public synchronized void stop() {
        if (executor == null) {
            return;
        }
        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        executor = null;
    }

    public void runNow() {
        // an exception would silently cancel all later scheduled runs, so catch it here
        try {
            service.checkExpiringDonations();
        } catch (RuntimeException e) {
            System.err.println("Expiry check failed: " + e.getMessage());
        }
    }
}
