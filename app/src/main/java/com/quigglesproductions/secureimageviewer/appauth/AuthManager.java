package com.quigglesproductions.secureimageviewer.appauth;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.quigglesproductions.secureimageviewer.Downloaders.UserInfoDownloader;
import com.quigglesproductions.secureimageviewer.api.ApiRequestType;
import com.quigglesproductions.secureimageviewer.login.BaseLogin;
import com.quigglesproductions.secureimageviewer.login.LoginActivity;
import com.quigglesproductions.secureimageviewer.managers.FolderManager;
import com.quigglesproductions.secureimageviewer.models.oauth.UserInfo;
import com.quigglesproductions.secureimageviewer.registration.RegistrationId;
import com.quigglesproductions.secureimageviewer.ui.SecureActivity;
import com.quigglesproductions.secureimageviewer.ui.splash.SplashScreenActivity;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.ResponseTypeValues;
import net.openid.appauth.TokenRequest;
import net.openid.appauth.TokenResponse;

import org.json.JSONException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class AuthManager{
    private static final AuthManager instance = new AuthManager();
    private Context rootContext;
    public String MY_CLIENT_ID = "10887ebe-29e9-45db-b739-56ef6919ea34";
    private static final Uri MY_REDIRECT_URI = Uri.parse("siv://oauth2redirect");
    public static final String AUTHMANAGER_PREF_NAME = "com.secureimageviewer.preference.authmanager";
    public static final String TOKEN_PREF = "com.secureimageviewer.preference.authmanager.token";
    public static final String USERINFO_PREF ="com.secureimageviewer.preference.authmanager.userinfo";

    public static final int AUTH_RESULT_CODE = 54895316;
    private AuthState authState;
    private AuthorizationService authService;
    private UserInfo userInfo;
    private AuthorizationServiceConfiguration serviceConfig;
    private AuthState.AuthStateAction delayedAction;
    private RegistrationId registrationId;
    private boolean isConfigured = false;
    SharedPreferences tokenPref;
    SharedPreferences registrationPref;
    private RegistrationCallback registrationCallback;
    private ArrayList<RegistrationCallback> registrationCallbacks = new ArrayList<>();
    public AuthManager(){
        /*serviceConfig =
                new AuthorizationServiceConfiguration(
                        Uri.parse("https://quigleyid.ddns.net/v1/oauth2/authorize"), // authorization endpoint
                        Uri.parse("https://quigleyid.ddns.net/v1/oauth2/token")); // token endpoint
        */
    }

    public static AuthManager getInstance(){
        return instance;
    }
    private SharedPreferences getTokenPref(){
        if(tokenPref == null)
        {
            if(rootContext != null) {
                tokenPref = rootContext.getSharedPreferences(
                        AUTHMANAGER_PREF_NAME, Context.MODE_PRIVATE);
            }
        }
        return tokenPref;
    }
    private SharedPreferences getRegistrationPref(){
        if(registrationPref == null) {
            registrationPref = rootContext.getSharedPreferences("com.secureimageviewer.registration", Context.MODE_PRIVATE);
        }
        return registrationPref;
    }
    public void ConfigureAuthManager(@NonNull Context context){
        rootContext = context;
        tokenPref = getTokenPref();
        loadAuthState();
        loadRegistrationId();
        authService = new AuthorizationService(context);
        if(authState != null)
            serviceConfig = authState.getAuthorizationServiceConfiguration();
        setAuthState(authState);
        setAuthService(authService);
        registrationComplete();
    }
    public void ConfigureAuthManager(@NonNull AuthorizationServiceConfiguration configuration){
        tokenPref = getTokenPref();
        authService = new AuthorizationService(rootContext);
        String authStateString = getTokenPref().getString(TOKEN_PREF,null);
        serviceConfig = configuration;
        if(authStateString != null){
            try {
                authState = AuthState.jsonDeserialize(authStateString);
                if(serviceConfig != null) {
                    if (authState.getAuthorizationServiceConfiguration() == null)
                        authState = new AuthState(serviceConfig);
                    else if (!authState.getAuthorizationServiceConfiguration().toJsonString().equals(configuration.toJsonString())) {
                        authState = new AuthState(serviceConfig);
                    }
                }
                else
                    authState = new AuthState();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else
        {
            if(serviceConfig != null)
                authState = new AuthState(serviceConfig);
            else
                authState = new AuthState();
        }
        setAuthState(authState);
        setAuthService(authService);
        saveAuthState(rootContext);
        loadRegistrationId();

        registrationComplete();
    }

    private void registrationComplete() {
        isConfigured = true;
        if(registrationCallback != null){
            registrationCallback.onRegistered();
        }
    }

    public void setAuthState(AuthState newAuthState) {
        authState = newAuthState;
    }

    public void setRegistrationId(RegistrationId registrationId){
        this.registrationId = registrationId;
        SharedPreferences.Editor editor = getRegistrationPref().edit();
        editor.putString("registrationId",registrationId.toJsonString());
        editor.apply();
    }
    public void runDelayedActionIfAvailable(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException ex){
        if(delayedAction != null) {
            delayedAction.execute(accessToken, idToken, ex);
            delayedAction = null;
        }
    }
    private void setDelayedAction(AuthState.AuthStateAction action){
        delayedAction = action;
    }

    public void performActionWithFreshTokens(@NonNull Context context, @NonNull AuthState.AuthStateAction action){
        Map<String, String> additionalParams = new HashMap<String,String>();
        additionalParams.put("X-Request-Id", UUID.randomUUID().toString());
            authState.performActionWithFreshTokens(authService,additionalParams, new AuthState.AuthStateAction() {
                @Override
                public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException ex) {
                    if (ex != null) {
                        switch (ex.type){
                            case AuthorizationException.TYPE_OAUTH_AUTHORIZATION_ERROR:
                                case AuthorizationException.TYPE_OAUTH_TOKEN_ERROR:
                                setDelayedAction(action);
                                requestLogin(context);
                                break;
                        }


                    } else {
                        authState.performActionWithFreshTokens(authService, action);
                    }
                    SharedPreferences.Editor editor = getTokenPref().edit();
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
                activity.startActivityForResult(intent, AuthManager.AUTH_RESULT_CODE);
            }
            else
                Toast.makeText(context,"Unable to connect to server",Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(context, "This didnt work", Toast.LENGTH_SHORT).show();
        }
    }
    public void updateAuthState(Context context,AuthorizationResponse resp,AuthorizationException ex) {
        authState.update(resp,ex);
        saveAuthState(context);
    }
    public void updateAuthState(Context context,TokenResponse resp,AuthorizationException ex) {
        authState.update(resp,ex);
        saveAuthState(context);
    }
    private void saveAuthState(@NonNull Context context){
        //SharedPreferences tokenPref  = context.getSharedPreferences(AUTHMANAGER_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = getTokenPref().edit();
        editor.putString(TOKEN_PREF,authState.jsonSerializeString());
        editor.apply();
    }
    private AuthState loadAuthState(){
        String jsonString = getTokenPref().getString(TOKEN_PREF,null);
        if(jsonString == null)
            return null;
        try {
            AuthState loadedAuthState = AuthState.jsonDeserialize(jsonString);
            authState = loadedAuthState;
            return authState;
        } catch (JSONException e) {
            return null;
        }
    }
    private RegistrationId loadRegistrationId(){
        String regIDJson = getRegistrationPref().getString("registrationId",null);
        RegistrationId currentRegId = RegistrationId.fromJsonString(regIDJson);
        registrationId = currentRegId;
        return currentRegId;
    }
    public void performTokenRequest(TokenRequest tokenRequest, AuthorizationService.TokenResponseCallback token) {
        authService.performTokenRequest(tokenRequest,token);
    }
    public void setAuthService(AuthorizationService authService) {
        this.authService = authService;
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
                                authState.update(response,ex);
                                saveAuthState(context);
                                callback.onTokenRequestCompleted(response,ex);
                            }
                        }
                );
            }
        }
        else
            updateAuthState(context, resp, ex);
    }
    public void retrieveUserInfo(Context context) {
        performActionWithFreshTokens(context, new AuthState.AuthStateAction() {
            @Override
            public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException ex) {
                if(ex!= null){

                }
                else
                {
                    try {
                        userInfo = new UserInfoDownloader(context).execute(accessToken).get();
                        SharedPreferences.Editor editor = getTokenPref().edit();
                        editor.putString(USERINFO_PREF,userInfo.jsonSerializeString());
                        editor.apply();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                SharedPreferences tokenPref  = context.getSharedPreferences(AUTHMANAGER_PREF_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = tokenPref.edit();
                editor.putString(TOKEN_PREF,authState.jsonSerializeString());
                editor.apply();
            }
        });
    }
    public UserInfo getUserInfo(){
        if(userInfo != null) {
            return userInfo;
        }
        else
        {
            String userInfoString = getTokenPref().getString(USERINFO_PREF,"");
            if(userInfoString != null){
                return UserInfo.getUserInfoFromJson(userInfoString);
            }
            else
                return null;
        }
    }

    public Intent getAuthorizationRequestIntent() {
        if(serviceConfig == null) {
            checkForConfiguration("https://quigleyid.ddns.net/v1/oauth2/metadata", new AuthorizationServiceConfiguration.RetrieveConfigurationCallback() {
                @Override
                public void onFetchConfigurationCompleted(@Nullable AuthorizationServiceConfiguration serviceConfiguration, @Nullable AuthorizationException ex) {
                    if(ex == null)
                        ConfigureAuthManager(serviceConfiguration);
                }
            });
        }
        if(serviceConfig == null)
            return null;
        AuthorizationRequest.Builder authRequestBuilder =
                new AuthorizationRequest.Builder(
                        serviceConfig, // the authorization service configuration
                        MY_CLIENT_ID, // the client ID, typically pre-registered and static
                        ResponseTypeValues.CODE, // the response_type value: we want a code
                        MY_REDIRECT_URI);
        AuthorizationRequest authRequest = authRequestBuilder
                .setScope("read write")
                .build();
        return authService.getAuthorizationRequestIntent(authRequest);
    }

    public State getState(){
        if(authState == null || authState.getRefreshToken() == null)
            return new State(State.LOGGED_OUT);
        if(authState.getNeedsTokenRefresh())
            return new State(State.NEEDS_REFRESH);
        if(authState.getAccessToken() != null)
            return new State(State.UP_TO_DATE);
        else
            return new State(-1);
    }

    public boolean hasDelayedAction() {
        if(delayedAction != null)
            return true;
        else
            return false;
    }

    public AuthState.AuthStateAction getDelayedAction() {
        AuthState.AuthStateAction result = null;
        if(delayedAction != null) {
            result = delayedAction;
            delayedAction = null;
        }
        return result;
    }

    public boolean isRegistrationIdSet() {
        if(registrationId == null)
            return false;
        if(registrationId.getRegistrationId() == null)
            return false;

        return true;
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if(netInfo != null && netInfo.isConnectedOrConnecting())
        {
            if(AuthManager.getInstance().isAvailable())
                return true;
            else
                return false;
        }
        return false;
        //return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public RegistrationId getRegistrationID() {
        return this.registrationId;
    }

    public void setContext(Context context) {
        this.rootContext = context;
    }

    public void checkForConfiguration(String url, AuthorizationServiceConfiguration.RetrieveConfigurationCallback retrieveConfigurationCallback) {
        AuthorizationServiceConfiguration.fetchFromUrl(Uri.parse(url),
                new AuthorizationServiceConfiguration.RetrieveConfigurationCallback() {

                    @Override
                    public void onFetchConfigurationCompleted(@Nullable AuthorizationServiceConfiguration serviceConfiguration, @Nullable AuthorizationException ex) {
                        AuthManager.getInstance().ConfigureAuthManager(serviceConfiguration);
                        retrieveConfigurationCallback.onFetchConfigurationCompleted(serviceConfiguration,ex);
                    }
                });
    }
    public void setRegistrationCallback(RegistrationCallback callback){
        registrationCallback = callback;
    }

    public boolean isConfigured() {
        return isConfigured;
    }

    public boolean isAvailable(){
        try {
            return new GetAvailable().execute("https://quigleyid.ddns.net/v1/health/available").get();
        } catch (ExecutionException e) {
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean hasValidConfiguration() {
        if(authState == null)
            return false;
        if(authState.getAuthorizationServiceConfiguration() == null)
            return false;
        return true;
    }

    public String getDeviceUuid() {
        return getRegistrationPref().getString("uuid", null);
    }

    public void setDeviceUuid(String deviceId) {
        SharedPreferences.Editor editor = getRegistrationPref().edit();
        editor.putString("uuid",deviceId);
    }

    public class State{
        public static final int LOGGED_OUT = 0;
        public static final int NEEDS_REFRESH = 1;
        public static final int UP_TO_DATE = 2;
        int state;
        public State(int state){
            this.state = state;
        }
        public String getStateDesc(){
            switch (state){
                case LOGGED_OUT:
                    return "Logged out";
                case NEEDS_REFRESH:
                    return "Needs refresh";
                case UP_TO_DATE:
                    return "Logged in";
                default:
                    return "Unknown";
            }
        }
    }

    public static class RegistrationCallback{
        public void onRegistered() {

        }
    }

    public class GetAvailable extends AsyncTask<String,Void,Boolean> {

        @Override
        protected Boolean doInBackground(String... strings) {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.setUseCaches(false);
                connection.setConnectTimeout(4000);
                connection.setReadTimeout(5000);
                Log.d("Get-Request", url.toString());
                int responseCode = connection.getResponseCode();
                if(responseCode == 204)
                    return true;
                else
                    return false;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

        }
    }
}
