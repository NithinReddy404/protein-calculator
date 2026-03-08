package com.hitacal.ui;

import com.hitacal.infra.DatabaseManager;
import com.hitacal.model.User;
import com.hitacal.repository.FoodRepository;
import com.hitacal.repository.UserRepository;
import com.hitacal.service.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.*;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class AdminController implements Initializable {
    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    @FXML private Label totalUsersLabel, foodCountLabel, dbSizeLabel;
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User,String> colUsername, colDisplay, colAdmin, colCreated, colLastLogin;

    private final UserRepository userRepo = new UserRepository();
    private final FoodRepository foodRepo = new FoodRepository();
    private final AuthService    authService = new AuthService();
    private final LogService     logService  = new LogService();
    private final GoalService    goalService = new GoalService();
    private final ExcelExportService excelService = new ExcelExportService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colUsername.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUsername()));
        colDisplay.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDisplayName()));
        colAdmin.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isAdmin() ? "✓" : ""));
        colCreated.setCellValueFactory(d -> new SimpleStringProperty(
            d.getValue().getCreatedAt() != null ? d.getValue().getCreatedAt().toString().substring(0,10) : "—"));
        colLastLogin.setCellValueFactory(d -> new SimpleStringProperty(
            d.getValue().getLastLoginAt() != null ? d.getValue().getLastLoginAt().toString().substring(0,16) : "Never"));
        loadData();
    }

    private void loadData() {
        try {
            var users = userRepo.findAll();
            usersTable.setItems(FXCollections.observableArrayList(users));
            totalUsersLabel.setText(String.valueOf(users.size()));
            foodCountLabel.setText(String.valueOf(foodRepo.count()));
            File db = new File(DatabaseManager.getDbPath());
            dbSizeLabel.setText(db.exists() ? (db.length() / 1024) + " KB" : "—");
        } catch (Exception e) { log.error("Admin load failed", e); }
    }

    @FXML private void onResetPassword() {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showInfo("Select a user first."); return; }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reset Password");
        dialog.setHeaderText("Reset password for: " + selected.getDisplayName());
        dialog.setContentText("New password:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(pw -> {
            try {
                authService.changePassword(selected, pw);
                showInfo("Password reset for " + selected.getDisplayName());
            } catch (Exception e) { showError(e.getMessage()); }
        });
    }

    @FXML private void onExportUser() {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showInfo("Select a user first."); return; }
        try {
            var logs = logService.getAllLogs(selected.getId());
            var goal = goalService.getLatest(selected.getId());
            File f   = excelService.exportForUser(selected, logs, goal);
            showInfo("Excel exported to:\n" + f.getAbsolutePath());
        } catch (Exception e) { showError("Export failed: " + e.getMessage()); }
    }

    @FXML private void onCreateUser() {
        Dialog<ButtonType> d = new Dialog<>();
        d.setTitle("Create User"); d.setHeaderText("New User Account");
        TextField un = new TextField(); un.setPromptText("username");
        TextField dn = new TextField(); dn.setPromptText("Display Name");
        PasswordField pw = new PasswordField(); pw.setPromptText("Password");
        javafx.scene.layout.VBox box = new javafx.scene.layout.VBox(10,
            new Label("Username:"), un, new Label("Display Name:"), dn, new Label("Password:"), pw);
        box.setStyle("-fx-padding:20;");
        d.getDialogPane().setContent(box);
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        d.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                try {
                    authService.register(un.getText().trim(), dn.getText().trim(), pw.getText());
                    showInfo("User created: " + un.getText());
                    loadData();
                } catch (Exception e) { showError(e.getMessage()); }
            }
        });
    }

    @FXML private void onBack() { Navigator.goTo("login", 480, 580); }

    private void showError(String msg) { new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait(); }
    private void showInfo(String msg)  { new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait(); }
}
