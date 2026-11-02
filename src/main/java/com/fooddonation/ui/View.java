package com.fooddonation.ui;

/** The top-level screens. Each one is an FXML file in resources/com/fooddonation/view. */
public enum View {
    LOGIN("login.fxml", "Sign in"),
    REGISTER("register.fxml", "Register as a donor"),
    DONOR_PANEL("donor-panel.fxml", "Donor control panel"),
    COORDINATOR_PANEL("coordinator-panel.fxml", "Coordinator dashboard");

    private final String fxml;
    private final String title;

    View(String fxml, String title) {
        this.fxml = fxml;
        this.title = title;
    }

    public String fxmlPath() {
        return "/com/fooddonation/view/" + fxml;
    }

    public String title() {
        return title;
    }
}
