package com.hitacal.repository;

import com.hitacal.infra.DatabaseManager;
import com.hitacal.model.DailyLog;
import com.hitacal.model.LogEntry;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DailyLogRepository {
    private Connection conn() throws SQLException {
        return DatabaseManager.getInstance().getConnection();
    }

    public Optional<DailyLog> findByUserAndDate(int userId, LocalDate date) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT * FROM daily_logs WHERE user_id=? AND log_date=?")) {
            ps.setInt(1, userId);
            ps.setString(2, date.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                DailyLog dl = map(rs);
                dl.setEntries(findEntries(dl.getId()));
                return Optional.of(dl);
            }
        }
    }

    public DailyLog getOrCreate(int userId, LocalDate date) throws SQLException {
        Optional<DailyLog> existing = findByUserAndDate(userId, date);
        if (existing.isPresent()) return existing.get();
        DailyLog dl = new DailyLog();
        dl.setUserId(userId);
        dl.setLogDate(date);
        dl.setCreatedAt(LocalDateTime.now());
        dl.setUpdatedAt(LocalDateTime.now());
        try (PreparedStatement ps = conn().prepareStatement(
                "INSERT INTO daily_logs(user_id,log_date,total_calories,total_protein_g,total_fat_g,total_carb_g,created_at,updated_at) VALUES(?,?,0,0,0,0,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setString(2, date.toString());
            ps.setString(3, dl.getCreatedAt().toString());
            ps.setString(4, dl.getUpdatedAt().toString());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) dl.setId(keys.getInt(1));
            }
        }
        return dl;
    }

    public void addEntry(LogEntry e) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement(
                "INSERT INTO log_entries(log_id,fdc_id,food_name,serving_grams,calories,protein_g,fat_g,carb_g,entry_time) VALUES(?,?,?,?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, e.getLogId());
            ps.setInt(2, e.getFdcId());
            ps.setString(3, e.getFoodName());
            ps.setDouble(4, e.getServingGrams());
            ps.setDouble(5, e.getCalories());
            ps.setDouble(6, e.getProteinG());
            ps.setDouble(7, e.getFatG());
            ps.setDouble(8, e.getCarbG());
            ps.setString(9, e.getEntryTime());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) e.setId(keys.getInt(1));
            }
        }
        recalcTotals(e.getLogId());
    }

    public void deleteEntry(int entryId, int logId) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement(
                "DELETE FROM log_entries WHERE id=?")) {
            ps.setInt(1, entryId);
            ps.executeUpdate();
        }
        recalcTotals(logId);
    }

    public void recalcTotals(int logId) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("""
                UPDATE daily_logs SET
                    total_calories  = (SELECT COALESCE(SUM(calories),0)  FROM log_entries WHERE log_id=?),
                    total_protein_g = (SELECT COALESCE(SUM(protein_g),0) FROM log_entries WHERE log_id=?),
                    total_fat_g     = (SELECT COALESCE(SUM(fat_g),0)     FROM log_entries WHERE log_id=?),
                    total_carb_g    = (SELECT COALESCE(SUM(carb_g),0)    FROM log_entries WHERE log_id=?),
                    updated_at = ?
                WHERE id=?""")) {
            for (int i = 1; i <= 4; i++) ps.setInt(i, logId);
            ps.setString(5, LocalDateTime.now().toString());
            ps.setInt(6, logId);
            ps.executeUpdate();
        }
    }

    public List<DailyLog> findAll(int userId) throws SQLException {
        List<DailyLog> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT * FROM daily_logs WHERE user_id=? ORDER BY log_date")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public int getStreak(int userId) throws SQLException {
        // Count consecutive days logged ending today/yesterday
        List<LocalDate> dates = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT log_date FROM daily_logs WHERE user_id=? ORDER BY log_date DESC LIMIT 120")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) dates.add(LocalDate.parse(rs.getString("log_date")));
            }
        }
        if (dates.isEmpty()) return 0;
        LocalDate expected = LocalDate.now();
        if (!dates.get(0).equals(expected)) expected = expected.minusDays(1);
        int streak = 0;
        for (LocalDate d : dates) {
            if (d.equals(expected)) { streak++; expected = expected.minusDays(1); }
            else break;
        }
        return streak;
    }

    private List<LogEntry> findEntries(int logId) throws SQLException {
        List<LogEntry> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT * FROM log_entries WHERE log_id=?")) {
            ps.setInt(1, logId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LogEntry e = new LogEntry();
                    e.setId(rs.getInt("id")); e.setLogId(rs.getInt("log_id"));
                    e.setFdcId(rs.getInt("fdc_id")); e.setFoodName(rs.getString("food_name"));
                    e.setServingGrams(rs.getDouble("serving_grams"));
                    e.setCalories(rs.getDouble("calories")); e.setProteinG(rs.getDouble("protein_g"));
                    e.setFatG(rs.getDouble("fat_g")); e.setCarbG(rs.getDouble("carb_g"));
                    e.setEntryTime(rs.getString("entry_time"));
                    list.add(e);
                }
            }
        }
        return list;
    }

    private DailyLog map(ResultSet rs) throws SQLException {
        DailyLog dl = new DailyLog();
        dl.setId(rs.getInt("id")); dl.setUserId(rs.getInt("user_id"));
        dl.setLogDate(LocalDate.parse(rs.getString("log_date")));
        dl.setTotalCalories(rs.getDouble("total_calories"));
        dl.setTotalProteinG(rs.getDouble("total_protein_g"));
        dl.setTotalFatG(rs.getDouble("total_fat_g"));
        dl.setTotalCarbG(rs.getDouble("total_carb_g"));
        dl.setNotes(rs.getString("notes"));
        return dl;
    }
}
