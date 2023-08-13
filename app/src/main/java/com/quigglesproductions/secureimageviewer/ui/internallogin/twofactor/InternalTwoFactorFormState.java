package com.quigglesproductions.secureimageviewer.ui.internallogin.twofactor;

import androidx.annotation.Nullable;

/**
 * Data validation state of the login form.
 */
class InternalTwoFactorFormState {
    @Nullable
    private Integer twoFactorError;
    private boolean isDataValid;

    InternalTwoFactorFormState(@Nullable Integer twoFactorError) {
        this.twoFactorError = twoFactorError;
        this.isDataValid = false;
    }

    InternalTwoFactorFormState(boolean isDataValid) {
        this.twoFactorError = null;
        this.isDataValid = isDataValid;
    }

    @Nullable
    Integer getTwoFactorError() {
        return twoFactorError;
    }

    boolean isDataValid() {
        return isDataValid;
    }
}