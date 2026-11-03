package com.fooddonation.controller;

import com.fooddonation.model.donation.Donation;
import com.fooddonation.model.donation.MoneyDonation;
import com.fooddonation.model.lookup.DonationStatus;
import com.fooddonation.service.DonationService;
import com.fooddonation.service.ReceiptService;
import com.fooddonation.ui.Controls;
import com.fooddonation.ui.Formats;
import com.fooddonation.ui.Session;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.LongConsumer;

/** DON-5 and DON-6: the coordinator reviews donations and moves them through the lifecycle. */
public class ReviewDonationsController implements Refreshable {
    private static final String ALL = "All statuses";
    private static final int HIGH_PRIORITY = 10;
    private static final PseudoClass URGENT = PseudoClass.getPseudoClass("urgent");

    private final DonationService donations;
    private final ReceiptService receipts;
    private final Session session;

    @FXML private Label waitingValue;
    @FXML private Label approvedValue;
    @FXML private Label pickedUpValue;
    @FXML private Label expiringValue;
    @FXML private Label moneyValue;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private TableView<Donation> table;
    @FXML private TableColumn<Donation, String> idColumn;
    @FXML private TableColumn<Donation, String> donorColumn;
    @FXML private TableColumn<Donation, String> whatColumn;
    @FXML private TableColumn<Donation, String> extrasColumn;
    @FXML private TableColumn<Donation, String> priorityColumn;
    @FXML private TableColumn<Donation, String> statusColumn;
    @FXML private TableColumn<Donation, String> expiryColumn;
    @FXML private TableColumn<Donation, String> pickupColumn;
    @FXML private Button approveButton;
    @FXML private Button rejectButton;
    @FXML private Button pickedUpButton;
    @FXML private Button deliveredButton;
    @FXML private Button receiptButton;
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
        searchField.textProperty().addListener((obs, old, value) -> refresh());

        Controls.column(idColumn, d -> "#" + d.getId());
        Controls.column(donorColumn, d -> d.getDonor().getName());
        Controls.column(whatColumn, Formats::what);
        // the Decorator's add-ons as small tags
        Controls.nodeColumn(extrasColumn, d -> Controls.tags(Formats.addOns(d)));
        Controls.nodeColumn(priorityColumn, d -> d.getPriority() >= HIGH_PRIORITY
                ? Controls.styled(String.valueOf(d.getPriority()), "priority", "high")
                : Controls.styled(String.valueOf(d.getPriority()), "priority"));
        Controls.nodeColumn(statusColumn, d -> Controls.styled(Formats.status(d), "badge", Formats.statusStyle(d)));
        Controls.nodeColumn(expiryColumn, d -> Formats.nearExpiry(d, LocalDate.now())
                ? Controls.styled(Formats.date(d.getExpiry()), "soon")
                : Controls.styled(d.getExpiry() == null ? "—" : Formats.date(d.getExpiry())));
        Controls.column(pickupColumn, Formats::pickup);

        // a red edge on rows that need attention first
        table.setRowFactory(view -> new TableRow<>() {
            @Override
            protected void updateItem(Donation item, boolean empty) {
                super.updateItem(item, empty);
                pseudoClassStateChanged(URGENT, !empty && item != null && isUrgent(item));
            }
        });
        table.setPlaceholder(new Label("No donations match."));
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> updateButtons(selected));
        updateButtons(null);
    }

    /** Highest priority first, so urgent donations are at the top. */
    @Override
    public void refresh() {
        Donation selected = table.getSelectionModel().getSelectedItem();
        List<Donation> all = donations.getAllDonations(session.getUser());
        updateCards(all);
        String search = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
        List<Donation> shown = all.stream()
                .filter(d -> ALL.equals(statusFilter.getValue()) || Formats.status(d).equals(statusFilter.getValue()))
                .filter(d -> search.isEmpty() || (d.getDonor().getName() + " " + Formats.what(d))
                        .toLowerCase(Locale.ROOT).contains(search))
                .sorted(Comparator.comparingInt(Donation::getPriority).reversed().thenComparing(Donation::getId))
                .toList();
        table.getItems().setAll(shown);
        if (selected != null) {
            shown.stream().filter(d -> d.getId() == selected.getId()).findFirst()
                    .ifPresent(d -> table.getSelectionModel().select(d));
        }
        updateButtons(table.getSelectionModel().getSelectedItem());
    }

    /** Donations waiting for review, shown next to "Donations" in the sidebar. */
    @Override
    public String badge() {
        long waiting = count(donations.getAllDonations(session.getUser()), DonationStatus.SUBMITTED);
        return waiting == 0 ? "" : String.valueOf(waiting);
    }

    private void updateCards(List<Donation> all) {
        LocalDate today = LocalDate.now();
        waitingValue.setText(String.valueOf(count(all, DonationStatus.SUBMITTED)));
        approvedValue.setText(String.valueOf(count(all, DonationStatus.APPROVED)));
        pickedUpValue.setText(String.valueOf(count(all, DonationStatus.PICKED_UP)));
        expiringValue.setText(String.valueOf(all.stream().filter(d -> Formats.nearExpiry(d, today)).count()));
        BigDecimal money = all.stream()
                .filter(d -> !DonationStatus.REJECTED.equals(Formats.status(d)))
                .map(Donation::getBaseDonation)
                .filter(d -> d instanceof MoneyDonation m && m.getAmount() != null)
                .map(d -> ((MoneyDonation) d).getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        moneyValue.setText(money.stripTrailingZeros().toPlainString());
    }

    private static long count(List<Donation> all, String status) {
        return all.stream().filter(d -> status.equals(Formats.status(d))).count();
    }

    private static boolean isUrgent(Donation donation) {
        String status = Formats.status(donation);
        boolean active = !DonationStatus.REJECTED.equals(status) && !DonationStatus.DELIVERED.equals(status);
        return active && (donation.getPriority() >= HIGH_PRIORITY || Formats.nearExpiry(donation, LocalDate.now()));
    }

    @FXML
    private void approve() {
        change(id -> donations.approve(session.getUser(), id), "approved");
    }

    @FXML
    private void reject() {
        Controls.ask(table, "Reject donation", "Why is this donation rejected?", "Reason the donor will see")
                .ifPresent(reason -> change(id -> donations.reject(session.getUser(), id, reason), "rejected"));
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
            long id = selectedId();
            Controls.showDocument(table, "Receipt for donation #" + id,
                    receipts.getReceipt(session.getUser(), id).toText());
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
        pickedUpButton.setDisable(!DonationStatus.APPROVED.equals(status));
        deliveredButton.setDisable(!DonationStatus.PICKED_UP.equals(status));
        receiptButton.setDisable(selected == null || !ReceiptService.isAccepted(selected));
    }
}
