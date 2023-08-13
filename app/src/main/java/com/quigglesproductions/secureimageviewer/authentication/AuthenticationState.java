package com.quigglesproductions.secureimageviewer.authentication;

import com.google.gson.Gson;
import com.quigglesproductions.secureimageviewer.authentication.pogo.TokenResponse;
import com.quigglesproductions.secureimageviewer.authentication.pogo.internal.InternalAuthToken;
import com.quigglesproductions.secureimageviewer.gson.ViewerGson;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class AuthenticationState {
    private String accessToken;
    private String refreshToken;
    private String[] scopes;
    private Long accessTokenExpiry;
    private Long refreshTokenExpiry;
    private LocalDateTime accessTokenExpiryDate;
    private LocalDateTime refreshTokenExpiryDate;
    private transient Clock clock = Clock.systemDefaultZone();

    AuthenticationState(){

    }

    private void setAccessToken(String accessToken){
        this.accessToken = accessToken;
    }
    private void setRefreshToken(String refreshToken){
        this.refreshToken = refreshToken;
    }

    void setAccessTokenExpiry(String expires_in){
        if(expires_in != null) {
            accessTokenExpiry = clock.millis() + TimeUnit.SECONDS.toMillis(Long.decode(expires_in));
            accessTokenExpiryDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(accessTokenExpiry), ZoneId.systemDefault());
        }
    }
    void setRefreshTokenExpiry(String refresh_expires_in){
        if(refresh_expires_in != null) {
            refreshTokenExpiry = clock.millis() + TimeUnit.SECONDS.toMillis(Long.decode(refresh_expires_in));
            refreshTokenExpiryDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(refreshTokenExpiry), ZoneId.systemDefault());
        }
    }

    void setScopes(String scope){
        if(scope != null && !scope.isEmpty())
            scopes = scope.split(",");
    }

    void setClock(Clock clock){
        this.clock = clock;
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

    public static class Builder{
        private TokenResponse tokenResponse;
        private Clock clock;
        public AuthenticationState.Builder withClock(Clock clock){
            this.clock = clock;
            return this;
        }
        public AuthenticationState.Builder fromTokenResponse(TokenResponse tokenResponse){
            this.tokenResponse = tokenResponse;
            return this;
        }
        public AuthenticationState.Builder fromInternalAuthToken(InternalAuthToken token){
            TokenResponse response = new TokenResponse();
            response.access_token = token.accessToken;
            response.expires_in = token.expiresIn;
            response.refresh_token = token.refreshToken;
            response.refresh_expires_in = token.refreshExpiresIn;
            response.scope = token.scope;
            response.token_type = token.tokenType;
            this.tokenResponse = response;
            return this;
        }
        public AuthenticationState build(){
            AuthenticationState authenticationState = new AuthenticationState();
            if(clock != null)
                authenticationState.setClock(clock);

            authenticationState.setAccessToken(tokenResponse.access_token);
            authenticationState.setRefreshToken(tokenResponse.refresh_token);
            authenticationState.setAccessTokenExpiry(tokenResponse.expires_in);
            authenticationState.setRefreshTokenExpiry(tokenResponse.refresh_expires_in);
            authenticationState.setScopes(tokenResponse.scope);
            return authenticationState;
        }
    }
}
