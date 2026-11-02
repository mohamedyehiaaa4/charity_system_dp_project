package com.fooddonation.controller;

import com.fooddonation.model.donation.Donation;
import com.fooddonation.service.DonationService;
import com.fooddonation.service.ReceiptService;
import com.fooddonation.ui.Controls;
import com.fooddonation.ui.Formats;
import com.fooddonation.ui.Session;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;

import java.time.Year;

/** DON-8 and DON-10: the donor's own donations with their status, receipts and the yearly tax summary. */
public class MyDonationsController implements Refreshable {
    private final DonationService donations;
    private final ReceiptService receipts;
    private final Session session;

    @FXML private TableView<Donation> table;
    @FXML private TableColumn<Donation, String> idColumn;
    @FXML private TableColumn<Donation, String> descriptionColumn;
    @FXML private TableColumn<Donation, String> detailsColumn;
    @FXML private TableColumn<Donation, String> priorityColumn;
    @FXML private TableColumn<Donation, String> statusColumn;
    @FXML private TableColumn<Donation, String> createdColumn;
    @FXML private TableColumn<Donation, String> reasonColumn;
    @FXML private Button receiptButton;
    @FXML private Spinner<Integer> yearSpinner;
    @FXML private TextArea output;
    @FXML private Label message;

    public MyDonationsController(DonationService donations, ReceiptService receipts, Session session) {
        this.donations = donations;
        this.receipts = receipts;
        this.session = session;
    }

    @FXML
    private void initialize() {
        Controls.column(idColumn, d -> "#" + d.getId());
        Controls.column(descriptionColumn, Donation::getDescription);
        Controls.column(detailsColumn, Formats::details);
        Controls.column(priorityColumn, d -> String.valueOf(d.getPriority()));
        Controls.column(statusColumn, Formats::status);
        Controls.column(createdColumn, d -> Formats.dateTime(d.getCreatedAt()));
        Controls.column(reasonColumn, d -> d.getRejectionReason() == null ? "" : d.getRejectionReason());
        table.setPlaceholder(new Label("No donations yet. Use the Submit tab to make one."));
        receiptButton.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
        int year = Year.now().getValue();
        yearSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(2000, year + 1, year));
    }

    @Override
    public void refresh() {
        table.getItems().setAll(donations.getMyDonations(session.getUser()));
    }

    @FXML
    private void showReceipt() {
        try {
            Donation selected = table.getSelectionModel().getSelectedItem();
            output.setText(receipts.getReceipt(session.getUser(), selected.getId()).toText());
            Controls.clear(message);
        } catch (IllegalStateException | IllegalArgumentException | SecurityException e) {
            output.clear();
            Controls.error(message, e);
        }
    }

    @FXML
    private void showAnnualSummary() {
        try {
            output.setText(receipts.getAnnualSummary(session.getUser(), session.getUser().getId(),
                    yearSpinner.getValue()).toText());
            Controls.clear(message);
        } catch (IllegalStateException | SecurityException e) {
            output.clear();
            Controls.error(message, e);
        }
    }
}
