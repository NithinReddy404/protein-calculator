package com.hitacal.ui;

import com.hitacal.model.User;
import com.hitacal.service.AuthService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {
    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    @FXML private TextField      usernameField;
    @FXML private PasswordField  passwordField;
    @FXML private Label          errorLabel;
    @FXML private Button         loginBtn;
    @FXML private Canvas         turtleCanvas;

    private final AuthService authService = new AuthService();
    private TurtlePane turtle;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        turtle = new TurtlePane(140, 120);
        // Replace canvas with TurtlePane
        javafx.scene.layout.Pane parent = (javafx.scene.layout.Pane) turtleCanvas.getParent();
        int idx = parent.getChildren().indexOf(turtleCanvas);
        parent.getChildren().set(idx, turtle);

        // Press Enter in password field = login
        passwordField.setOnAction(e -> onLogin());
    }

    @FXML
    private void onLogin() {
        errorLabel.setText("");
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter username and password.");
            turtle.setExpression(TurtlePane.Expression.WORRIED);
            return;
        }
        try {
            User user = authService.login(username, password);
            AppSession.get().setCurrentUser(user);
            turtle.setExpression(TurtlePane.Expression.HAPPY);
            // Brief delay so user sees the happy turtle
            Timeline delay = new Timeline(new KeyFrame(Duration.millis(600), e -> {
                if (user.isAdmin()) Navigator.goTo("admin", 1000, 680);
                else Navigator.goTo("dashboard", 1200, 750);
            }));
            delay.play();
        } catch (Exception ex) {
            errorLabel.setText(ex.getMessage());
            turtle.setExpression(TurtlePane.Expression.WORRIED);
            shakeField(usernameField);
        }
    }

    @FXML
    private void onNewUser() {
        showRegisterDialog();
    }

    @FXML
    private void onAdminLogin() {
        usernameField.setText("admin");
        usernameField.requestFocus();
        passwordField.requestFocus();
    }

    private void showRegisterDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Create New Account");
        dialog.setHeaderText("Register for hitacal AI");

        TextField un = new TextField(); un.setPromptText("username (lowercase, no spaces)");
        TextField dn = new TextField(); dn.setPromptText("Display Name");
        PasswordField pw = new PasswordField(); pw.setPromptText("Password (min 6 chars)");

        javafx.scene.layout.VBox box = new javafx.scene.layout.VBox(10,
            new Label("Username:"), un,
            new Label("Display Name:"), dn,
            new Label("Password:"), pw
        );
        box.setStyle("-fx-padding: 20;");
        dialog.getDialogPane().setContent(box);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                try {
                    authService.register(un.getText().trim(), dn.getText().trim(), pw.getText());
                    showInfo("Account created! You can now login.");
                    usernameField.setText(un.getText().trim());
                    turtle.setExpression(TurtlePane.Expression.EXCITED);
                } catch (Exception ex) {
                    showError(ex.getMessage());
                }
            }
        });
    }

    private void shakeField(TextField field) {
        Timeline shake = new Timeline(
            new KeyFrame(Duration.millis(0),   e -> field.setTranslateX(0)),
            new KeyFrame(Duration.millis(60),  e -> field.setTranslateX(-8)),
            new KeyFrame(Duration.millis(120), e -> field.setTranslateX(8)),
            new KeyFrame(Duration.millis(180), e -> field.setTranslateX(-6)),
            new KeyFrame(Duration.millis(240), e -> field.setTranslateX(6)),
            new KeyFrame(Duration.millis(300), e -> field.setTranslateX(0))
        );
        shake.play();
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText("Error"); a.showAndWait();
    }
    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText("Success"); a.showAndWait();
    }
}
