package com.quigglesproductions.secureimageviewer.authentication;

import android.content.Context;
import android.content.SharedPreferences;

import com.quigglesproductions.secureimageviewer.appauth.AuthManager;
import com.quigglesproductions.secureimageviewer.authentication.pogo.TokenRefreshRequest;
import com.quigglesproductions.secureimageviewer.authentication.pogo.TokenResponse;
import com.quigglesproductions.secureimageviewer.retrofit.RetrofitException;

import java.time.LocalDateTime;

import javax.inject.Inject;

import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Module
@InstallIn(SingletonComponent.class)
public class TokenManager {
    private static final String tokenPreferences = "com.secureimageviewer.preference.authentication.token";
    private static final String authenticationState = "com.secureimageviewer.preference.authentication.token.authstate";
    Context context;
    public TokenManager(){

    }
    @Inject
    public TokenManager(@ApplicationContext Context context){
        this.context = context;
    }

    void setContext(Context context){
        this.context = context;
    }

    SharedPreferences getTokenPref(){
        SharedPreferences tokenPref = context.getSharedPreferences(tokenPreferences,Context.MODE_PRIVATE);
        return tokenPref;
    }

    public AuthenticationState getAuthenticationState(){
        String json = getTokenPref().getString(authenticationState,null);
        if(json == null)
            return null;
        return AuthenticationState.fromJson(json);
    }

    public void setAuthenticaionState(AuthenticationState authenticaionState){
        SharedPreferences.Editor editor = getTokenPref().edit();
        editor.putString(authenticationState,authenticaionState.toJson());
        editor.commit();
    }

    public String getAccessToken() {
        AuthenticationState authenticationState = getAuthenticationState();
        if(authenticationState == null)
            return null;
        return authenticationState.getAccessToken();
    }

    public AuthenticationState refreshToken(TokenResponse tokenResponse) {
        AuthenticationState authState = new AuthenticationState(tokenResponse);
        setAuthenticaionState(authState);
        return authState;
    }

    public boolean isTokenValid() {
        AuthenticationState authState = getAuthenticationState();
        if(authState == null)
            return false;
        if(authState.getAccessTokenExpiry().isBefore(LocalDateTime.now()))
            return false;
        else
            return true;
    }

    public String getRefreshToken() {
        AuthenticationState authenticationState = getAuthenticationState();
        if(authenticationState == null)
            return null;
        return authenticationState.getRefreshToken();
    }

    public boolean isRefreshTokenValid() {
        AuthenticationState authState = getAuthenticationState();
        if(authState == null)
            return false;
        if(authState.getRefreshTokenExpiry().isBefore(LocalDateTime.now()))
            return false;
        else
            return true;
    }




}
