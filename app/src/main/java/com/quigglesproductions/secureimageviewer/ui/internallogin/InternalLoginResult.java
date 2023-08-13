package com.quigglesproductions.secureimageviewer.ui.internallogin;

import androidx.annotation.Nullable;

import com.quigglesproductions.secureimageviewer.authentication.pogo.internal.InternalAuthResponse;
import com.quigglesproductions.secureimageviewer.ui.internallogin.InternalLoggedInUserView;

/**
 * Authentication result : success (user details) or error message.
 */
public class InternalLoginResult {
    @Nullable
    private InternalLoggedInUserView success;
    @Nullable
    private InternalAuthResponse inProgress;
    @Nullable
    private Integer error;

    public InternalLoginResult(@Nullable Integer error) {
        this.error = error;
    }

    public InternalLoginResult(@Nullable InternalLoggedInUserView success) {
        this.success = success;
    }
    public InternalLoginResult(@Nullable InternalAuthResponse inProgress) {
        this.inProgress = inProgress;
    }

    @Nullable
    public InternalLoggedInUserView getSuccess() {
        return success;
    }

    @Nullable
    public Integer getError() {
        return error;
    }
    @Nullable
    public InternalAuthResponse getInProgress(){
        return inProgress;
    }
}