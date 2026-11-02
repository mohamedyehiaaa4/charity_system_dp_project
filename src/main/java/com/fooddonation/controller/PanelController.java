package com.fooddonation.controller;

import com.fooddonation.ui.Formats;
import com.fooddonation.ui.Navigator;
import com.fooddonation.ui.Session;
import com.fooddonation.ui.View;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.util.Map;

/**
 * Shared shell for the donor and coordinator panels: a header with the signed-in user and a
 * sign-out button, and tabs whose content comes from their own FXML file and controller.
 */
public abstract class PanelController {
    protected final Session session;
    private final Navigator navigator;

    @FXML protected TabPane tabs;
    @FXML private Label userLabel;

    protected PanelController(Session session, Navigator navigator) {
        this.session = session;
        this.navigator = navigator;
    }

    /** Which controller belongs to which tab, so the tab can be refreshed when it is opened. */
    protected abstract Map<Tab, Refreshable> tabControllers();

    @FXML
    protected void initialize() {
        userLabel.setText(session.getUser().getName() + " · " + Formats.label(session.getUser().getRole()));
        Map<Tab, Refreshable> controllers = tabControllers();
        tabs.getSelectionModel().selectedItemProperty().addListener((obs, old, tab) -> {
            Refreshable controller = controllers.get(tab);
            if (controller != null) {
                controller.refresh();
            }
        });
        Refreshable first = controllers.get(tabs.getSelectionModel().getSelectedItem());
        if (first != null) {
            first.refresh();
        }
    }

    protected void open(Tab tab) {
        tabs.getSelectionModel().select(tab);
    }

    @FXML
    private void signOut() {
        session.signOut();
        navigator.show(View.LOGIN);
    }
}
