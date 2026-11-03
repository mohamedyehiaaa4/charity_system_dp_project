package com.fooddonation.service.donationevent;

public interface DonationSubject {

    void attach(DonationObserver observer);

    void detach(DonationObserver observer);

    void notifyObservers(DonationEvent event);
}
