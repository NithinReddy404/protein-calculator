package com.hitacal.infra;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class DataSeeder {
    private static final Logger LOG = LoggerFactory.getLogger(DataSeeder.class);

    public static void seed(Connection conn) throws SQLException {
        seedMotivations(conn);
        seedCitations(conn);
        seedAdminUser(conn);
    }

    private static void seedMotivations(Connection conn) throws SQLException {
        try (var st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM motivations")) {
            if (rs.next() && rs.getInt(1) > 0) return;
        }
        LOG.info("Seeding motivations...");
        String[][] msgs = {
            {"Every bite logged is a step closer, {name}. Keep it up!", "0", "0", "DAILY"},
            {"You showed up today, {name}. That is already a win.", "0", "0", "DAILY"},
            {"Tracking takes courage, {name}. You have got this!", "0", "0", "DAILY"},
            {"Small steps, big shell. One log at a time, {name}!", "0", "0", "DAILY"},
            {"The turtle is cheering for you, {name}. Stay consistent!", "0", "0", "DAILY"},
            {"Almost there, {name}! Your goal can smell you coming.", "0", "75", "GOAL"},
            {"You are in the home stretch, {name}. The turtle is rooting for you!", "0", "75", "GOAL"},
            {"Only a little more, {name}. The turtle believes in you!", "0", "90", "GOAL"},
            {"{name}, 7 days in a row! Your shell is getting shinier!", "7", "0", "STREAK"},
            {"14-day streak, {name}! You are unstoppable. Even the turtle is impressed.", "14", "0", "STREAK"},
            {"21 days, {name}! A habit is born. The turtle salutes you!", "21", "0", "STREAK"},
            {"30 days, {name}! You have officially out-turtled the turtle. You legend!", "30", "0", "STREAK"},
            {"You hit your goal today, {name}! The turtle is doing a happy dance!", "0", "100", "CHEERING"},
            {"Goal crushed, {name}! Every day like this gets you there faster!", "0", "100", "CHEERING"},
            {"Perfect day, {name}! Shell polished, spirits high. You are amazing!", "0", "100", "CHEERING"},
        };
        try (var ps = conn.prepareStatement(
                "INSERT INTO motivations(template, min_streak, min_progress_pct, tag) VALUES(?,?,?,?)")) {
            for (String[] m : msgs) {
                ps.setString(1, m[0]);
                ps.setInt(2, Integer.parseInt(m[1]));
                ps.setDouble(3, Double.parseDouble(m[2]));
                ps.setString(4, m[3]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void seedCitations(Connection conn) throws SQLException {
        try (var st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM citations")) {
            if (rs.next() && rs.getInt(1) > 0) return;
        }
        String[][] cites = {
            {"Comparison of weight-loss diets with different compositions of fat, protein, and carbohydrates",
             "Sacks FM, Bray GA, Carey VJ, et al.", "New England Journal of Medicine", "2009",
             "10.1056/NEJMoa0804748", "https://doi.org/10.1056/NEJMoa0804748"},
            {"Quantification of the effect of energy imbalance on bodyweight",
             "Hall KD, Sacks G, Chandramohan D, et al.", "The Lancet", "2011",
             "10.1016/S0140-6736(11)60812-X", "https://doi.org/10.1016/S0140-6736(11)60812-X"},
        };
        try (var ps = conn.prepareStatement(
                "INSERT INTO citations(title,authors,journal,year,doi,url) VALUES(?,?,?,?,?,?)")) {
            for (String[] c : cites) {
                ps.setString(1, c[0]); ps.setString(2, c[1]); ps.setString(3, c[2]);
                ps.setInt(4, Integer.parseInt(c[3])); ps.setString(5, c[4]); ps.setString(6, c[5]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void seedAdminUser(Connection conn) throws SQLException {
        try (var st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM users WHERE is_admin=1")) {
            if (rs.next() && rs.getInt(1) > 0) return;
        }
        LOG.info("Creating default admin user...");
        String hash = BcryptUtil.hash("changeme");
        try (var ps = conn.prepareStatement(
                "INSERT INTO users(username,display_name,password_hash,is_admin,created_at) VALUES(?,?,?,1,?)")) {
            ps.setString(1, "admin");
            ps.setString(2, "Admin");
            ps.setString(3, hash);
            ps.setString(4, LocalDateTime.now().toString());
            ps.executeUpdate();
        }
        LOG.info("Admin user created: username=admin password=changeme");
    }
}
