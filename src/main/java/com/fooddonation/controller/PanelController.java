package com.fooddonation.controller;

import com.fooddonation.ui.Navigator;
import com.fooddonation.ui.Session;
import com.fooddonation.ui.View;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;

/**
 * Shared shell for the donor and coordinator panels: a sidebar menu with the signed-in user and a
 * sign-out button, and pages whose content comes from their own FXML file and controller.
 * The pages live in a TabPane whose tab header is hidden; the sidebar buttons switch between them.
 */
public abstract class PanelController {
    protected final Session session;
    private final Navigator navigator;
    private final ToggleGroup menu = new ToggleGroup();
    private final Map<Tab, ToggleButton> buttons = new HashMap<>();
    private final Map<Tab, Label> badges = new HashMap<>();

    @FXML protected TabPane tabs;
    @FXML private VBox nav;
    @FXML private Label userName;
    @FXML private Label userEmail;

    protected PanelController(Session session, Navigator navigator) {
        this.session = session;
        this.navigator = navigator;
    }

    /** Which controller belongs to which tab, so the tab can be refreshed when it is opened. */
    protected abstract Map<Tab, Refreshable> tabControllers();

    /** The tab shown as the main action button at the top of the menu, or null for none. */
    protected Tab primaryTab() {
        return null;
    }

    @FXML
    protected void initialize() {
        userName.setText(session.getUser().getName());
        userEmail.setText(session.getUser().getEmail());
        for (Tab tab : tabs.getTabs()) {
            nav.getChildren().add(menuButton(tab));
        }
        // clicking the open page again must not leave the menu with nothing selected
        menu.selectedToggleProperty().addListener((obs, old, now) -> {
            if (now == null && old != null) {
                old.setSelected(true);
            }
        });
        tabs.getSelectionModel().selectedItemProperty().addListener((obs, old, tab) -> opened(tab));
        opened(tabs.getSelectionModel().getSelectedItem());
    }

    protected void open(Tab tab) {
        tabs.getSelectionModel().select(tab);
    }

    private void opened(Tab tab) {
        buttons.get(tab).setSelected(true);
        Refreshable controller = tabControllers().get(tab);
        if (controller != null) {
            controller.refresh();
        }
        updateBadges();
    }

    private ToggleButton menuButton(Tab tab) {
        Label text = new Label(tab.getText());
        Label badge = new Label();
        badge.getStyleClass().add("nav-badge");
        badge.managedProperty().bind(badge.visibleProperty());
        badge.setVisible(false);
        Pane gap = new Pane();
        HBox.setHgrow(gap, Priority.ALWAYS);
        HBox content = new HBox(8, text, gap, badge);
        content.setAlignment(tab == primaryTab() ? Pos.CENTER : Pos.CENTER_LEFT);
        content.setPrefWidth(172);

        ToggleButton button = new ToggleButton();
        button.setGraphic(content);
        button.setToggleGroup(menu);
        button.setMaxWidth(Double.MAX_VALUE);
        button.getStyleClass().setAll(tab == primaryTab() ? "nav-primary" : "nav-item");
        button.setOnAction(e -> open(tab));
        if (tab == primaryTab()) {
            VBox.setMargin(button, new Insets(0, 0, 12, 0));
        }
        buttons.put(tab, button);
        badges.put(tab, badge);
        return button;
    }

    private void updateBadges() {
        tabControllers().forEach((tab, controller) -> {
            String count = controller.badge();
            Label badge = badges.get(tab);
            badge.setText(count);
            badge.setVisible(!count.isEmpty());
        });
    }

    @FXML
    private void signOut() {
        session.signOut();
        navigator.show(View.LOGIN);
    }
}
