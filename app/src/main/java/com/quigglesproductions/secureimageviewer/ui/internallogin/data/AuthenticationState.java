package com.quigglesproductions.secureimageviewer.ui.internallogin.data;

public enum AuthenticationState {
    REQUIRES_2FA,
    CREDENTIALS_INVALID,
    ACCEPTED,
    COMPLETE,
    EXPIRED,
    RENEW_2FA,
    ERROR
}
