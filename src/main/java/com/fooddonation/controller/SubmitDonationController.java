package com.fooddonation.controller;

import com.fooddonation.model.donation.Donation;
import com.fooddonation.model.donation.DonationFactory;
import com.fooddonation.model.donation.DonationRequest;
import com.fooddonation.model.donation.DonationType;
import com.fooddonation.model.donation.Unit;
import com.fooddonation.model.donation.addon.AddOn;
import com.fooddonation.model.lookup.FoodCategory;
import com.fooddonation.model.lookup.LookupType;
import com.fooddonation.model.lookup.StorageType;
import com.fooddonation.service.DonationService;
import com.fooddonation.service.LookupService;
import com.fooddonation.view.Controls;
import com.fooddonation.view.Formats;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * DON-2, DON-3, DON-4, DON-9: the donation form. Step 1 chooses food or money, step 2 is the form.
 * The controller only collects the input; the factory checks it and the decorators add the services.
 * The live summary uses the same factory and decorators to show the priority before submitting.
 */
public class SubmitDonationController implements Refreshable {
    private static final List<DonationType> FOOD_TYPES = List.of(DonationType.COOKED, DonationType.REFRIGERATED,
            DonationType.FRESH, DonationType.PACKAGED);
    private static final List<String> QUICK_AMOUNTS = List.of("100", "250", "500", "1000");
    private static final Map<AddOn, String> ADD_ON_HELP = Map.of(
            AddOn.HOME_PICKUP, "A volunteer collects it from you",
            AddOn.REFRIGERATED_TRANSPORT, "Kept cold on the way",
            AddOn.URGENT_PRIORITY, "Close to expiry, handle it first",
            AddOn.SPECIAL_PACKAGING, "Needs containers or labels");

    private final DonationService donations;
    private final DonationFactory factory;
    private final LookupService lookups;
    private final Session session;
    private Runnable onSubmitted = () -> { };

    private final ToggleGroup foodType = new ToggleGroup();
    private final ToggleGroup quickAmount = new ToggleGroup();
    private final Map<AddOn, ToggleButton> addOnCards = new EnumMap<>(AddOn.class);
    private boolean money;

    @FXML private Button backButton;
    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private VBox chooseStep;
    @FXML private HBox formStep;
    @FXML private VBox foodFields;
    @FXML private VBox moneyFields;
    @FXML private HBox typeTiles;
    @FXML private TextField foodNameField;
    @FXML private TextField quantityField;
    @FXML private ComboBox<Unit> unitBox;
    @FXML private ComboBox<FoodCategory> categoryBox;
    @FXML private ComboBox<StorageType> storageBox;
    @FXML private DatePicker expiryPicker;
    @FXML private DatePicker pickupDatePicker;
    @FXML private ComboBox<String> pickupFromBox;
    @FXML private ComboBox<String> pickupToBox;
    @FXML private TextField pickupAddressField;
    @FXML private GridPane addOnGrid;
    @FXML private HBox amountChips;
    @FXML private TextField amountField;
    @FXML private Label summaryTitle;
    @FXML private Label summaryMeta;
    @FXML private HBox summaryTags;
    @FXML private Label priorityFrom;
    @FXML private Label priorityArrow;
    @FXML private Label priorityTo;
    @FXML private Label priorityUp;
    @FXML private Label priorityHow;
    @FXML private VBox pickupSummary;
    @FXML private Label summaryPickup;
    @FXML private Label summaryAddress;
    @FXML private VBox expirySummary;
    @FXML private Label summaryExpiry;
    @FXML private VBox receiptSummary;
    @FXML private Button submitButton;
    @FXML private Label message;

    public SubmitDonationController(DonationService donations, DonationFactory factory, LookupService lookups,
                                    Session session) {
        this.donations = donations;
        this.factory = factory;
        this.lookups = lookups;
        this.session = session;
    }

    public void setOnSubmitted(Runnable onSubmitted) {
        this.onSubmitted = onSubmitted;
    }

    @FXML
    private void initialize() {
        for (DonationType type : FOOD_TYPES) {
            typeTiles.getChildren().add(typeTile(type));
        }
        // one card per add-on, so a new add-on shows up here without changing this screen
        AddOn[] addOns = AddOn.values();
        for (int i = 0; i < addOns.length; i++) {
            addOnGrid.add(addOnCard(addOns[i]), i % 2, i / 2);
        }
        addOnGrid.getColumnConstraints().setAll(half(), half());
        for (String amount : QUICK_AMOUNTS) {
            ToggleButton chip = new ToggleButton(amount);
            chip.getStyleClass().setAll("amount-chip");
            chip.setToggleGroup(quickAmount);
            chip.setOnAction(e -> amountField.setText(chip.isSelected() ? amount : ""));
            amountChips.getChildren().add(chip);
        }

        unitBox.getItems().setAll(Unit.values());
        Controls.showAs(unitBox, Formats::label);
        Controls.showAs(categoryBox, Formats::label);
        Controls.showAs(storageBox, Formats::label);
        List<String> times = new ArrayList<>();
        for (LocalTime t = LocalTime.of(7, 0); !t.isAfter(LocalTime.of(22, 0)); t = t.plusMinutes(30)) {
            times.add(t.toString());
        }
        pickupFromBox.getItems().setAll(times);
        pickupToBox.getItems().setAll(times);
        pickupAddressField.setPromptText("Empty = " + session.getUser().getAddress() + " (your profile)");

        // the summary follows every change
        foodType.selectedToggleProperty().addListener((obs, old, now) -> {
            if (now == null && old != null) {
                old.setSelected(true); // one food type is always chosen
            }
            updateSummary();
        });
        List.of(foodNameField.textProperty(), quantityField.textProperty(), pickupAddressField.textProperty())
                .forEach(p -> p.addListener((obs, old, now) -> updateSummary()));
        amountField.textProperty().addListener((obs, old, now) -> {
            quickAmount.getToggles().forEach(t -> t.setSelected(((ToggleButton) t).getText().equals(now)));
            updateSummary();
        });
        List.of(unitBox.valueProperty(), categoryBox.valueProperty(), storageBox.valueProperty(),
                        expiryPicker.valueProperty(), pickupDatePicker.valueProperty(),
                        pickupFromBox.valueProperty(), pickupToBox.valueProperty())
                .forEach(p -> p.addListener((obs, old, now) -> updateSummary()));

        clearForm();
        showChoice();
    }

    /** Categories and storage types can be edited by the coordinator, so reload them each time. */
    @Override
    public void refresh() {
        FoodCategory category = categoryBox.getValue();
        StorageType storage = storageBox.getValue();
        categoryBox.getItems().setAll(lookups.getAll(LookupType.FOOD_CATEGORY).stream()
                .map(FoodCategory.class::cast).toList());
        storageBox.getItems().setAll(lookups.getAll(LookupType.STORAGE_TYPE).stream()
                .map(StorageType.class::cast).toList());
        categoryBox.setValue(category == null ? null : categoryBox.getItems().stream()
                .filter(c -> c.getId() == category.getId()).findFirst().orElse(null));
        storageBox.setValue(storage == null ? null : storageBox.getItems().stream()
                .filter(s -> s.getId() == storage.getId()).findFirst().orElse(null));
        updateSummary();
    }

    // ---------- steps ----------

    @FXML
    private void chooseFood() {
        money = false;
        showForm("Donate food", "Step 2 of 2 · Fill in the details. The summary on the right updates as you type.");
    }

    @FXML
    private void chooseMoney() {
        money = true;
        showForm("Donate money", "Step 2 of 2 · Choose or type an amount.");
    }

    @FXML
    private void back() {
        showChoice();
    }

    private void showChoice() {
        show(chooseStep, true);
        show(formStep, false);
        show(backButton, false);
        titleLabel.setText("New donation");
        subtitleLabel.setText("Step 1 of 2");
        Controls.clear(message);
    }

    private void showForm(String title, String subtitle) {
        show(chooseStep, false);
        show(formStep, true);
        show(backButton, true);
        show(foodFields, !money);
        show(moneyFields, money);
        show(pickupSummary, !money);
        show(expirySummary, !money);
        show(receiptSummary, money);
        titleLabel.setText(title);
        subtitleLabel.setText(subtitle);
        Controls.clear(message);
        updateSummary();
    }

    private static void show(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    // ---------- submit ----------

    @FXML
    private void submit() {
        try {
            Donation donation = donations.submitDonation(session.getUser(), readForm(), selectedAddOns());
            Controls.ok(message, "Donation #" + donation.getId() + " submitted.");
            clearForm();
            showChoice();
            onSubmitted.run();
        } catch (IllegalArgumentException | SecurityException e) {
            Controls.error(message, e);
        }
    }

    @FXML
    private void clearForm() {
        foodType.selectToggle(foodType.getToggles().get(0));
        foodNameField.clear();
        quantityField.clear();
        unitBox.setValue(Unit.KG);
        expiryPicker.setValue(LocalDate.now().plusDays(2));
        pickupDatePicker.setValue(LocalDate.now().plusDays(1));
        pickupFromBox.setValue("10:00");
        pickupToBox.setValue("12:00");
        pickupAddressField.clear();
        amountField.clear();
        addOnCards.values().forEach(card -> card.setSelected(false));
        Controls.clear(message);
        updateSummary();
    }

    /** Turns the fields into a request. Missing values stay null so the factory reports them. */
    private DonationRequest readForm() {
        DonationRequest request = new DonationRequest();
        request.setType(selectedType());
        if (money) {
            request.setAmount(number(amountField.getText(), "Amount") == null ? null
                    : new BigDecimal(amountField.getText().trim()));
            return request;
        }
        request.setFoodName(foodNameField.getText());
        request.setQuantity(number(quantityField.getText(), "Quantity"));
        request.setUnit(unitBox.getValue());
        request.setCategory(categoryBox.getValue());
        request.setStorageType(storageBox.getValue());
        request.setExpiry(expiryPicker.getValue());
        LocalDate day = pickupDatePicker.getValue();
        request.setPickupStart(at(day, pickupFromBox.getValue()));
        request.setPickupEnd(at(day, pickupToBox.getValue()));
        request.setPickupAddress(pickupAddressField.getText());
        return request;
    }

    private DonationType selectedType() {
        if (money) {
            return DonationType.MONEY;
        }
        return foodType.getSelectedToggle() == null ? DonationType.COOKED
                : (DonationType) foodType.getSelectedToggle().getUserData();
    }

    private Set<AddOn> selectedAddOns() {
        Set<AddOn> addOns = EnumSet.noneOf(AddOn.class);
        if (!money) {
            addOnCards.forEach((addOn, card) -> {
                if (card.isSelected()) {
                    addOns.add(addOn);
                }
            });
        }
        return addOns;
    }

    // ---------- live summary ----------

    /** Priority of a preview donation built by the real factory and wrapped by the real decorators. */
    private int priority(DonationType type, Set<AddOn> addOns) {
        DonationRequest request = new DonationRequest();
        request.setType(type);
        return factory.restore(request, 0, null, null, null, addOns).getPriority();
    }

    private void updateSummary() {
        if (summaryTitle == null) {
            return; // still loading
        }
        DonationType type = selectedType();
        Set<AddOn> addOns = selectedAddOns();
        int base = priority(type, Set.of());
        int total = priority(type, addOns);

        if (money) {
            String amount = amountField.getText() == null ? "" : amountField.getText().trim();
            summaryTitle.setText("Money donation");
            summaryMeta.setText(amount.isEmpty() ? "No amount yet" : amount);
            priorityHow.setText("Money doesn't expire, so it has the lowest priority.");
            submitButton.setText(amount.isEmpty() ? "Donate" : "Donate " + amount);
        } else {
            String name = foodNameField.getText() == null || foodNameField.getText().isBlank()
                    ? "Food donation" : foodNameField.getText().trim();
            summaryTitle.setText(name);
            List<String> meta = new ArrayList<>();
            if (quantityField.getText() != null && !quantityField.getText().isBlank()) {
                meta.add(quantityField.getText().trim() + " " + Formats.label(unitBox.getValue()).toLowerCase());
            }
            meta.add(Formats.label(type));
            if (categoryBox.getValue() != null) {
                meta.add(categoryBox.getValue().getName());
            }
            summaryMeta.setText(String.join(" · ", meta));
            StringBuilder how = new StringBuilder(Formats.label(type) + " " + base);
            for (AddOn addOn : addOns) {
                how.append(" + ").append(Formats.label(addOn)).append(" ")
                        .append(priority(type, EnumSet.of(addOn)) - base);
            }
            priorityHow.setText(how.toString());
            summaryPickup.setText(dayName(pickupDatePicker.getValue()) + ", " + pickupFromBox.getValue()
                    + "–" + pickupToBox.getValue());
            String address = pickupAddressField.getText();
            summaryAddress.setText(address == null || address.isBlank() ? session.getUser().getAddress() : address);
            summaryExpiry.setText(Formats.date(expiryPicker.getValue()));
            submitButton.setText("Submit donation");
        }

        summaryTags.getChildren().setAll(Controls.tags(addOns.stream().map(Formats::label).toList()));
        boolean raised = total != base;
        show(priorityFrom, raised);
        show(priorityArrow, raised);
        show(priorityUp, raised);
        priorityFrom.setText(String.valueOf(base));
        priorityTo.setText(String.valueOf(total));
        priorityTo.getStyleClass().removeAll("raised");
        if (raised) {
            priorityTo.getStyleClass().add("raised");
        }
        priorityUp.setText("▲ +" + (total - base));
    }

    private static String dayName(LocalDate day) {
        if (day == null) {
            return "No day";
        }
        LocalDate today = LocalDate.now();
        return day.equals(today) ? "Today" : day.equals(today.plusDays(1)) ? "Tomorrow" : Formats.date(day);
    }

    // ---------- building the tiles and cards ----------

    private ToggleButton typeTile(DonationType type) {
        ToggleButton tile = new ToggleButton();
        tile.setGraphic(new VBox(2, Controls.styled(Formats.label(type), "tile-title"),
                Controls.styled("Priority " + priority(type, Set.of()), "meta")));
        tile.getStyleClass().setAll("type-tile");
        tile.setUserData(type);
        tile.setToggleGroup(foodType);
        tile.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(tile, Priority.ALWAYS);
        return tile;
    }

    private ToggleButton addOnCard(AddOn addOn) {
        int bonus = priority(DonationType.COOKED, EnumSet.of(addOn)) - priority(DonationType.COOKED, Set.of());
        Label check = Controls.styled("✓", "check");
        VBox text = new VBox(1, Controls.styled(Formats.label(addOn), "tile-title"),
                Controls.styled(ADD_ON_HELP.getOrDefault(addOn, ""), "meta"));
        Pane gap = new Pane();
        HBox.setHgrow(gap, Priority.ALWAYS);
        HBox content = new HBox(10, check, text, gap, Controls.styled("+" + bonus, "bonus"));
        content.setAlignment(Pos.CENTER_LEFT);
        content.setPrefWidth(300);

        ToggleButton card = new ToggleButton();
        card.setGraphic(content);
        card.getStyleClass().setAll("option-card");
        card.setMaxWidth(Double.MAX_VALUE);
        card.selectedProperty().addListener((obs, old, now) -> updateSummary());
        addOnCards.put(addOn, card);
        return card;
    }

    private static ColumnConstraints half() {
        ColumnConstraints column = new ColumnConstraints();
        column.setPercentWidth(50);
        return column;
    }

    // ---------- parsing ----------

    private static Double number(String text, String field) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(field + " must be a number");
        }
    }

    private static LocalDateTime at(LocalDate day, String time) {
        return day == null || time == null ? null : day.atTime(LocalTime.parse(time));
    }
}
