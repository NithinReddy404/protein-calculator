package com.hitacal.infra;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.*;

public class DatabaseManager {
    private static final Logger log = LoggerFactory.getLogger(DatabaseManager.class);
    private static DatabaseManager instance;
    private Connection connection;

    private static final String DB_DIR  = System.getProperty("user.home") + File.separator + ".hitacal";
    private static final String DB_PATH = DB_DIR + File.separator + "hitacal.db";

    private DatabaseManager() {}

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) instance = new DatabaseManager();
        return instance;
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            new File(DB_DIR).mkdirs();
            connection = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
            try (Statement st = connection.createStatement()) {
                st.execute("PRAGMA journal_mode=WAL");
                st.execute("PRAGMA foreign_keys=ON");
            }
            log.info("SQLite connected: {}", DB_PATH);
        }
        return connection;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (SQLException e) {
            log.warn("Error closing DB", e);
        }
    }

    public static String getDbPath() { return DB_PATH; }
}
