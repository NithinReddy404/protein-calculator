package com.hitacal.service;

import com.hitacal.infra.DatabaseManager;
import com.hitacal.model.Motivation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class MotivationService {
    private int dailyRoundRobinIndex = 0;

    public String getMessage(String displayName, int streak, double progressPct) {
        try {
            String tag;
            if (progressPct >= 100)     tag = "CHEERING";
            else if (isStreakMilestone(streak)) tag = "STREAK";
            else if (progressPct >= 75) tag = "GOAL";
            else                        tag = "DAILY";

            List<Motivation> pool = fetchByTag(tag, streak, progressPct);
            if (pool.isEmpty()) pool = fetchByTag("DAILY", 0, 0);
            if (pool.isEmpty()) return "Keep going, " + displayName + "! 🐢";

            Motivation m;
            if ("DAILY".equals(tag)) {
                m = pool.get(dailyRoundRobinIndex % pool.size());
                dailyRoundRobinIndex++;
            } else {
                m = pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
            }
            return m.resolve(displayName);
        } catch (Exception e) {
            return "You're doing great, " + displayName + "! 🐢";
        }
    }

    private boolean isStreakMilestone(int streak) {
        return streak > 0 && (streak == 7 || streak == 14 || streak == 21 ||
               streak == 30 || streak == 60 || streak == 90);
    }

    private List<Motivation> fetchByTag(String tag, int streak, double progressPct) throws SQLException {
        List<Motivation> list = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getInstance().getConnection().prepareStatement(
                "SELECT * FROM motivations WHERE tag=? AND min_streak<=? AND min_progress_pct<=?")) {
            ps.setString(1, tag); ps.setInt(2, streak); ps.setDouble(3, progressPct);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(new Motivation(rs.getInt("id"), rs.getString("template"),
                        rs.getInt("min_streak"), rs.getDouble("min_progress_pct"), rs.getString("tag")));
            }
        }
        return list;
    }
}
