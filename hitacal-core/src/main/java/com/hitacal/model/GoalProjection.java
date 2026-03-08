package com.hitacal.model;

import java.time.LocalDate;

public class GoalProjection {
    private final LocalDate projectedEndDate;
    private final int calorieTarget;
    private final int weeksNeeded;
    private final boolean rateExceedsSafeLimit;
    private final double bmiIdealLow;
    private final double bmiIdealHigh;

    public GoalProjection(LocalDate projectedEndDate, int calorieTarget,
                          int weeksNeeded, boolean rateExceedsSafeLimit,
                          double bmiIdealLow, double bmiIdealHigh) {
        this.projectedEndDate     = projectedEndDate;
        this.calorieTarget        = calorieTarget;
        this.weeksNeeded          = weeksNeeded;
        this.rateExceedsSafeLimit = rateExceedsSafeLimit;
        this.bmiIdealLow          = bmiIdealLow;
        this.bmiIdealHigh         = bmiIdealHigh;
    }

    public LocalDate getProjectedEndDate()      { return projectedEndDate; }
    public int getCalorieTarget()               { return calorieTarget; }
    public int getWeeksNeeded()                 { return weeksNeeded; }
    public boolean isRateExceedsSafeLimit()     { return rateExceedsSafeLimit; }
    public double getBmiIdealLow()              { return bmiIdealLow; }
    public double getBmiIdealHigh()             { return bmiIdealHigh; }
}
