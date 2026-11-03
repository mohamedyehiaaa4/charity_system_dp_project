package com.fooddonation.controller;

import com.fooddonation.model.donation.addon.AddOn;
import com.fooddonation.model.donation.Donation;
import com.fooddonation.model.donation.DonationRequest;
import com.fooddonation.model.donation.DonationType;
import com.fooddonation.model.donation.Unit;
import com.fooddonation.model.lookup.FoodCategory;
import com.fooddonation.model.lookup.LookupType;
import com.fooddonation.model.lookup.StorageType;
import com.fooddonation.persistence.LookupRepository;
import com.fooddonation.service.DonationService;
import com.fooddonation.ui.Controls;
import com.fooddonation.ui.Formats;
import com.fooddonation.ui.Session;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.EnumSet;
import java.util.Set;

/**
 * DON-2, DON-3, DON-4, DON-9: the donation form. The controller only collects the input; the
 * factory checks it and the decorators add the services.
 */
public class SubmitDonationController implements Refreshable {
    private final DonationService donations;
    private final LookupRepository lookups;
    private final Session session;
    private Runnable onSubmitted = () -> { };

    @FXML private ComboBox<DonationType> typeBox;
    @FXML private Pane foodFields;
    @FXML private Pane moneyFields;
    @FXML private TextField foodNameField;
    @FXML private TextField quantityField;
    @FXML private ComboBox<Unit> unitBox;
    @FXML private ComboBox<FoodCategory> categoryBox;
    @FXML private ComboBox<StorageType> storageBox;
    @FXML private DatePicker expiryPicker;
    @FXML private DatePicker pickupDatePicker;
    @FXML private TextField pickupFromField;
    @FXML private TextField pickupToField;
    @FXML private TextField pickupAddressField;
    @FXML private FlowPane addOnBox;
    @FXML private TextField amountField;
    @FXML private Label message;

    public SubmitDonationController(DonationService donations, LookupRepository lookups, Session session) {
        this.donations = donations;
        this.lookups = lookups;
        this.session = session;
    }

    public void setOnSubmitted(Runnable onSubmitted) {
        this.onSubmitted = onSubmitted;
    }

    @FXML
    private void initialize() {
        typeBox.getItems().setAll(DonationType.values());
        Controls.showAs(typeBox, Formats::label);
        typeBox.valueProperty().addListener((obs, old, type) -> showFieldsFor(type));
        unitBox.getItems().setAll(Unit.values());
        Controls.showAs(unitBox, Formats::label);
        Controls.showAs(categoryBox, Formats::label);
        Controls.showAs(storageBox, Formats::label);
        // one check box per add-on, so a new add-on shows up here without changing this screen
        for (AddOn addOn : AddOn.values()) {
            CheckBox box = new CheckBox(Formats.label(addOn));
            box.setUserData(addOn);
            addOnBox.getChildren().add(box);
        }
        clearForm();
    }

    /** Categories and storage types can be edited by the coordinator, so reload them each time. */
    @Override
    public void refresh() {
        FoodCategory category = categoryBox.getValue();
        StorageType storage = storageBox.getValue();
        categoryBox.getItems().setAll(lookups.readAll(LookupType.FOOD_CATEGORY).stream()
                .map(FoodCategory.class::cast).toList());
        storageBox.getItems().setAll(lookups.readAll(LookupType.STORAGE_TYPE).stream()
                .map(StorageType.class::cast).toList());
        categoryBox.setValue(category == null ? null : categoryBox.getItems().stream()
                .filter(c -> c.getId() == category.getId()).findFirst().orElse(null));
        storageBox.setValue(storage == null ? null : storageBox.getItems().stream()
                .filter(s -> s.getId() == storage.getId()).findFirst().orElse(null));
    }

    @FXML
    private void submit() {
        try {
            Donation donation = donations.submitDonation(session.getUser(), readForm(), selectedAddOns());
            Controls.ok(message, "Donation #" + donation.getId() + " submitted: " + donation.getDescription()
                    + " (priority " + donation.getPriority() + ")");
            clearForm();
            onSubmitted.run();
        } catch (IllegalArgumentException | SecurityException e) {
            Controls.error(message, e);
        }
    }

    @FXML
    private void clearForm() {
        typeBox.setValue(DonationType.COOKED);
        foodNameField.clear();
        quantityField.clear();
        unitBox.setValue(Unit.KG);
        expiryPicker.setValue(LocalDate.now().plusDays(2));
        pickupDatePicker.setValue(LocalDate.now().plusDays(1));
        pickupFromField.setText("10:00");
        pickupToField.setText("12:00");
        pickupAddressField.clear();
        amountField.clear();
        addOnBox.getChildren().forEach(node -> ((CheckBox) node).setSelected(false));
    }

    private void showFieldsFor(DonationType type) {
        boolean money = type == DonationType.MONEY;
        foodFields.setVisible(!money);
        foodFields.setManaged(!money);
        moneyFields.setVisible(money);
        moneyFields.setManaged(money);
        addOnBox.setDisable(money);
    }

    /** Turns the text fields into a request. Missing values stay null so the factory reports them. */
    private DonationRequest readForm() {
        DonationRequest request = new DonationRequest();
        request.setType(typeBox.getValue());
        if (typeBox.getValue() == DonationType.MONEY) {
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
        request.setPickupStart(at(day, pickupFromField.getText(), "Pickup from"));
        request.setPickupEnd(at(day, pickupToField.getText(), "Pickup to"));
        request.setPickupAddress(pickupAddressField.getText());
        return request;
    }

    private Set<AddOn> selectedAddOns() {
        Set<AddOn> addOns = EnumSet.noneOf(AddOn.class);
        if (typeBox.getValue() != DonationType.MONEY) {
            addOnBox.getChildren().stream().map(CheckBox.class::cast).filter(CheckBox::isSelected)
                    .forEach(box -> addOns.add((AddOn) box.getUserData()));
        }
        return addOns;
    }

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

    private static LocalDateTime at(LocalDate day, String time, String field) {
        if (day == null || time == null || time.isBlank()) {
            return null;
        }
        try {
            return day.atTime(LocalTime.parse(time.trim()));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(field + " must be a time like 14:30");
        }
    }
}
