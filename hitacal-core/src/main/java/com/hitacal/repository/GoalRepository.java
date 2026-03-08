package com.hitacal.repository;

import com.hitacal.infra.DatabaseManager;
import com.hitacal.model.Goal;

import java.sql.*;
import java.time.LocalDate;
import java.util.Optional;

public class GoalRepository {
    private Connection conn() throws SQLException {
        return DatabaseManager.getInstance().getConnection();
    }

    public Optional<Goal> findLatestByUser(int userId) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT * FROM goals WHERE user_id=? ORDER BY id DESC LIMIT 1")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public Goal save(Goal g) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement(
                "INSERT INTO goals(user_id,start_weight_kg,target_weight_kg,height_cm,weekly_loss_rate_kg,calorie_target,start_date,projected_end_date,notes) VALUES(?,?,?,?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, g.getUserId());
            ps.setDouble(2, g.getStartWeightKg()); ps.setDouble(3, g.getTargetWeightKg());
            ps.setDouble(4, g.getHeightCm());       ps.setDouble(5, g.getWeeklyLossRateKg());
            ps.setInt(6, g.getCalorieTarget());
            ps.setString(7, g.getStartDate().toString());
            ps.setString(8, g.getProjectedEndDate().toString());
            ps.setString(9, g.getNotes());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) g.setId(keys.getInt(1));
            }
        }
        return g;
    }

    private Goal map(ResultSet rs) throws SQLException {
        Goal g = new Goal();
        g.setId(rs.getInt("id")); g.setUserId(rs.getInt("user_id"));
        g.setStartWeightKg(rs.getDouble("start_weight_kg"));
        g.setTargetWeightKg(rs.getDouble("target_weight_kg"));
        g.setHeightCm(rs.getDouble("height_cm"));
        g.setWeeklyLossRateKg(rs.getDouble("weekly_loss_rate_kg"));
        g.setCalorieTarget(rs.getInt("calorie_target"));
        g.setStartDate(LocalDate.parse(rs.getString("start_date")));
        g.setProjectedEndDate(LocalDate.parse(rs.getString("projected_end_date")));
        g.setNotes(rs.getString("notes"));
        return g;
    }
}
