package com.hitacal.model;

import java.time.LocalDateTime;

public class User {
    private int id;
    private String username;
    private String displayName;
    private String passwordHash;
    private boolean admin;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

    public User() {}

    public User(int id, String username, String displayName,
                String passwordHash, boolean admin,
                LocalDateTime createdAt, LocalDateTime lastLoginAt) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.passwordHash = passwordHash;
        this.admin = admin;
        this.createdAt = createdAt;
        this.lastLoginAt = lastLoginAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public boolean isAdmin() { return admin; }
    public void setAdmin(boolean admin) { this.admin = admin; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }

    @Override public String toString() { return displayName + " (@" + username + ")"; }
}
