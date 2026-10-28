package com.fooddonation.event;

public interface DonationSubject {

    void attach(DonationObserver observer);

    void detach(DonationObserver observer);

    void notifyObservers(DonationEvent event);
}
