package com.fooddonation.controller;

import com.fooddonation.model.user.Role;
import com.fooddonation.model.user.User;
import com.fooddonation.service.AuthService;
import com.fooddonation.ui.Controls;
import com.fooddonation.ui.Navigator;
import com.fooddonation.ui.Session;
import com.fooddonation.ui.View;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.Optional;

/** Sign in by email. Phase 1 has no passwords. */
public class LoginController {
    static final String COORDINATOR_EMAIL = "coordinator@fooddonation.org";

    private final AuthService auth;
    private final Session session;
    private final Navigator navigator;

    @FXML private TextField emailField;
    @FXML private Label message;

    public LoginController(AuthService auth, Session session, Navigator navigator) {
        this.auth = auth;
        this.session = session;
        this.navigator = navigator;
    }

    @FXML
    private void signIn() {
        Optional<User> user = auth.login(emailField.getText());
        if (user.isEmpty()) {
            Controls.error(message, new IllegalArgumentException(
                    "No account with that email. Register as a donor, or use the coordinator account."));
            return;
        }
        session.signIn(user.get());
        navigator.show(user.get().getRole() == Role.COORDINATOR ? View.COORDINATOR_PANEL : View.DONOR_PANEL);
    }

    /** Fills in the seeded coordinator account, to save typing while testing. */
    @FXML
    private void useCoordinator() {
        emailField.setText(COORDINATOR_EMAIL);
        signIn();
    }

    @FXML
    private void register() {
        navigator.show(View.REGISTER);
    }
}
