package com.quigglesproductions.secureimageviewer.ui.internallogin;

/**
 * Class exposing authenticated user details to the UI.
 */
public class InternalLoggedInUserView {
    private String displayName;
    //... other data fields that may be accessible to the UI

    public InternalLoggedInUserView(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}