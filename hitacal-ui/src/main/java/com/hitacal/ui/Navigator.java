package com.hitacal.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Navigator {
    private static final Logger log = LoggerFactory.getLogger(Navigator.class);

    public static void goTo(String fxmlName) {
        goTo(fxmlName, 1100, 700);
    }

    public static void goTo(String fxmlName, double w, double h) {
        try {
            FXMLLoader loader = new FXMLLoader(
                Navigator.class.getResource("/fxml/" + fxmlName + ".fxml"));
            Parent root = loader.load();
            Stage stage = AppSession.get().getPrimaryStage();
            Scene scene = new Scene(root, w, h);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            log.error("Failed to load FXML: {}", fxmlName, e);
        }
    }
}
