package com.fooddonation.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;

/** Switches the window between screens. Controllers ask it to move on; they never load FXML themselves. */
public class Navigator {
    private static final String STYLESHEET = "/com/fooddonation/view/styles.css";

    private final Stage stage;
    private Callback<Class<?>, Object> controllerFactory;

    public Navigator(Stage stage) {
        this.stage = stage;
    }

    /** Set once at startup. Every FXML file, including the ones pulled in with fx:include, uses it. */
    public void setControllerFactory(Callback<Class<?>, Object> controllerFactory) {
        this.controllerFactory = controllerFactory;
    }

    public void show(View view) {
        Parent root = load(view.fxmlPath());
        Scene scene = stage.getScene();
        if (scene == null) {
            scene = new Scene(root, 1100, 720);
            scene.getStylesheets().add(resource(STYLESHEET).toExternalForm());
            stage.setScene(scene);
        } else {
            scene.setRoot(root);
        }
        stage.setTitle("Food Donation System · " + view.title());
    }

    private Parent load(String path) {
        FXMLLoader loader = new FXMLLoader(resource(path));
        loader.setControllerFactory(controllerFactory);
        try {
            return loader.load();
        } catch (IOException e) {
            throw new IllegalStateException("Could not load " + path, e);
        }
    }

    private URL resource(String path) {
        URL url = Navigator.class.getResource(path);
        if (url == null) {
            throw new IllegalStateException("Missing resource " + path);
        }
        return url;
    }
}
