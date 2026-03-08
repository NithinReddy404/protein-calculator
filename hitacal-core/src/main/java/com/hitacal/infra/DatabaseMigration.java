package com.hitacal.infra;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class DatabaseMigration {
    private static final Logger log = LoggerFactory.getLogger(DatabaseMigration.class);

    public static void migrate() throws SQLException {
        Connection conn = DatabaseManager.getInstance().getConnection();
        ensureVersionTable(conn);
        int version = currentVersion(conn);
        log.info("DB schema version: {}", version);
        if (version < 1) applyV1(conn);
    }

    private static void ensureVersionTable(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS schema_version (
                    version INTEGER PRIMARY KEY,
                    applied_at TEXT NOT NULL
                )""");
        }
    }

    private static int currentVersion(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT MAX(version) FROM schema_version")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private static void applyV1(Connection conn) throws SQLException {
        log.info("Applying schema V1...");
        conn.setAutoCommit(false);
        try (Statement st = conn.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id            INTEGER PRIMARY KEY AUTOINCREMENT,
                    username      TEXT NOT NULL UNIQUE,
                    display_name  TEXT NOT NULL,
                    password_hash TEXT NOT NULL,
                    is_admin      INTEGER NOT NULL DEFAULT 0,
                    created_at    TEXT NOT NULL,
                    last_login_at TEXT
                )""");
            st.execute("""
                CREATE TABLE IF NOT EXISTS goals (
                    id                  INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id             INTEGER NOT NULL REFERENCES users(id),
                    start_weight_kg     REAL NOT NULL,
                    target_weight_kg    REAL NOT NULL,
                    height_cm           REAL,
                    weekly_loss_rate_kg REAL NOT NULL,
                    calorie_target      INTEGER NOT NULL,
                    start_date          TEXT NOT NULL,
                    projected_end_date  TEXT NOT NULL,
                    notes               TEXT
                )""");
            st.execute("""
                CREATE TABLE IF NOT EXISTS daily_logs (
                    id              INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id         INTEGER NOT NULL REFERENCES users(id),
                    log_date        TEXT NOT NULL,
                    total_calories  REAL NOT NULL DEFAULT 0,
                    total_protein_g REAL NOT NULL DEFAULT 0,
                    total_fat_g     REAL NOT NULL DEFAULT 0,
                    total_carb_g    REAL NOT NULL DEFAULT 0,
                    notes           TEXT,
                    created_at      TEXT NOT NULL,
                    updated_at      TEXT NOT NULL,
                    UNIQUE(user_id, log_date)
                )""");
            st.execute("CREATE INDEX IF NOT EXISTS idx_dl_user_date ON daily_logs(user_id, log_date)");
            st.execute("""
                CREATE TABLE IF NOT EXISTS log_entries (
                    id            INTEGER PRIMARY KEY AUTOINCREMENT,
                    log_id        INTEGER NOT NULL REFERENCES daily_logs(id) ON DELETE CASCADE,
                    fdc_id        INTEGER,
                    food_name     TEXT NOT NULL,
                    serving_grams REAL NOT NULL,
                    calories      REAL NOT NULL,
                    protein_g     REAL NOT NULL,
                    fat_g         REAL NOT NULL,
                    carb_g        REAL NOT NULL,
                    entry_time    TEXT
                )""");
            st.execute("""
                CREATE TABLE IF NOT EXISTS food_items (
                    fdc_id        INTEGER PRIMARY KEY,
                    description   TEXT NOT NULL,
                    category      TEXT,
                    calories_100g REAL,
                    protein_100g  REAL,
                    fat_100g      REAL,
                    carb_100g     REAL,
                    fiber_100g    REAL,
                    data_type     TEXT
                )""");
            st.execute("CREATE INDEX IF NOT EXISTS idx_food_desc ON food_items(LOWER(description))");
            st.execute("""
                CREATE TABLE IF NOT EXISTS motivations (
                    id               INTEGER PRIMARY KEY AUTOINCREMENT,
                    template         TEXT NOT NULL,
                    min_streak       INTEGER NOT NULL DEFAULT 0,
                    min_progress_pct REAL NOT NULL DEFAULT 0,
                    tag              TEXT NOT NULL
                )""");
            st.execute("""
                CREATE TABLE IF NOT EXISTS citations (
                    id      INTEGER PRIMARY KEY AUTOINCREMENT,
                    title   TEXT NOT NULL,
                    authors TEXT NOT NULL,
                    journal TEXT NOT NULL,
                    year    INTEGER NOT NULL,
                    doi     TEXT,
                    url     TEXT
                )""");
            // record version
            st.execute("INSERT INTO schema_version(version,applied_at) VALUES(1,datetime('now'))");
            conn.commit();
            log.info("Schema V1 applied.");
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }
}
