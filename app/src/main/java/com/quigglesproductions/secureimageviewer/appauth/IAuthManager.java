package com.quigglesproductions.secureimageviewer.appauth;

import androidx.annotation.NonNull;

import net.openid.appauth.AuthState;

public interface IAuthManager {
    public void performActionWithFreshTokens(@NonNull AuthState.AuthStateAction action);
}
