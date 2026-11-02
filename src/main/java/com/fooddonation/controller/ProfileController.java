package com.fooddonation.controller;

import com.fooddonation.model.user.Coordinator;
import com.fooddonation.model.user.Donor;
import com.fooddonation.model.user.DonorType;
import com.fooddonation.model.user.User;
import com.fooddonation.notification.ChannelType;
import com.fooddonation.service.AuthService;
import com.fooddonation.ui.Controls;
import com.fooddonation.ui.Formats;
import com.fooddonation.ui.Session;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

/** DON-8 and COM-2: view and update the profile, including the notification channel. */
public class ProfileController implements Refreshable {
    private final AuthService auth;
    private final Session session;

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;
    @FXML private Pane donorTypeRow;
    @FXML private ComboBox<DonorType> donorTypeBox;
    @FXML private ComboBox<ChannelType> channelBox;
    @FXML private Label message;

    public ProfileController(AuthService auth, Session session) {
        this.auth = auth;
        this.session = session;
    }

    @FXML
    private void initialize() {
        donorTypeBox.getItems().setAll(DonorType.values());
        Controls.showAs(donorTypeBox, Formats::label);
        channelBox.getItems().setAll(ChannelType.values());
        Controls.showAs(channelBox, Formats::label);
    }

    @Override
    public void refresh() {
        User user = session.getUser();
        nameField.setText(user.getName());
        emailField.setText(user.getEmail());
        phoneField.setText(user.getPhone());
        addressField.setText(user.getAddress());
        channelBox.setValue(user.getPreferredChannel());
        boolean donor = user instanceof Donor;
        donorTypeRow.setVisible(donor);
        donorTypeRow.setManaged(donor);
        if (user instanceof Donor d) {
            donorTypeBox.setValue(d.getDonorType());
        }
        Controls.clear(message);
    }

    @FXML
    private void save() {
        // edit a copy, so a rejected change doesn't leave the signed-in user half updated
        User changed = copyWithFormValues(session.getUser());
        try {
            auth.updateProfile(changed);
            session.signIn(changed);
            Controls.ok(message, "Saved. New messages go by " + Formats.label(changed.getPreferredChannel()) + ".");
        } catch (IllegalArgumentException e) {
            Controls.error(message, e);
        }
    }

    private User copyWithFormValues(User user) {
        User copy;
        if (user instanceof Donor) {
            Donor donor = new Donor();
            donor.setDonorType(donorTypeBox.getValue());
            copy = donor;
        } else {
            copy = new Coordinator();
        }
        copy.setId(user.getId());
        copy.setName(nameField.getText());
        copy.setEmail(emailField.getText());
        copy.setPhone(phoneField.getText());
        copy.setAddress(addressField.getText());
        copy.setPreferredChannel(channelBox.getValue());
        return copy;
    }
}
