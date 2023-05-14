package com.quigglesproductions.secureimageviewer.appauth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.quigglesproductions.secureimageviewer.App;
import com.quigglesproductions.secureimageviewer.Downloaders.UserInfoDownloader;
import com.quigglesproductions.secureimageviewer.apprequest.requests.DeviceRegistrationRequest;
import com.quigglesproductions.secureimageviewer.models.oauth.UserInfo;
import com.quigglesproductions.secureimageviewer.registration.RegistrationId;
import com.quigglesproductions.secureimageviewer.ui.SecureActivity;
import com.techyourchance.threadposter.BackgroundThreadPoster;
import com.techyourchance.threadposter.UiThreadPoster;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.ResponseTypeValues;
import net.openid.appauth.TokenRequest;
import net.openid.appauth.TokenResponse;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;

public class AuthManager implements IAuthManager{
    private static final AuthManager instance = new AuthManager();
    private Context rootContext;
    public String MY_CLIENT_ID = "10887ebe-29e9-45db-b739-56ef6919ea34";
    private static String scope = "read write";
    private static final Uri MY_REDIRECT_URI = Uri.parse("siv://oauth2redirect");
    public static final String AUTHMANAGER_PREF_NAME = "com.secureimageviewer.preference.authmanager";
    public static final String TOKEN_PREF = "com.secureimageviewer.preference.authmanager.token";
    public static final String USERINFO_PREF ="com.secureimageviewer.preference.authmanager.userinfo";

    public static final int AUTH_RESULT_CODE = 54895316;
    private ViewerAuthState authState;
    private AuthorizationService authService;
    private UserInfo userInfo;
    private AuthorizationServiceConfiguration serviceConfig;
    private AuthState.AuthStateAction delayedAction;
    private DeviceRegistration deviceRegistration;
    private boolean isConfigured = false;
    SharedPreferences tokenPref;
    SharedPreferences registrationPref;
    private RegistrationCallback registrationCallback;
    private boolean loginRequestInProgress;

    private final BackgroundThreadPoster backgroundThreadPoster = new BackgroundThreadPoster();
    private final UiThreadPoster uiThreadPoster = new UiThreadPoster();
    private ArrayList<RegistrationCallback> registrationCallbacks = new ArrayList<>();
    public AuthManager(){

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

    public void ConfigureAuthManager(@NonNull Context context){
        deviceRegistration = new DeviceRegistration(context);
        rootContext = context;
        tokenPref = getTokenPref();
        loadAuthState();
        authService = new AuthorizationService(context);
        if(authState != null)
            serviceConfig = authState.getAuthorizationServiceConfiguration();
        setAuthState(authState);
        setAuthService(authService);
        registrationComplete();
    }
    public void ConfigureAuthManager(@NonNull AuthorizationServiceConfiguration configuration){
        boolean stateOverwritten = false;
        tokenPref = getTokenPref();
        authService = new AuthorizationService(rootContext);
        deviceRegistration = new DeviceRegistration(rootContext);
        String authStateString = getTokenPref().getString(TOKEN_PREF,null);
        serviceConfig = configuration;
        String accessToken = null;
        String refreshToken = null;
        if(authStateString != null){
            try {
                authState = ViewerAuthState.jsonDeserialize(authStateString);
                refreshToken = authState.getRefreshToken();
                accessToken = authState.getAccessToken();
                if(serviceConfig != null) {
                    if (authState.getAuthorizationServiceConfiguration() == null)
                        authState = new ViewerAuthState(serviceConfig);
                    else if (!authState.getAuthorizationServiceConfiguration().toJsonString().equals(configuration.toJsonString())) {
                        authState = new ViewerAuthState(serviceConfig);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else
        {
            if(serviceConfig != null)
                authState = new ViewerAuthState(serviceConfig);
            else
                authState = new ViewerAuthState();
        }
        if(stateOverwritten && refreshToken != null) {
            TokenRequest.Builder reqbuilder = new TokenRequest.Builder(serviceConfig,MY_CLIENT_ID);
            reqbuilder.setScope(scope);
            TokenResponse.Builder builder = new TokenResponse.Builder(reqbuilder.build());
            builder.setAccessToken(accessToken);
            builder.setRefreshToken(refreshToken);
            authState.update(builder.build(), null);
        }
        setAuthState(authState);
        setAuthService(authService);
        saveAuthState(rootContext);
        isConfigured = true;
        registrationComplete();
    }

    private void registrationComplete() {
        isConfigured = true;
        if(registrationCallback != null){
            registrationCallback.onRegistered();
        }
    }

    public void setAuthState(ViewerAuthState newAuthState) {
        if(newAuthState == null)
            authState = null;
        else
            authState = newAuthState;
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
                            setDelayedAction(action);
                            requestLogin(rootContext);
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
                                    if(!loginRequestInProgress){
                                        setDelayedAction(action);
                                        requestLogin(context);
                                    }
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
    public void updateAuthState(Context context,AuthorizationResponse resp,AuthorizationException ex) {
        authState.update(resp,ex);
        loginRequestInProgress = false;
        saveAuthState(context);
    }
    public void updateAuthState(Context context,TokenResponse resp,AuthorizationException ex) {
        authState.update(resp,ex);
        saveAuthState(context);
    }
    private void saveAuthState(@NonNull Context context){
        //SharedPreferences tokenPref  = context.getSharedPreferences(AUTHMANAGER_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = getTokenPref().edit();
        String jsonString = authState.jsonSerializeString();
        editor.putString(TOKEN_PREF,jsonString);
        editor.apply();
    }
    private AuthState loadAuthState(){
        String jsonString = getTokenPref().getString(TOKEN_PREF,null);
        if(jsonString == null)
            return null;
        try {
            ViewerAuthState loadedAuthState = ViewerAuthState.jsonDeserialize(jsonString);
            authState = loadedAuthState;
            return authState;
        } catch (JSONException e) {
            return null;
        }
    }
    /*public RegistrationId generateRegistrationId(){
        if (!AuthManager.getInstance().isRegistrationIdSet()) {
            if (AuthManager.getInstance().getRegistrationID() == null) {
                String deviceId = Settings.Secure.getString(rootContext.getContentResolver(), Settings.Secure.ANDROID_ID);
                //String deviceId = AuthManager.getInstance().getDeviceUuid();
                RegistrationId registrationId = new RegistrationId();
                /*if (deviceId == null) {
                    UUID deviceUuid = UUID.randomUUID();
                    deviceId = deviceUuid.toString();
                    AuthManager.getInstance().setDeviceUuid(deviceId);
                }
                registrationId.setDeviceId(deviceId);
                String deviceName = Settings.Secure.getString(rootContext.getContentResolver(), "bluetooth_name");
                registrationId.setDeviceName(deviceName);
                AuthManager.getInstance().registerDevice(registrationId);
            }
        }
        return loadRegistrationId();
    }*/
    /*private RegistrationId loadRegistrationId(){
        String regIDJson = getRegistrationPref().getString("registrationId",null);
        RegistrationId currentRegId = RegistrationId.fromJsonString(regIDJson);
        registrationId = currentRegId;
        return currentRegId;
    }*/
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
                                long refreshExpirationSeconds = Long.decode(response.additionalParameters.get("refresh_expires_in"));
                                authState.setRefreshTokenExpirationTime(refreshExpirationSeconds);
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
                .setScope(scope)
                .build();
        return authService.getAuthorizationRequestIntent(authRequest);
    }

    public State getState(){
        if(authState == null || authState.getRefreshToken() == null)
            return new State(State.LOGGED_OUT);
        if(authState.getNeedsTokenRefresh())
            return new State(State.NEEDS_REFRESH);
        if(authState.getAccessToken() != null || !authState.getNeedsTokenRefresh())
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

    public static void isOnline(Context context,AuthAvailableCallback callback) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if(netInfo != null && netInfo.isConnectedOrConnecting())
        {
            AuthManager.getInstance().getIsAvailable(callback);
        }
        else {
            callback.requestComplete(false, null);
        }
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

    public void getIsAvailable(AuthAvailableCallback callback){
            new GetAvailable(callback).execute("https://quigleyid.ddns.net/v1/health/available");
    }

    public boolean hasValidConfiguration() {
        if(authState == null)
            return false;
        if(authState.getAuthorizationServiceConfiguration() == null)
            return false;
        return true;
    }



    public void getTokenInfo(){
        TokenInfo tokenInfo = new TokenInfo();
        tokenInfo.accessTokenExpirationTime = new Date(authState.getAccessTokenExpirationTime());
        //tokenInfo.refreshTokenExpirationTime = new Date(authState.getr());
    }

    public Date getAccessTokenExpirationDate() {
        if(authState == null)
            return null;
        if(authState.getAccessTokenExpirationTime() == null)
            return null;
        return new Date(authState.getAccessTokenExpirationTime());
    }

    public Date getRefreshExpirationDate() {
        if(authState == null)
            return null;
        return authState.getRefreshTokenExpirationDate();
    }

    public String getRefreshTokenExpirationDateString() {
        Date date = getRefreshExpirationDate();
        if(date == null)
            return null;
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return format.format(date);
    }

    public String getAccessTokenExpirationDateString() {
        Date date = getAccessTokenExpirationDate();
        if(date == null)
            return null;
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return format.format(date);
    }

    public void getHttpsUrlConnection(@NotNull Context context,@NotNull String urlString,@NotNull UrlConnectionRetrievalCallback callback) {
        performActionWithFreshTokens(context,new AuthState.AuthStateAction() {
            @Override
            public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException ex) {

                    backgroundThreadPoster.post(() ->{
                        try {
                            URL url = new URL(urlString);
                            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                            urlConnection.setRequestProperty("Authorization", "Bearer " + accessToken);
                            if(AuthManager.getInstance().getDeviceRegistration().getRegistrationID() != null)
                                urlConnection.setRequestProperty("X-Device-Id", AuthManager.getInstance().getDeviceRegistration().getRegistrationID().getRegistrationId());
                            urlConnection.setConnectTimeout(10000);
                            Log.d("Get-Request",urlString);
                            uiThreadPoster.post(() ->{
                                callback.UrlConnectionRetrieved(urlConnection,null);
                            });

                        }
                        catch(IOException exception){
                            callback.UrlConnectionRetrieved(null,exception);
                        }
                    });


            }
        });
    }

    public DeviceRegistration getDeviceRegistration() {
        return deviceRegistration;
    }

    public String getAccessToken() {
        if(authState == null)
            return null;
        if(authState.getAccessToken() == null)
            return null;
        else
            return authState.getAccessToken();
    }

    public interface UrlConnectionRetrievalCallback{
        void UrlConnectionRetrieved(HttpsURLConnection connection, IOException exception);
    }
    public class TokenInfo{
        public Date accessTokenExpirationTime;
        public Date refreshTokenExpirationTime;
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

    public class GetAvailable extends AsyncTask<String,Void, GetAvailable.AvailabilityRequestResult> {

        AuthAvailableCallback callback;
        public GetAvailable(AuthAvailableCallback callback) {
            this.callback = callback;
        }

        @Override
        protected GetAvailable.AvailabilityRequestResult doInBackground(String... strings) {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.setUseCaches(false);
                connection.setConnectTimeout(4000);
                connection.setReadTimeout(4000);
                Log.d("Get-Request", url.toString());
                int responseCode = connection.getResponseCode();
                if(responseCode == 204) {
                    return new AvailabilityRequestResult(true);
                }
                else {
                    return new AvailabilityRequestResult(false);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("AuthAvailability",e.getMessage());
                return new AvailabilityRequestResult(false,e);
            }

        }

        @Override
        protected void onPostExecute(AvailabilityRequestResult result) {
            super.onPostExecute(result);
            callback.requestComplete(result.success,result.error);
        }

        public class AvailabilityRequestResult{
            boolean success;
            Exception error;
            public AvailabilityRequestResult(boolean result){
                this.success = result;
            }
            public AvailabilityRequestResult(boolean result, Exception exception){
                success = result;
                error = exception;
            }
        }
    }

    public interface AuthAvailableCallback{
        void requestComplete(boolean available,Exception ex);
    }
}
