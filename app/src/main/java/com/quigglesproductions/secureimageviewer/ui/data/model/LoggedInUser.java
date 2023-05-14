package com.quigglesproductions.secureimageviewer.ui.data.model;

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
public class LoggedInUser {

    private String userId;
    private String displayName;
    private String emailAddress;

    public LoggedInUser(String userId,String emailAddress, String displayName) {
        this.userId = userId;
        this.emailAddress = emailAddress;
        this.displayName = displayName;
    }

    public String getUserId() {
        return userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }
}