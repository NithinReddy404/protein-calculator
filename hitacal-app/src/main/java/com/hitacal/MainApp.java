package com.hitacal;

import com.hitacal.infra.DatabaseMigration;
import com.hitacal.infra.DatabaseManager;
import com.hitacal.infra.SeedData;
import com.hitacal.ui.AppSession;
import com.hitacal.ui.Navigator;
import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainApp extends Application {
    private static final Logger log = LoggerFactory.getLogger(MainApp.class);

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("hitacal AI 🐢");
        primaryStage.setMinWidth(480);
        primaryStage.setMinHeight(500);
        AppSession.get().setPrimaryStage(primaryStage);
        Navigator.goTo("login", 480, 580);
    }

    @Override
    public void stop() throws Exception {
        DatabaseManager.getInstance().close();
        log.info("hitacal AI shutdown.");
    }

    public static void main(String[] args) {
        // Init DB before UI
        try {
            DatabaseMigration.migrate();
            SeedData.seed();
            log.info("Database ready.");
        } catch (Exception e) {
            log.error("Database init failed", e);
            System.exit(1);
        }
        launch(args);
    }
}
