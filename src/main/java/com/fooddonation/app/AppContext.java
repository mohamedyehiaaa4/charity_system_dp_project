package com.fooddonation.app;

import com.fooddonation.event.CoordinatorDashboardObserver;
import com.fooddonation.event.DonorNotificationObserver;
import com.fooddonation.event.VolunteerNotificationObserver;
import com.fooddonation.model.donation.DonationFactory;
import com.fooddonation.notification.ChannelType;
import com.fooddonation.notification.EmailNotification;
import com.fooddonation.notification.InAppInbox;
import com.fooddonation.notification.InAppNotification;
import com.fooddonation.notification.NotificationService;
import com.fooddonation.notification.SmsNotification;
import com.fooddonation.persistence.DatabaseConnection;
import com.fooddonation.persistence.DonationRepository;
import com.fooddonation.persistence.LookupRepository;
import com.fooddonation.persistence.UserRepository;
import com.fooddonation.service.AuthService;
import com.fooddonation.service.DonationService;
import com.fooddonation.service.ExpiryScheduler;

import java.time.Clock;
import java.util.Map;

/**
 * Creates every object once and connects them. This is the only place that calls
 * DatabaseConnection.getInstance(); everything else gets what it needs through its constructor.
 */
public class AppContext implements AutoCloseable {
    private final DatabaseConnection db;
    private final UserRepository users;
    private final LookupRepository lookups;
    private final DonationRepository donations;
    private final AuthService auth;
    private final InAppInbox inbox;
    private final NotificationService notifications;
    private final DonationService donationService;
    private final ExpiryScheduler scheduler;

    public AppContext(AppConfig config) {
        db = DatabaseConnection.getInstance();
        db.initialize();

        DonationFactory factory = new DonationFactory();
        users = new UserRepository(db);
        lookups = new LookupRepository(db);
        donations = new DonationRepository(db, factory);
        auth = new AuthService(users);

        inbox = new InAppInbox();
        notifications = new NotificationService(Map.of(
                ChannelType.IN_APP, new InAppNotification(inbox),
                ChannelType.EMAIL, new EmailNotification(config.emailLog()),
                ChannelType.SMS, new SmsNotification(config.smsLog())));

        donationService = new DonationService(factory, donations, lookups, users,
                Clock.system(config.timezone()), config.expiryWarningWindow());
        // observers are attached once, here
        donationService.attach(new CoordinatorDashboardObserver(notifications, users, config.coordinatorEmail()));
        donationService.attach(new DonorNotificationObserver(notifications));
        donationService.attach(new VolunteerNotificationObserver(notifications, config.volunteers()));

        scheduler = new ExpiryScheduler(donationService, config.expiryCheckPeriod());
    }

    /** Starts the background expiry checks. */
    public void start() {
        scheduler.start();
    }

    /** Stops the scheduler first so it can't use the database after it is closed. */
    @Override
    public void close() {
        scheduler.stop();
        db.close();
    }

    public UserRepository getUsers() {
        return users;
    }

    public LookupRepository getLookups() {
        return lookups;
    }

    public DonationRepository getDonations() {
        return donations;
    }

    public AuthService getAuth() {
        return auth;
    }

    public InAppInbox getInbox() {
        return inbox;
    }

    public NotificationService getNotifications() {
        return notifications;
    }

    public DonationService getDonationService() {
        return donationService;
    }

    public ExpiryScheduler getScheduler() {
        return scheduler;
    }
}
