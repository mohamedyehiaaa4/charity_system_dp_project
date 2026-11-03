package com.fooddonation.app;

import com.fooddonation.controller.CoordinatorPanelController;
import com.fooddonation.controller.DonorPanelController;
import com.fooddonation.controller.InboxController;
import com.fooddonation.controller.LoginController;
import com.fooddonation.controller.LookupTablesController;
import com.fooddonation.controller.MessageLogController;
import com.fooddonation.controller.MyDonationsController;
import com.fooddonation.controller.ProfileController;
import com.fooddonation.controller.RegisterController;
import com.fooddonation.controller.ReviewDonationsController;
import com.fooddonation.controller.SubmitDonationController;
import com.fooddonation.ui.Navigator;
import com.fooddonation.ui.Session;
import javafx.util.Callback;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Creates the controller named in each FXML file and passes in what it needs, the same way
 * AppContext wires the model. Controllers never reach for globals, so each can be read on its own.
 */
public class ControllerFactory implements Callback<Class<?>, Object> {
    private final Map<Class<?>, Supplier<Object>> controllers = new HashMap<>();

    public ControllerFactory(AppContext app, AppConfig config, Session session, Navigator navigator) {
        controllers.put(LoginController.class, () -> new LoginController(app.getAuth(), session, navigator));
        controllers.put(RegisterController.class, () -> new RegisterController(app.getAuth(), session, navigator));

        controllers.put(DonorPanelController.class, () -> new DonorPanelController(session, navigator));
        controllers.put(SubmitDonationController.class,
                () -> new SubmitDonationController(app.getDonationService(), app.getLookups(), session));
        controllers.put(MyDonationsController.class,
                () -> new MyDonationsController(app.getDonationService(), app.getReceipts(), session));
        controllers.put(ProfileController.class, () -> new ProfileController(app.getAuth(), session));
        controllers.put(InboxController.class, () -> new InboxController(app.getInbox(), session));

        controllers.put(CoordinatorPanelController.class, () -> new CoordinatorPanelController(session, navigator));
        controllers.put(ReviewDonationsController.class,
                () -> new ReviewDonationsController(app.getDonationService(), app.getReceipts(), session));
        controllers.put(LookupTablesController.class, () -> new LookupTablesController(app.getLookups(), session));
        controllers.put(MessageLogController.class,
                () -> new MessageLogController(config.emailLog(), config.smsLog()));
    }

    @Override
    public Object call(Class<?> type) {
        Supplier<Object> controller = controllers.get(type);
        if (controller == null) {
            throw new IllegalStateException("No controller registered for " + type.getName());
        }
        return controller.get();
    }
}
