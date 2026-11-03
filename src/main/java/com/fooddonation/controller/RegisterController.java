package com.fooddonation.controller;

import com.fooddonation.model.user.Donor;
import com.fooddonation.model.user.DonorType;
import com.fooddonation.notification.ChannelType;
import com.fooddonation.service.AuthService;
import com.fooddonation.view.Controls;
import com.fooddonation.view.Formats;
import com.fooddonation.view.View;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/** DON-1: a donor registers with contact details, donor type and preferred channel. */
public class RegisterController {
    private final AuthService auth;
    private final Session session;
    private final Navigator navigator;

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;
    @FXML private ComboBox<DonorType> donorTypeBox;
    @FXML private ComboBox<ChannelType> channelBox;
    @FXML private Label message;

    public RegisterController(AuthService auth, Session session, Navigator navigator) {
        this.auth = auth;
        this.session = session;
        this.navigator = navigator;
    }

    @FXML
    private void initialize() {
        donorTypeBox.getItems().setAll(DonorType.values());
        donorTypeBox.setValue(DonorType.INDIVIDUAL);
        Controls.showAs(donorTypeBox, Formats::label);
        channelBox.getItems().setAll(ChannelType.values());
        channelBox.setValue(ChannelType.IN_APP);
        Controls.showAs(channelBox, Formats::label);
    }

    @FXML
    private void register() {
        try {
            Donor donor = auth.register(nameField.getText(), emailField.getText(), phoneField.getText(),
                    addressField.getText(), donorTypeBox.getValue(), channelBox.getValue());
            session.signIn(donor);
            navigator.show(View.DONOR_PANEL);
        } catch (IllegalArgumentException e) {
            Controls.error(message, e);
        }
    }

    @FXML
    private void back() {
        navigator.show(View.LOGIN);
    }
}
