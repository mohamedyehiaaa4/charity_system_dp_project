package com.fooddonation.controller;

import com.fooddonation.model.donation.Donation;
import com.fooddonation.model.donation.FoodDonation;
import com.fooddonation.model.donation.MoneyDonation;
import com.fooddonation.model.lookup.DonationStatus;
import com.fooddonation.service.DonationService;
import com.fooddonation.service.ReceiptService;
import com.fooddonation.view.Controls;
import com.fooddonation.view.Formats;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.time.Year;
import java.util.Comparator;
import java.util.List;

/** DON-8 and DON-10: the donor's own donations with a progress tracker, receipts and the yearly tax summary. */
public class MyDonationsController implements Refreshable {
    private static final List<String> STEPS = List.of(DonationStatus.SUBMITTED, DonationStatus.APPROVED,
            DonationStatus.PICKED_UP, DonationStatus.DELIVERED);

    private final DonationService donations;
    private final ReceiptService receipts;
    private final Session session;

    @FXML private Label totalValue;
    @FXML private Label foodValue;
    @FXML private Label moneyValue;
    @FXML private Label progressValue;
    @FXML private VBox list;
    @FXML private Spinner<Integer> yearSpinner;
    @FXML private Label message;

    public MyDonationsController(DonationService donations, ReceiptService receipts, Session session) {
        this.donations = donations;
        this.receipts = receipts;
        this.session = session;
    }

    @FXML
    private void initialize() {
        int year = Year.now().getValue();
        yearSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(2000, year + 1, year));
    }

    /** Newest first. */
    @Override
    public void refresh() {
        List<Donation> mine = donations.getMyDonations(session.getUser()).stream()
                .sorted(Comparator.comparing(Donation::getId).reversed())
                .toList();
        totalValue.setText(String.valueOf(mine.size()));
        foodValue.setText(String.valueOf(mine.stream().filter(d -> d.getBaseDonation() instanceof FoodDonation).count()));
        BigDecimal money = mine.stream()
                .filter(d -> !DonationStatus.REJECTED.equals(Formats.status(d)))
                .map(Donation::getBaseDonation)
                .filter(d -> d instanceof MoneyDonation m && m.getAmount() != null)
                .map(d -> ((MoneyDonation) d).getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        moneyValue.setText(money.stripTrailingZeros().toPlainString());
        progressValue.setText(String.valueOf(mine.stream().filter(d -> {
            String status = Formats.status(d);
            return !DonationStatus.DELIVERED.equals(status) && !DonationStatus.REJECTED.equals(status);
        }).count()));

        list.getChildren().clear();
        if (mine.isEmpty()) {
            list.getChildren().add(Controls.styled("No donations yet. Use \"+ New donation\" to make one.", "muted"));
        }
        mine.forEach(d -> list.getChildren().add(card(d)));
    }

    /** One donation: what it is on the left, where it is in the lifecycle in the middle, receipt on the right. */
    private Node card(Donation donation) {
        HBox tags = Controls.tags(Formats.addOns(donation));
        VBox info = new VBox(3, Controls.styled(Formats.what(donation), "donation-title"),
                Controls.styled("#" + donation.getId() + " · " + Formats.label(donation.getType())
                        + " · Priority " + donation.getPriority(), "meta"), tags);
        info.setPrefWidth(270);
        info.setMinWidth(220);

        Node middle;
        if (DonationStatus.REJECTED.equals(Formats.status(donation))) {
            String reason = donation.getRejectionReason() == null ? "" : donation.getRejectionReason();
            middle = new VBox(3, Controls.styled("✕ Rejected", "rejected-text"),
                    Controls.styled("Reason: " + reason, "meta"));
        } else {
            middle = tracker(Formats.status(donation));
        }
        HBox.setHgrow(middle, Priority.ALWAYS);

        Button receipt = new Button("Receipt");
        receipt.getStyleClass().add("small");
        receipt.setDisable(!ReceiptService.isAccepted(donation));
        receipt.setOnAction(e -> showReceipt(donation));
        VBox right = new VBox(6, Controls.styled("Submitted " + Formats.dateTime(donation.getCreatedAt()), "meta"),
                receipt);
        right.setAlignment(Pos.CENTER_RIGHT);
        right.setMinWidth(160);

        HBox card = new HBox(info, middle, right);
        card.getStyleClass().add("donation-card");
        return card;
    }

    /** Submitted -> Approved -> Picked up -> Delivered, with the steps done so far filled in. */
    private Node tracker(String status) {
        int reached = Math.max(0, STEPS.indexOf(status));
        HBox track = new HBox();
        track.setAlignment(Pos.TOP_CENTER);
        for (int i = 0; i < STEPS.size(); i++) {
            if (i > 0) {
                Region line = new Region();
                line.getStyleClass().add("step-line");
                if (i <= reached) {
                    line.getStyleClass().add("done");
                }
                HBox.setHgrow(line, Priority.ALWAYS);
                line.setTranslateY(7);
                line.setMinWidth(30);
                track.getChildren().add(line);
            }
            String state = i <= reached ? "done" : i == reached + 1 ? "next" : "";
            Region dot = new Region();
            dot.getStyleClass().add("step-dot");
            Label name = Controls.styled(i == reached + 1 && i == 1 ? "Waiting for review" : STEPS.get(i), "step-label");
            if (!state.isEmpty()) {
                dot.getStyleClass().add(state);
                name.getStyleClass().add(state);
            }
            VBox step = new VBox(5, dot, name);
            step.setAlignment(Pos.TOP_CENTER);
            step.setMinWidth(90);
            track.getChildren().add(step);
        }
        return track;
    }

    private void showReceipt(Donation donation) {
        try {
            Controls.showDocument(list, "Receipt for donation #" + donation.getId(),
                    receipts.getReceipt(session.getUser(), donation.getId()).toText());
            Controls.clear(message);
        } catch (IllegalStateException | IllegalArgumentException | SecurityException e) {
            Controls.error(message, e);
        }
    }

    @FXML
    private void showAnnualSummary() {
        try {
            int year = yearSpinner.getValue();
            Controls.showDocument(list, "Donation summary " + year, receipts.getAnnualSummary(session.getUser(),
                    session.getUser().getId(), year).toText());
            Controls.clear(message);
        } catch (IllegalStateException | SecurityException e) {
            Controls.error(message, e);
        }
    }
}
