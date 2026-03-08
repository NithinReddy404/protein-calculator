package com.hitacal.model;

public class LogEntry {
    private int id;
    private int logId;
    private int fdcId;
    private String foodName;
    private double servingGrams;
    private double calories;
    private double proteinG;
    private double fatG;
    private double carbG;
    private String entryTime;

    public int getId() { return id; }
    public void setId(int v) { this.id = v; }
    public int getLogId() { return logId; }
    public void setLogId(int v) { this.logId = v; }
    public int getFdcId() { return fdcId; }
    public void setFdcId(int v) { this.fdcId = v; }
    public String getFoodName() { return foodName; }
    public void setFoodName(String v) { this.foodName = v; }
    public double getServingGrams() { return servingGrams; }
    public void setServingGrams(double v) { this.servingGrams = v; }
    public double getCalories() { return calories; }
    public void setCalories(double v) { this.calories = v; }
    public double getProteinG() { return proteinG; }
    public void setProteinG(double v) { this.proteinG = v; }
    public double getFatG() { return fatG; }
    public void setFatG(double v) { this.fatG = v; }
    public double getCarbG() { return carbG; }
    public void setCarbG(double v) { this.carbG = v; }
    public String getEntryTime() { return entryTime; }
    public void setEntryTime(String v) { this.entryTime = v; }

    @Override public String toString() {
        return String.format("%s — %.0fg (%.0f kcal)", foodName, servingGrams, calories);
    }
}
