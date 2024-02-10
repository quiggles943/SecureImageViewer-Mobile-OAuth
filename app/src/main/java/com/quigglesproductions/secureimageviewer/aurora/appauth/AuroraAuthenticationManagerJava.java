package com.quigglesproductions.secureimageviewer.aurora.appauth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.quigglesproductions.secureimageviewer.App;
import com.quigglesproductions.secureimageviewer.appauth.AuthManager;
import com.quigglesproductions.secureimageviewer.ui.SecureActivity;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.TokenRequest;
import net.openid.appauth.TokenResponse;

import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuroraAuthenticationManagerJava {
    public static final String AUTHMANAGER_PREF_NAME = "com.secureimageviewer.preference.aurora.AuroraAuthenticationManagerJava";
    public static final String TOKEN_PREF = "com.secureimageviewer.preference.aurora.AuroraAuthenticationManagerJava.token";
    SharedPreferences tokenPreferences;
    Context rootContext;
    AuroraAuthState authState;
    private AuthorizationServiceConfiguration serviceConfig;
    private AuthorizationService authService;
    private AuthState.AuthStateAction delayedAction;
    private boolean loginRequestInProgress;

    public AuroraAuthenticationManagerJava(Context context){
        rootContext = context;
        configureAuthenticationManager();
    }
    private void configureAuthenticationManager(){
        tokenPreferences = getTokenPreferences();
        loadAuthState();
        authService = new AuthorizationService(rootContext);
        if(authState != null)
            serviceConfig = authState.getAuthorizationServiceConfiguration();
    }
    private SharedPreferences getTokenPreferences(){
        if(tokenPreferences == null)
        {
            if(rootContext != null) {
                tokenPreferences = rootContext.getSharedPreferences(
                        AUTHMANAGER_PREF_NAME, Context.MODE_PRIVATE);
            }
        }
        return tokenPreferences;
    }

    private AuthState loadAuthState(){
        String jsonString = getTokenPreferences().getString(TOKEN_PREF,null);
        if(jsonString == null)
            return null;
        try {
            AuroraAuthState loadedAuthState = AuroraAuthState.jsonDeserialize(jsonString);
            authState = loadedAuthState;
            return authState;
        } catch (JSONException e) {
            return null;
        }
    }

    private void saveAuthState(AuroraAuthState newAuthState){
        SharedPreferences.Editor editor = getTokenPreferences().edit();
        String jsonString = newAuthState.jsonSerializeString();
        editor.putString(TOKEN_PREF,jsonString);
        editor.apply();
        authState = (AuroraAuthState) newAuthState;
    }

    public void setAuthState(AuroraAuthState authState){
        saveAuthState(authState);
    }

    private void setDelayedAction(AuthState.AuthStateAction action){
        delayedAction = action;
    }

    public void performActionWithFreshTokens(@NonNull AuthState.AuthStateAction action){
        Map<String, String> additionalParams = new HashMap<String,String>();
        additionalParams.put("X-Request-Id", UUID.randomUUID().toString());
        authState.performActionWithFreshTokens(authService,additionalParams, new AuthState.AuthStateAction() {
            @Override
            public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException ex) {
                if (ex != null) {
                    switch (ex.type){
                        case AuthorizationException.TYPE_OAUTH_AUTHORIZATION_ERROR:
                        case AuthorizationException.TYPE_OAUTH_TOKEN_ERROR:
                            if(!loginRequestInProgress) {
                                setDelayedAction(action);
                                requestLogin(rootContext);
                            }
                            break;
                    }


                } else {
                    authState.performActionWithFreshTokens(authService, action);
                }
                SharedPreferences.Editor editor = getTokenPreferences().edit();
                editor.putString(TOKEN_PREF, authState.jsonSerializeString());
                editor.apply();
            }
        });
    }

    public void requestLogin(Context context){
        if(context instanceof SecureActivity){
            SecureActivity activity = (SecureActivity) context;
            Intent intent = AuthManager.getInstance().getAuthorizationRequestIntent();

            if(intent != null) {
                loginRequestInProgress = true;
                activity.startActivityForResult(intent, AuthManager.AUTH_RESULT_CODE);
            }
            else
                Toast.makeText(context,"Unable to connect to server",Toast.LENGTH_SHORT).show();
        }
        else if(context instanceof App){
            Intent intent = AuthManager.getInstance().getAuthorizationRequestIntent();

            if(intent != null) {

            }
            else
                Toast.makeText(context,"Unable to connect to server",Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(context, "This didnt work", Toast.LENGTH_SHORT).show();
        }
    }

    public void updateAuthState(Context context, AuthorizationResponse resp, AuthorizationException ex) {
        authState.update(resp,ex);
        loginRequestInProgress = false;
        saveAuthState(authState);
    }

    public void getToken(@NonNull Context context,AuthorizationResponse resp, AuthorizationException ex,AuthorizationService.TokenResponseCallback callback) {
        if(resp.accessToken == null) {
            if (resp != null) {
                TokenRequest tokenRequest = resp.createTokenExchangeRequest();
                AuthManager.getInstance().performTokenRequest(
                        tokenRequest,
                        new AuthorizationService.TokenResponseCallback() {
                            @Override
                            public void onTokenRequestCompleted(@Nullable TokenResponse response, @Nullable AuthorizationException ex) {
                                long refreshExpirationSeconds = Long.decode(response.additionalParameters.get("refresh_expires_in"));
                                authState.setRefreshTokenExpirationTime(refreshExpirationSeconds);
                                authState.update(response,ex);
                                saveAuthState(authState);
                                callback.onTokenRequestCompleted(response,ex);
                            }
                        }
                );
            }
        }
        else
            updateAuthState(context, resp, ex);
    }

    class Builder{
        private AuthorizationServiceConfiguration serviceConfiguration;

        public void withServiceConfiguration(AuthorizationServiceConfiguration serviceConfiguration) {
            this.serviceConfiguration = serviceConfiguration;
        }
    }
}
