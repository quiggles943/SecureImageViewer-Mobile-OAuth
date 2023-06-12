package com.quigglesproductions.secureimageviewer.authentication;

public interface IAuthenticationLayer {

    void retrieveValidAccessToken(AuthenticationManager.TokenRetrievalCallback callback);
}
