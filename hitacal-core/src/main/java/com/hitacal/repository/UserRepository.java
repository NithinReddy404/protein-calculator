package com.hitacal.repository;

import com.hitacal.infra.DatabaseManager;
import com.hitacal.model.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository {
    private Connection conn() throws SQLException {
        return DatabaseManager.getInstance().getConnection();
    }

    public Optional<User> findByUsername(String username) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT * FROM users WHERE username=?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public Optional<User> findById(int id) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM users WHERE id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public List<User> findAll() throws SQLException {
        List<User> list = new ArrayList<>();
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM users ORDER BY display_name")) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public User insert(User u) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement(
                "INSERT INTO users(username,display_name,password_hash,is_admin,created_at) VALUES(?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getDisplayName());
            ps.setString(3, u.getPasswordHash());
            ps.setInt(4, u.isAdmin() ? 1 : 0);
            ps.setString(5, LocalDateTime.now().toString());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) u.setId(keys.getInt(1));
            }
        }
        return u;
    }

    public void updateLastLogin(int userId) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement(
                "UPDATE users SET last_login_at=? WHERE id=?")) {
            ps.setString(1, LocalDateTime.now().toString());
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    public void updatePasswordHash(int userId, String newHash) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement(
                "UPDATE users SET password_hash=? WHERE id=?")) {
            ps.setString(1, newHash);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    private User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setDisplayName(rs.getString("display_name"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setAdmin(rs.getInt("is_admin") == 1);
        String ca = rs.getString("created_at");
        if (ca != null) u.setCreatedAt(LocalDateTime.parse(ca.length() > 19 ? ca.substring(0,19) : ca));
        String la = rs.getString("last_login_at");
        if (la != null) u.setLastLoginAt(LocalDateTime.parse(la.length() > 19 ? la.substring(0,19) : la));
        return u;
    }
}
