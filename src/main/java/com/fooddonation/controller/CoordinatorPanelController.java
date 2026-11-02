package com.fooddonation.controller;

import com.fooddonation.ui.Navigator;
import com.fooddonation.ui.Session;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;

import java.util.Map;

/** The coordinator dashboard. Each tab has its own controller (fx:include). */
public class CoordinatorPanelController extends PanelController {
    @FXML private Tab reviewTab;
    @FXML private Tab lookupsTab;
    @FXML private Tab inboxTab;
    @FXML private Tab logTab;
    @FXML private Tab profileTab;

    @FXML private ReviewDonationsController reviewController;
    @FXML private LookupTablesController lookupsController;
    @FXML private InboxController inboxController;
    @FXML private MessageLogController logController;
    @FXML private ProfileController profileController;

    public CoordinatorPanelController(Session session, Navigator navigator) {
        super(session, navigator);
    }

    @Override
    protected Map<Tab, Refreshable> tabControllers() {
        return Map.of(reviewTab, reviewController, lookupsTab, lookupsController, inboxTab, inboxController,
                logTab, logController, profileTab, profileController);
    }
}
