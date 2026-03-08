package com.hitacal.service;

import com.hitacal.infra.BcryptUtil;
import com.hitacal.model.User;
import com.hitacal.repository.UserRepository;

import java.sql.SQLException;

public class AuthService {
    private final UserRepository userRepo = new UserRepository();
    private User currentUser;

    public User login(String username, String password) throws SQLException {
        User user = userRepo.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Invalid username or password."));
        if (!BcryptUtil.verify(password, user.getPasswordHash()))
            throw new IllegalArgumentException("Invalid username or password.");
        userRepo.updateLastLogin(user.getId());
        this.currentUser = user;
        return user;
    }

    public User register(String username, String displayName, String password) throws SQLException {
        if (username == null || !username.matches("[a-z0-9_]{3,32}"))
            throw new IllegalArgumentException("Username must be 3–32 lowercase alphanumeric/underscore chars.");
        if (displayName == null || displayName.isBlank())
            throw new IllegalArgumentException("Display name is required.");
        if (password == null || password.length() < 6)
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        if (userRepo.findByUsername(username).isPresent())
            throw new IllegalArgumentException("Username already taken.");
        User u = new User();
        u.setUsername(username);
        u.setDisplayName(displayName.trim());
        u.setPasswordHash(BcryptUtil.hash(password));
        u.setAdmin(false);
        return userRepo.insert(u);
    }

    public void changePassword(User user, String newPassword) throws SQLException {
        if (newPassword == null || newPassword.length() < 6)
            throw new IllegalArgumentException("New password must be at least 6 characters.");
        userRepo.updatePasswordHash(user.getId(), BcryptUtil.hash(newPassword));
    }

    public User getCurrentUser() { return currentUser; }
    public void logout() { currentUser = null; }
}
