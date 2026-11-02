package com.fooddonation.controller;

import com.fooddonation.model.donation.Donation;
import com.fooddonation.model.lookup.DonationStatus;
import com.fooddonation.service.DonationService;
import com.fooddonation.service.ReceiptService;
import com.fooddonation.ui.Controls;
import com.fooddonation.ui.Formats;
import com.fooddonation.ui.Session;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.Comparator;
import java.util.List;
import java.util.function.LongConsumer;

/** DON-5 and DON-6: the coordinator reviews donations and moves them through the lifecycle. */
public class ReviewDonationsController implements Refreshable {
    private static final String ALL = "All statuses";

    private final DonationService donations;
    private final ReceiptService receipts;
    private final Session session;

    @FXML private ComboBox<String> statusFilter;
    @FXML private TableView<Donation> table;
    @FXML private TableColumn<Donation, String> idColumn;
    @FXML private TableColumn<Donation, String> donorColumn;
    @FXML private TableColumn<Donation, String> descriptionColumn;
    @FXML private TableColumn<Donation, String> detailsColumn;
    @FXML private TableColumn<Donation, String> priorityColumn;
    @FXML private TableColumn<Donation, String> statusColumn;
    @FXML private TableColumn<Donation, String> expiryColumn;
    @FXML private TableColumn<Donation, String> pickupColumn;
    @FXML private Button approveButton;
    @FXML private Button rejectButton;
    @FXML private Button pickedUpButton;
    @FXML private Button deliveredButton;
    @FXML private Button receiptButton;
    @FXML private TextField reasonField;
    @FXML private TextArea output;
    @FXML private Label message;

    public ReviewDonationsController(DonationService donations, ReceiptService receipts, Session session) {
        this.donations = donations;
        this.receipts = receipts;
        this.session = session;
    }

    @FXML
    private void initialize() {
        statusFilter.getItems().setAll(ALL, DonationStatus.SUBMITTED, DonationStatus.APPROVED,
                DonationStatus.REJECTED, DonationStatus.PICKED_UP, DonationStatus.DELIVERED);
        statusFilter.setValue(ALL);
        statusFilter.valueProperty().addListener((obs, old, value) -> refresh());

        Controls.column(idColumn, d -> "#" + d.getId());
        Controls.column(donorColumn, d -> d.getDonor().getName());
        Controls.column(descriptionColumn, Donation::getDescription);
        Controls.column(detailsColumn, Formats::details);
        Controls.column(priorityColumn, d -> String.valueOf(d.getPriority()));
        Controls.column(statusColumn, Formats::status);
        Controls.column(expiryColumn, d -> Formats.date(d.getExpiry()));
        Controls.column(pickupColumn, Formats::pickup);
        table.setPlaceholder(new Label("No donations with this status."));
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> updateButtons(selected));
        updateButtons(null);
    }

    /** Highest priority first, so urgent donations are at the top. */
    @Override
    public void refresh() {
        Donation selected = table.getSelectionModel().getSelectedItem();
        List<Donation> all = donations.getAllDonations(session.getUser()).stream()
                .filter(d -> ALL.equals(statusFilter.getValue()) || Formats.status(d).equals(statusFilter.getValue()))
                .sorted(Comparator.comparingInt(Donation::getPriority).reversed().thenComparing(Donation::getId))
                .toList();
        table.getItems().setAll(all);
        if (selected != null) {
            all.stream().filter(d -> d.getId() == selected.getId()).findFirst()
                    .ifPresent(d -> table.getSelectionModel().select(d));
        }
        updateButtons(table.getSelectionModel().getSelectedItem());
    }

    @FXML
    private void approve() {
        change(id -> donations.approve(session.getUser(), id), "approved");
    }

    @FXML
    private void reject() {
        change(id -> donations.reject(session.getUser(), id, reasonField.getText()), "rejected");
        reasonField.clear();
    }

    @FXML
    private void markPickedUp() {
        change(id -> donations.markPickedUp(session.getUser(), id), "marked as picked up");
    }

    @FXML
    private void markDelivered() {
        change(id -> donations.markDelivered(session.getUser(), id), "marked as delivered");
    }

    @FXML
    private void showReceipt() {
        try {
            output.setText(receipts.getReceipt(session.getUser(), selectedId()).toText());
            Controls.clear(message);
        } catch (IllegalStateException | IllegalArgumentException e) {
            Controls.error(message, e);
        }
    }

    /** DON-6: runs the same check the scheduler runs every minute. */
    @FXML
    private void checkExpiry() {
        List<Donation> warned = donations.checkExpiringDonations();
        Controls.ok(message, warned.isEmpty()
                ? "No new donations near expiry."
                : warned.size() + " donation(s) near expiry. The coordinator inbox has the warnings.");
        refresh();
    }

    private void change(LongConsumer action, String done) {
        try {
            long id = selectedId();
            action.accept(id);
            Controls.ok(message, "Donation #" + id + " " + done + ". The donor has been notified.");
            refresh();
        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
            Controls.error(message, e);
        }
    }

    private long selectedId() {
        Donation selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            throw new IllegalArgumentException("Select a donation first");
        }
        return selected.getId();
    }

    /** Only the moves the lifecycle allows from the selected donation's status are enabled. */
    private void updateButtons(Donation selected) {
        String status = selected == null ? "" : Formats.status(selected);
        approveButton.setDisable(!DonationStatus.SUBMITTED.equals(status));
        rejectButton.setDisable(!DonationStatus.SUBMITTED.equals(status));
        reasonField.setDisable(!DonationStatus.SUBMITTED.equals(status));
        pickedUpButton.setDisable(!DonationStatus.APPROVED.equals(status));
        deliveredButton.setDisable(!DonationStatus.PICKED_UP.equals(status));
        receiptButton.setDisable(selected == null || !ReceiptService.isAccepted(selected));
    }
}
