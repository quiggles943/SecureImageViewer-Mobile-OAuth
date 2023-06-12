package com.quigglesproductions.secureimageviewer.appauth;

import androidx.annotation.NonNull;

import com.quigglesproductions.secureimageviewer.authentication.IAuthenticationLayer;

import net.openid.appauth.AuthState;

public interface IAuthManager extends IAuthenticationLayer {
    public void performActionWithFreshTokens(@NonNull AuthState.AuthStateAction action);
}
