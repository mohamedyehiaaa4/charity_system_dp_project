package com.fooddonation.ui;

import javafx.application.Application;

/**
 * Starts the GUI. It is a separate class from FoodDonationApp because a main class that
 * extends Application won't start from the classpath ("JavaFX runtime components are missing").
 * Run it from IntelliJ, or with {@code mvn javafx:run}.
 */
public final class Launcher {

    private Launcher() {
    }

    public static void main(String[] args) {
        Application.launch(FoodDonationApp.class, args);
    }
}
