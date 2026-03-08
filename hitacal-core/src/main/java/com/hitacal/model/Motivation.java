package com.hitacal.model;

public class Motivation {
    private int id;
    private String template;
    private int minStreak;
    private double minProgressPct;
    private String tag; // DAILY, GOAL, STREAK, CHEERING

    public Motivation() {}
    public Motivation(int id, String template, int minStreak, double minProgressPct, String tag) {
        this.id = id; this.template = template; this.minStreak = minStreak;
        this.minProgressPct = minProgressPct; this.tag = tag;
    }

    public String resolve(String displayName) {
        return template.replace("{name}", displayName);
    }

    public int getId() { return id; }
    public void setId(int v) { this.id = v; }
    public String getTemplate() { return template; }
    public void setTemplate(String v) { this.template = v; }
    public int getMinStreak() { return minStreak; }
    public void setMinStreak(int v) { this.minStreak = v; }
    public double getMinProgressPct() { return minProgressPct; }
    public void setMinProgressPct(double v) { this.minProgressPct = v; }
    public String getTag() { return tag; }
    public void setTag(String v) { this.tag = v; }
}
