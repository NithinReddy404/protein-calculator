package com.hitacal.ui;

import com.hitacal.model.*;
import com.hitacal.service.*;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DashboardController implements Initializable {
    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);

    @FXML private Label         userGreetLabel, dateLabel;
    @FXML private Label         streakLabel, motivationLabel, totalCalLabel;
    @FXML private Label         proteinLabel, fatLabel, carbLabel;
    @FXML private Label         calGoalLabel, goalWeightLabel, projectedDateLabel;
    @FXML private ProgressBar   calProgress, proteinProgress, fatProgress, carbProgress, weightProgress;
    @FXML private TextField     searchField, servingField, notesField;
    @FXML private ListView<FoodItem>  searchResultsList;
    @FXML private TableView<LogEntry> logTable;
    @FXML private TableColumn<LogEntry,String> colFood;
    @FXML private TableColumn<LogEntry,Number> colGrams, colCal, colProt, colFat;
    @FXML private Canvas        turtleCanvas;

    private TurtlePane turtle;

    private final LogService        logService        = new LogService();
    private final GoalService       goalService       = new GoalService();
    private final MotivationService motivationService = new MotivationService();
    private final ExcelExportService excelService     = new ExcelExportService();

    private User     user;
    private DailyLog todayLog;
    private Goal     currentGoal;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        user = AppSession.get().getCurrentUser();

        // Swap canvas for TurtlePane
        javafx.scene.layout.Pane parent = (javafx.scene.layout.Pane) turtleCanvas.getParent();
        int idx = parent.getChildren().indexOf(turtleCanvas);
        turtle = new TurtlePane(190, 160);
        parent.getChildren().set(idx, turtle);

        // Header
        userGreetLabel.setText("Hello, " + user.getDisplayName() + "!");
        dateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM d yyyy")));

        // Table columns
        colFood.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFoodName()));
        colGrams.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getServingGrams()));
        colCal.setCellValueFactory(d -> new SimpleDoubleProperty(Math.round(d.getValue().getCalories())));
        colProt.setCellValueFactory(d -> new SimpleDoubleProperty(Math.round(d.getValue().getProteinG() * 10.0) / 10.0));
        colFat.setCellValueFactory(d -> new SimpleDoubleProperty(Math.round(d.getValue().getFatG() * 10.0) / 10.0));

        loadData();
    }

    private void loadData() {
        try {
            todayLog = logService.getOrCreateToday(user.getId());
            loadGoal();
            refreshUI();
        } catch (Exception e) {
            log.error("Load error", e);
            showError("Failed to load data: " + e.getMessage());
        }
    }

    private void loadGoal() throws Exception {
        currentGoal = goalService.getLatest(user.getId()).orElse(null);
    }

    private void refreshUI() throws Exception {
        // Streak & motivation
        int streak = logService.getStreak(user.getId());
        streakLabel.setText("🔥 " + streak + " day" + (streak == 1 ? "" : "s") + " streak");
        double progressPct = currentGoal != null
            ? Math.min(100, (todayLog.getTotalCalories() / currentGoal.getCalorieTarget()) * 100) : 0;
        motivationLabel.setText(motivationService.getMessage(user.getDisplayName(), streak, progressPct));

        // Totals
        double cal  = todayLog.getTotalCalories();
        double prot = todayLog.getTotalProteinG();
        double fat  = todayLog.getTotalFatG();
        double carb = todayLog.getTotalCarbG();

        totalCalLabel.setText(String.format("🔥 %.0f kcal", cal));
        proteinLabel.setText(String.format("%.1f g", prot));
        fatLabel.setText(String.format("%.1f g", fat));
        carbLabel.setText(String.format("%.1f g", carb));

        // Progress bars
        if (currentGoal != null) {
            int goalCal = currentGoal.getCalorieTarget();
            calGoalLabel.setText("Goal: " + goalCal + " kcal");
            calProgress.setProgress(Math.min(1.2, cal / goalCal));
            proteinProgress.setProgress(Math.min(1.0, prot / 50.0));
            fatProgress.setProgress(Math.min(1.0, fat / 55.0));
            carbProgress.setProgress(Math.min(1.0, carb / 250.0));

            goalWeightLabel.setText("Target: " + currentGoal.getTargetWeightKg() + " kg");
            projectedDateLabel.setText("Projected: " + currentGoal.getProjectedEndDate());

            // Turtle expression based on progress
            if (progressPct >= 100) turtle.setExpression(TurtlePane.Expression.CHEERING);
            else if (cal > goalCal * 1.2) turtle.setExpression(TurtlePane.Expression.WORRIED);
            else if (streak > 0 && isStreakMilestone(streak)) turtle.setExpression(TurtlePane.Expression.EXCITED);
        }

        // Log table
        logTable.setItems(FXCollections.observableArrayList(todayLog.getEntries()));
    }

    @FXML private void onSearchTyped() {
        String q = searchField.getText().trim();
        if (q.length() >= 2) performSearch(q);
    }

    @FXML private void onSearch() { performSearch(searchField.getText().trim()); }

    private void performSearch(String q) {
        if (q.isEmpty()) return;
        turtle.setExpression(TurtlePane.Expression.THINKING);
        try {
            List<FoodItem> results = logService.searchFood(q);
            searchResultsList.setItems(FXCollections.observableArrayList(results));
            if (results.isEmpty()) showInfo("No foods found for \"" + q + "\". Try a simpler term.");
            else turtle.setExpression(TurtlePane.Expression.IDLE);
        } catch (Exception e) { showError(e.getMessage()); }
    }

    @FXML private void onAddFood() {
        FoodItem selected = searchResultsList.getSelectionModel().getSelectedItem();
        if (selected == null) { showInfo("Please select a food from the search results."); return; }
        double grams;
        try { grams = Double.parseDouble(servingField.getText().trim()); }
        catch (NumberFormatException e) { showError("Enter a valid number for grams."); return; }
        if (grams <= 0) { showError("Grams must be positive."); return; }
        try {
            todayLog = logService.addFood(user.getId(), selected.getFdcId(), grams);
            turtle.setExpression(TurtlePane.Expression.HAPPY);
            refreshUI();
        } catch (Exception e) { showError(e.getMessage()); }
    }

    @FXML private void onRemoveEntry() {
        LogEntry selected = logTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        try {
            logService.deleteEntry(selected.getId(), todayLog.getId());
            todayLog = logService.getDay(user.getId(), LocalDate.now()).orElse(todayLog);
            refreshUI();
        } catch (Exception e) { showError(e.getMessage()); }
    }

    @FXML private void onSaveDay() {
        try {
            turtle.setExpression(TurtlePane.Expression.HAPPY);
            showInfo("Day saved! Great work, " + user.getDisplayName() + " 🐢");
        } catch (Exception e) { showError(e.getMessage()); }
    }

    @FXML private void onExport() {
        try {
            var allLogs = logService.getAllLogs(user.getId());
            var goal    = goalService.getLatest(user.getId());
            var file    = excelService.exportForUser(user, allLogs, goal);
            turtle.setExpression(TurtlePane.Expression.CHEERING);
            showInfo("Excel exported to:\n" + file.getAbsolutePath());
        } catch (Exception e) { showError("Export failed: " + e.getMessage()); }
    }

    @FXML private void onGoalSetup() { Navigator.goTo("goal_setup", 700, 700); }

    @FXML private void onLogout() {
        AppSession.get().setCurrentUser(null);
        Navigator.goTo("login", 480, 580);
    }

    private boolean isStreakMilestone(int s) {
        return s == 7 || s == 14 || s == 21 || s == 30 || s == 60 || s == 90;
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText("Error"); a.showAndWait();
    }
    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText("hitacal AI"); a.showAndWait();
    }
}
