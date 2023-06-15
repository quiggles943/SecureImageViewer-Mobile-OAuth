package com.quigglesproductions.secureimageviewer.authentication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.quigglesproductions.secureimageviewer.BuildConfig;
import com.quigglesproductions.secureimageviewer.authentication.pogo.TokenRefreshRequest;
import com.quigglesproductions.secureimageviewer.authentication.pogo.TokenResponse;
import com.quigglesproductions.secureimageviewer.authentication.pogo.TokenRetrievalRequest;
import com.quigglesproductions.secureimageviewer.authentication.retrofit.AuthRequestService;
import com.quigglesproductions.secureimageviewer.models.DeviceStatus;
import com.quigglesproductions.secureimageviewer.registration.DeviceRegistrationModel;
import com.quigglesproductions.secureimageviewer.registration.DeviceRegistrationResponseModel;
import com.quigglesproductions.secureimageviewer.retrofit.RetrofitException;
import com.quigglesproductions.secureimageviewer.ui.ui.login.LoginActivity;

import javax.inject.Inject;

import dagger.Lazy;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Module
@InstallIn(SingletonComponent.class)
public class AuthenticationManager implements IAuthenticationLayer{
    static final String ClientId = "10887ebe-29e9-45db-b739-56ef6919ea34";
    static final String ClientIdDev = "bf289a3f-aed6-4252-96b3-207e299c7357";
    static final String ClientSecret ="15e75e7d0f16490aa1a6a4c80a8c63cb";
    static final String ClientSecretDev = "eda3e728d03945f78315dae42e0aa807";

    public static final int AUTHENTICATION_RESPONSE = 234323;
    private final Context rootContext;
    @Inject
    TokenManager tokenManager;
    @Inject
    AuthRequestService authRequestService;
    @Inject
    DeviceRegistrationManager deviceRegistrationManager;

    @Inject
    public AuthenticationManager(@ApplicationContext Context context){
        this.rootContext = context;
        //tokenManager = new TokenManager(context);
    }


    public TokenRetrievalRequest generateTokenRetrievalRequest(){
        TokenRetrievalRequest request = new TokenRetrievalRequest();
        request.clientId = getClientId();
        request.clientSecret = getClientSecret();
        request.scope = "read, write";
        //request.audience = new String[]{"quigleyserver.ddns.net"};
        return request;
    }
    public TokenRefreshRequest generateTokenRefreshRequest(){
        TokenRefreshRequest request = new TokenRefreshRequest();
        request.clientId = getClientId();
        request.refreshToken = getTokenManager().getRefreshToken();
        return request;
    }

    public Intent getLoginRequestIntent(Activity activity) {
        Intent intent = new Intent(activity, LoginActivity.class);
        return intent;
    }

    public TokenManager getTokenManager() {
        return tokenManager;
    }

    public void updateAuthenticationState(AuthenticationState authenticationState) {
        getTokenManager().setAuthenticaionState(authenticationState);
    }

    String getClientId(){
        if(BuildConfig.BUILD_TYPE.contentEquals("debug"))
            return ClientIdDev;
        else
            return ClientId;
    }
    String getClientSecret(){
        if(BuildConfig.BUILD_TYPE.contentEquals("debug"))
            return ClientSecretDev;
        else
            return ClientSecret;
    }

    private void retrieveNewAccessToken(Callback<TokenResponse> callback){
        TokenRefreshRequest tokenRequest = generateTokenRefreshRequest();
        authRequestService.doRefreshToken(tokenRequest.getPartMap()).enqueue(callback);
    }
    public void retrieveValidAccessToken(TokenRetrievalCallback callback) {
        if(tokenManager.isTokenValid()) {
            callback.tokenRetrieved(tokenManager.getAccessToken(), null);
            return;
        }

        if(!tokenManager.isTokenValid() && tokenManager.isRefreshTokenValid()){
            retrieveNewAccessToken(new Callback<TokenResponse>() {
                @Override
                public void onResponse(Call<TokenResponse> call, Response<TokenResponse> response) {
                    if(response.isSuccessful()) {
                        tokenManager.refreshToken(response.body());
                        callback.tokenRetrieved(tokenManager.getAccessToken(),null);
                    }
                    else{
                        callback.tokenRetrieved(null,new RetrofitException(response));
                    }
                }

                @Override
                public void onFailure(Call<TokenResponse> call, Throwable t) {
                    callback.tokenRetrieved(null,new RetrofitException(t));
                }
            });
        }

    }

    public AuthRequestService getRequestService() {
        return authRequestService;
    }

    public void registerDevice(DeviceRegistrationModel sendModel, Callback<DeviceRegistrationResponseModel> callback) {
        getRequestService().doRegisterDevice("https://quigleyserver.ddns.net:14500/api/v1/device/register",sendModel).enqueue(callback);
    }

    public void getDeviceStatus(Callback<DeviceStatus> callback){
        getRequestService().doGetDeviceStatus("https://quigleyserver.ddns.net:14500/api/v1/device/status", deviceRegistrationManager.getDeviceId()).enqueue(callback);
    }

    public DeviceRegistrationManager getDeviceRegistrationManager() {
        return deviceRegistrationManager;
    }

    public interface TokenRetrievalCallback{
        void tokenRetrieved(String accessToken, Exception exception);
    }

}
