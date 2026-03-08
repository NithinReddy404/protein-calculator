package com.hitacal.model;

public class FoodItem {
    private int fdcId;
    private String description;
    private String category;
    private double calories100g;
    private double protein100g;
    private double fat100g;
    private double carb100g;
    private double fiber100g;
    private String dataType;

    public FoodItem() {}

    public FoodItem(int fdcId, String description, String category,
                    double calories100g, double protein100g, double fat100g,
                    double carb100g, double fiber100g, String dataType) {
        this.fdcId = fdcId; this.description = description; this.category = category;
        this.calories100g = calories100g; this.protein100g = protein100g;
        this.fat100g = fat100g; this.carb100g = carb100g;
        this.fiber100g = fiber100g; this.dataType = dataType;
    }

    public int getFdcId() { return fdcId; }
    public void setFdcId(int v) { this.fdcId = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public String getCategory() { return category; }
    public void setCategory(String v) { this.category = v; }
    public double getCalories100g() { return calories100g; }
    public void setCalories100g(double v) { this.calories100g = v; }
    public double getProtein100g() { return protein100g; }
    public void setProtein100g(double v) { this.protein100g = v; }
    public double getFat100g() { return fat100g; }
    public void setFat100g(double v) { this.fat100g = v; }
    public double getCarb100g() { return carb100g; }
    public void setCarb100g(double v) { this.carb100g = v; }
    public double getFiber100g() { return fiber100g; }
    public void setFiber100g(double v) { this.fiber100g = v; }
    public String getDataType() { return dataType; }
    public void setDataType(String v) { this.dataType = v; }

    @Override public String toString() { return description + " (" + (int)calories100g + " kcal/100g)"; }
}
