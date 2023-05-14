package com.quigglesproductions.secureimageviewer.authentication;

import com.google.gson.Gson;
import com.quigglesproductions.secureimageviewer.authentication.pogo.TokenResponse;
import com.quigglesproductions.secureimageviewer.gson.ViewerGson;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class AuthenticationState {
    private final String accessToken;
    private final String refreshToken;
    private final String[] scopes;
    private Long accessTokenExpiry;
    private Long refreshTokenExpiry;
    private LocalDateTime accessTokenExpiryDate;
    private LocalDateTime refreshTokenExpiryDate;

    public AuthenticationState(TokenResponse tokenResponse){
        accessToken = tokenResponse.access_token;
        refreshToken = tokenResponse.refresh_token;
        scopes = tokenResponse.scope.split(",");
        if(tokenResponse.expires_in != null) {
            accessTokenExpiry = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(Long.decode(tokenResponse.expires_in));
            accessTokenExpiryDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(accessTokenExpiry), ZoneId.systemDefault());
        }
        if(tokenResponse.refresh_expires_in != null) {
            refreshTokenExpiry = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(Long.decode(tokenResponse.refresh_expires_in));
            refreshTokenExpiryDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(refreshTokenExpiry), ZoneId.systemDefault());
        }


    }

    public static AuthenticationState fromJson(String json) {
        Gson gson = ViewerGson.getGson();
        return gson.fromJson(json,AuthenticationState.class);
    }

    public LocalDateTime getAccessTokenExpiry() {
        return accessTokenExpiryDate;
    }

    public LocalDateTime getRefreshTokenExpiry() {
        return refreshTokenExpiryDate;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String[] getScopes() {
        return scopes;
    }

    public String toJson(){
        Gson gson = ViewerGson.getGson();
        return gson.toJson(this);
    }
}
