package com.fooddonation.controller;

import com.fooddonation.model.lookup.LookupEntry;
import com.fooddonation.model.lookup.LookupType;
import com.fooddonation.persistence.LookupRepository;
import com.fooddonation.ui.Controls;
import com.fooddonation.ui.Formats;
import com.fooddonation.ui.Session;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

/** DON-7: create, read, update and delete food categories, storage types and donation statuses. */
public class LookupTablesController implements Refreshable {
    private final LookupRepository lookups;
    private final Session session;

    @FXML private ComboBox<LookupType> typeBox;
    @FXML private TableView<LookupEntry> table;
    @FXML private TableColumn<LookupEntry, String> idColumn;
    @FXML private TableColumn<LookupEntry, String> nameColumn;
    @FXML private TableColumn<LookupEntry, String> systemColumn;
    @FXML private TextField nameField;
    @FXML private Button renameButton;
    @FXML private Button deleteButton;
    @FXML private Label message;

    public LookupTablesController(LookupRepository lookups, Session session) {
        this.lookups = lookups;
        this.session = session;
    }

    @FXML
    private void initialize() {
        typeBox.getItems().setAll(LookupType.values());
        typeBox.setValue(LookupType.FOOD_CATEGORY);
        Controls.showAs(typeBox, Formats::label);
        typeBox.valueProperty().addListener((obs, old, type) -> refresh());

        Controls.column(idColumn, e -> String.valueOf(e.getId()));
        Controls.column(nameColumn, LookupEntry::getName);
        Controls.column(systemColumn, e -> e.isSystem() ? "Protected" : "");
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            nameField.setText(selected == null ? "" : selected.getName());
            boolean editable = selected != null && !selected.isSystem();
            renameButton.setDisable(!editable);
            deleteButton.setDisable(!editable);
        });
        renameButton.setDisable(true);
        deleteButton.setDisable(true);
    }

    @Override
    public void refresh() {
        table.getItems().setAll(lookups.readAll(typeBox.getValue()));
    }

    @FXML
    private void add() {
        try {
            LookupEntry entry = lookups.create(session.getUser(), typeBox.getValue(), nameField.getText());
            Controls.ok(message, "Added \"" + entry.getName() + "\".");
            refresh();
            nameField.clear();
        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
            Controls.error(message, e);
        }
    }

    @FXML
    private void rename() {
        LookupEntry selected = table.getSelectionModel().getSelectedItem();
        try {
            lookups.update(session.getUser(), typeBox.getValue(), selected.getId(), nameField.getText());
            Controls.ok(message, "Renamed to \"" + nameField.getText().trim() + "\".");
            refresh();
        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
            Controls.error(message, e);
        }
    }

    @FXML
    private void delete() {
        LookupEntry selected = table.getSelectionModel().getSelectedItem();
        try {
            lookups.delete(session.getUser(), typeBox.getValue(), selected.getId());
            Controls.ok(message, "Deleted \"" + selected.getName() + "\".");
            refresh();
        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
            Controls.error(message, e);
        }
    }
}
