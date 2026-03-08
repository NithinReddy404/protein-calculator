package com.hitacal.repository;

import com.hitacal.infra.DatabaseManager;
import com.hitacal.model.FoodItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FoodRepository {
    private Connection conn() throws SQLException {
        return DatabaseManager.getInstance().getConnection();
    }

    public List<FoodItem> search(String query, int limit) throws SQLException {
        List<FoodItem> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT * FROM food_items WHERE LOWER(description) LIKE LOWER(?) LIMIT ?")) {
            ps.setString(1, "%" + query + "%");
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public Optional<FoodItem> findByFdcId(int fdcId) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT * FROM food_items WHERE fdc_id=?")) {
            ps.setInt(1, fdcId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public int count() throws SQLException {
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM food_items")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private FoodItem map(ResultSet rs) throws SQLException {
        return new FoodItem(
            rs.getInt("fdc_id"), rs.getString("description"),
            rs.getString("category"), rs.getDouble("calories_100g"),
            rs.getDouble("protein_100g"), rs.getDouble("fat_100g"),
            rs.getDouble("carb_100g"), rs.getDouble("fiber_100g"),
            rs.getString("data_type")
        );
    }
}
