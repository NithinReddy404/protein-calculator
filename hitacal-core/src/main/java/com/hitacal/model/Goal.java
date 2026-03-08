package com.hitacal.model;

import java.time.LocalDate;

public class Goal {
    private int id;
    private int userId;
    private double startWeightKg;
    private double targetWeightKg;
    private double heightCm;
    private double weeklyLossRateKg;
    private int calorieTarget;
    private LocalDate startDate;
    private LocalDate projectedEndDate;
    private String notes;

    public Goal() {}

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public double getStartWeightKg() { return startWeightKg; }
    public void setStartWeightKg(double v) { this.startWeightKg = v; }
    public double getTargetWeightKg() { return targetWeightKg; }
    public void setTargetWeightKg(double v) { this.targetWeightKg = v; }
    public double getHeightCm() { return heightCm; }
    public void setHeightCm(double v) { this.heightCm = v; }
    public double getWeeklyLossRateKg() { return weeklyLossRateKg; }
    public void setWeeklyLossRateKg(double v) { this.weeklyLossRateKg = v; }
    public int getCalorieTarget() { return calorieTarget; }
    public void setCalorieTarget(int v) { this.calorieTarget = v; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate v) { this.startDate = v; }
    public LocalDate getProjectedEndDate() { return projectedEndDate; }
    public void setProjectedEndDate(LocalDate v) { this.projectedEndDate = v; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
