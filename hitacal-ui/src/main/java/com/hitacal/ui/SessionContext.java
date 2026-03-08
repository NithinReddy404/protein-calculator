package com.hitacal.ui;

import com.hitacal.model.User;

public class SessionContext {
    private static User currentUser;

    public static void setCurrentUser(User u) { currentUser = u; }
    public static User getCurrentUser()       { return currentUser; }
    public static void clear()                { currentUser = null; }
    public static boolean isAdmin()           { return currentUser != null && currentUser.isAdmin(); }

    private SessionContext() {}
}
