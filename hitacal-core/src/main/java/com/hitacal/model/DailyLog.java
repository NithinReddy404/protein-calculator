package com.hitacal.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DailyLog {
    private int id;
    private int userId;
    private LocalDate logDate;
    private double totalCalories;
    private double totalProteinG;
    private double totalFatG;
    private double totalCarbG;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<LogEntry> entries = new ArrayList<>();

    public int getId() { return id; }
    public void setId(int v) { this.id = v; }
    public int getUserId() { return userId; }
    public void setUserId(int v) { this.userId = v; }
    public LocalDate getLogDate() { return logDate; }
    public void setLogDate(LocalDate v) { this.logDate = v; }
    public double getTotalCalories() { return totalCalories; }
    public void setTotalCalories(double v) { this.totalCalories = v; }
    public double getTotalProteinG() { return totalProteinG; }
    public void setTotalProteinG(double v) { this.totalProteinG = v; }
    public double getTotalFatG() { return totalFatG; }
    public void setTotalFatG(double v) { this.totalFatG = v; }
    public double getTotalCarbG() { return totalCarbG; }
    public void setTotalCarbG(double v) { this.totalCarbG = v; }
    public String getNotes() { return notes; }
    public void setNotes(String v) { this.notes = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime v) { this.updatedAt = v; }
    public List<LogEntry> getEntries() { return entries; }
    public void setEntries(List<LogEntry> v) { this.entries = v; }
}
