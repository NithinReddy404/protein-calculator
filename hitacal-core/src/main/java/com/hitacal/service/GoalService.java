package com.hitacal.service;

import com.hitacal.model.Goal;
import com.hitacal.repository.GoalRepository;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

public class GoalService {
    private final GoalRepository goalRepo = new GoalRepository();

    public static final double KCAL_PER_KG_FAT = 7700.0;
    public static final double MAX_SAFE_RATE    = 1.0;

    public Goal buildGoal(int userId, double startKg, double targetKg, double heightCm,
                          double rateKgPerWeek, int activityLevel) {
        Goal g = new Goal();
        g.setUserId(userId);
        g.setStartWeightKg(startKg);
        g.setTargetWeightKg(targetKg);
        g.setHeightCm(heightCm);
        g.setWeeklyLossRateKg(rateKgPerWeek);
        g.setStartDate(LocalDate.now());

        double delta = Math.abs(targetKg - startKg);
        long weeks = (long) Math.ceil(delta / rateKgPerWeek);
        g.setProjectedEndDate(LocalDate.now().plusDays(weeks * 7));

        double deficitPerDay = (rateKgPerWeek * KCAL_PER_KG_FAT) / 7.0;
        double tdee = computeTdee(startKg, heightCm, activityLevel);
        int calTarget = (int) Math.max(1200, tdee - deficitPerDay);
        g.setCalorieTarget(calTarget);
        return g;
    }

    public Goal save(Goal g) throws SQLException { return goalRepo.save(g); }

    public Optional<Goal> getLatest(int userId) throws SQLException {
        return goalRepo.findLatestByUser(userId);
    }

    /** Returns [lowKg, highKg] BMI 18.5–24.9 range, or null if height not set */
    public double[] bmiIdealRange(double heightCm) {
        if (heightCm < 100) return null;
        double h = heightCm / 100.0;
        return new double[]{ 18.5 * h * h, 24.9 * h * h };
    }

    public boolean isRateUnsafe(double rateKgPerWeek) { return rateKgPerWeek > MAX_SAFE_RATE; }

    /** Mifflin-St Jeor TDEE; activityLevel 1=sedentary … 4=very active; default female formula */
    private double computeTdee(double weightKg, double heightCm, int activityLevel) {
        double bmr = 10 * weightKg + 6.25 * heightCm - 5 * 30 - 161; // assume female, age 30
        double[] multipliers = { 1.2, 1.375, 1.55, 1.725 };
        int idx = Math.max(0, Math.min(3, activityLevel - 1));
        return bmr * multipliers[idx];
    }

    public double progressPct(Goal g, double currentWeightKg) {
        double total = Math.abs(g.getTargetWeightKg() - g.getStartWeightKg());
        if (total == 0) return 100.0;
        double lost  = Math.abs(g.getStartWeightKg() - currentWeightKg);
        return Math.min(100.0, (lost / total) * 100.0);
    }
}
