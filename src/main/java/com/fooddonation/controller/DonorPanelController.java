package com.fooddonation.controller;

import com.fooddonation.ui.Navigator;
import com.fooddonation.ui.Session;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;

import java.util.Map;

/** DON-8: the donor control panel. Each tab has its own controller (fx:include). */
public class DonorPanelController extends PanelController {
    @FXML private Tab submitTab;
    @FXML private Tab donationsTab;
    @FXML private Tab inboxTab;
    @FXML private Tab logTab;
    @FXML private Tab profileTab;

    // filled by FXMLLoader from the fx:include ids ("submit" -> submitController)
    @FXML private SubmitDonationController submitController;
    @FXML private MyDonationsController donationsController;
    @FXML private InboxController inboxController;
    @FXML private MessageLogController logController;
    @FXML private ProfileController profileController;

    public DonorPanelController(Session session, Navigator navigator) {
        super(session, navigator);
    }

    @Override
    protected Map<Tab, Refreshable> tabControllers() {
        return Map.of(submitTab, submitController, donationsTab, donationsController,
                inboxTab, inboxController, logTab, logController, profileTab, profileController);
    }

    @Override
    protected Tab primaryTab() {
        return submitTab;
    }

    @Override
    @FXML
    protected void initialize() {
        tabs.getSelectionModel().select(donationsTab); // open on the donor's own donations
        super.initialize();
        // after a submit, show the donor their list with the new donation in it
        submitController.setOnSubmitted(() -> open(donationsTab));
    }
}
