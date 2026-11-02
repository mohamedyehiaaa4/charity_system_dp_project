package com.fooddonation.ui;

import com.fooddonation.app.AppConfig;
import com.fooddonation.app.AppContext;
import javafx.application.Application;
import javafx.stage.Stage;

/** The JavaFX application: builds the model once through AppContext, then opens the login screen. */
public class FoodDonationApp extends Application {
    private AppContext context;

    @Override
    public void start(Stage stage) {
        AppConfig config = AppConfig.load();
        context = new AppContext(config);
        context.start();

        Session session = new Session();
        Navigator navigator = new Navigator(stage);
        navigator.setControllerFactory(new ControllerFactory(context, config, session, navigator));

        stage.setMinWidth(900);
        stage.setMinHeight(620);
        navigator.show(View.LOGIN);
        stage.show();
    }

    /** Stops the expiry scheduler and closes the database when the window closes. */
    @Override
    public void stop() {
        if (context != null) {
            context.close();
        }
    }
}
