package com.hitacal.ui;

import com.hitacal.model.Goal;
import com.hitacal.service.GoalService;
import javafx.fxml.*;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class GoalSetupController implements Initializable {
    private static final Logger log = LoggerFactory.getLogger(GoalSetupController.class);

    @FXML private TextField startWeightField, targetWeightField, heightField;
    @FXML private ComboBox<String> rateCombo, activityCombo;
    @FXML private Label bmiLabel, rateWarningLabel, projDateLabel, calTargetLabel, weeksLabel, errorLabel;

    private final GoalService goalService = new GoalService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        rateCombo.getItems().addAll("0.25 kg/week (gentle)", "0.5 kg/week (moderate)",
                                    "0.75 kg/week (steady)", "1.0 kg/week (fast)");
        rateCombo.setValue("0.5 kg/week (moderate)");

        activityCombo.getItems().addAll("Sedentary (desk job, little exercise)",
                                         "Lightly active (light exercise 1–3 days/week)",
                                         "Moderately active (exercise 3–5 days/week)",
                                         "Very active (hard exercise 6–7 days/week)");
        activityCombo.setValue("Lightly active (light exercise 1–3 days/week)");

        // Live preview on every field change
        javafx.beans.value.ChangeListener<String> preview = (obs, o, n) -> updatePreview();
        startWeightField.textProperty().addListener(preview);
        targetWeightField.textProperty().addListener(preview);
        heightField.textProperty().addListener(preview);
        rateCombo.valueProperty().addListener((obs, o, n) -> updatePreview());
        activityCombo.valueProperty().addListener((obs, o, n) -> updatePreview());
    }

    private void updatePreview() {
        try {
            double start  = Double.parseDouble(startWeightField.getText().trim());
            double target = Double.parseDouble(targetWeightField.getText().trim());
            double rate   = parseRate();
            int    activ  = activityCombo.getSelectionModel().getSelectedIndex() + 1;
            double height = heightField.getText().isBlank() ? 165 :
                            Double.parseDouble(heightField.getText().trim());

            rateWarningLabel.setText(goalService.isRateUnsafe(rate)
                ? "⚠ Rate above 1 kg/week may be unsafe. Consult a healthcare professional." : "");

            // BMI suggestion
            double[] bmi = goalService.bmiIdealRange(height);
            if (bmi != null)
                bmiLabel.setText(String.format("BMI 18.5–24.9 ideal range: %.1f – %.1f kg  (disclaimer: BMI is a rough estimate)", bmi[0], bmi[1]));

            Goal g = goalService.buildGoal(
                AppSession.get().getCurrentUser().getId(), start, target, height, rate, activ);
            projDateLabel.setText("📅 Projected date: " + g.getProjectedEndDate());
            calTargetLabel.setText("🎯 Daily calorie target: " + g.getCalorieTarget() + " kcal");
            long weeks = java.time.temporal.ChronoUnit.WEEKS.between(g.getStartDate(), g.getProjectedEndDate());
            weeksLabel.setText("That's approximately " + weeks + " weeks from today.");
            errorLabel.setText("");
        } catch (NumberFormatException ignored) {
            projDateLabel.setText("Projected date: enter valid weights");
            calTargetLabel.setText("Daily calorie target: —");
        }
    }

    @FXML private void onSave() {
        errorLabel.setText("");
        try {
            double start  = Double.parseDouble(startWeightField.getText().trim());
            double target = Double.parseDouble(targetWeightField.getText().trim());
            double rate   = parseRate();
            int    activ  = activityCombo.getSelectionModel().getSelectedIndex() + 1;
            double height = heightField.getText().isBlank() ? 0 :
                            Double.parseDouble(heightField.getText().trim());

            if (Math.abs(target - start) < 0.5)
                throw new IllegalArgumentException("Target weight must differ from current by at least 0.5 kg.");

            Goal g = goalService.buildGoal(
                AppSession.get().getCurrentUser().getId(), start, target, height, rate, activ);
            goalService.save(g);

            Alert a = new Alert(Alert.AlertType.INFORMATION,
                "Goal saved! Your target: " + g.getCalorieTarget() + " kcal/day\nProjected date: " + g.getProjectedEndDate(),
                ButtonType.OK);
            a.setHeaderText("Goal Set 🐢"); a.showAndWait();
            Navigator.goTo("dashboard", 1200, 750);
        } catch (Exception e) {
            errorLabel.setText(e.getMessage());
            log.warn("Goal save failed: {}", e.getMessage());
        }
    }

    @FXML private void onBack() { Navigator.goTo("dashboard", 1200, 750); }

    private double parseRate() {
        String v = rateCombo.getValue();
        return Double.parseDouble(v.split(" ")[0]);
    }
}
