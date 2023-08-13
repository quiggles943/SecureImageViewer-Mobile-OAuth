package com.quigglesproductions.secureimageviewer.ui.internallogin.twofactor;

import androidx.annotation.Nullable;

import com.quigglesproductions.secureimageviewer.authentication.pogo.internal.InternalAuthResponse;
import com.quigglesproductions.secureimageviewer.ui.internallogin.InternalLoggedInUserView;

/**
 * Authentication result : success (user details) or error message.
 */
public class InternalTwoFactorResult {
    @Nullable
    private InternalLoggedInUserView success;
    @Nullable
    private InternalAuthResponse inProgress;
    @Nullable
    private Integer error;

    public InternalTwoFactorResult(@Nullable Integer error) {
        this.error = error;
    }

    public InternalTwoFactorResult(@Nullable InternalLoggedInUserView success) {
        this.success = success;
    }
    public InternalTwoFactorResult(@Nullable InternalAuthResponse inProgress) {
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