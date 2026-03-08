package com.hitacal.ui;

import com.hitacal.model.User;
import javafx.stage.Stage;

/** Global app session — holds current user and primary stage reference. */
public class AppSession {
    private static AppSession instance = new AppSession();
    private User  currentUser;
    private Stage primaryStage;

    private AppSession() {}
    public static AppSession get() { return instance; }

    public User  getCurrentUser()  { return currentUser; }
    public void  setCurrentUser(User u) { this.currentUser = u; }
    public Stage getPrimaryStage() { return primaryStage; }
    public void  setPrimaryStage(Stage s) { this.primaryStage = s; }
}
