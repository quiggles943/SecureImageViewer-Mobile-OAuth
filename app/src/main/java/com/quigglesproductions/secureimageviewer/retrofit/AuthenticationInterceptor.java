package com.quigglesproductions.secureimageviewer.retrofit;


import android.content.Context;
import android.content.Intent;

import com.quigglesproductions.secureimageviewer.appauth.AuthManager;
import com.quigglesproductions.secureimageviewer.authentication.AuthenticationManager;
import com.quigglesproductions.secureimageviewer.authentication.TokenManager;
import com.quigglesproductions.secureimageviewer.authentication.pogo.TokenRefreshRequest;
import com.quigglesproductions.secureimageviewer.authentication.pogo.TokenResponse;
import com.quigglesproductions.secureimageviewer.authentication.retrofit.AuthRequestService;
import com.quigglesproductions.secureimageviewer.ui.SecureActivity;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthenticationInterceptor implements Interceptor {

    Context context;
    AuthRequestService apiInterface;
    @Inject
    AuthenticationManager authenticationManager;
    //@Inject
    //public AuthenticationInterceptor(Context context){
    //    this.context = context;
    //}
    @Inject
    public AuthenticationInterceptor(AuthRequestService apiInterface){
        this.apiInterface = apiInterface;
    }
    public AuthenticationInterceptor(Context context){
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        boolean ready = true;
        TokenManager tokenManager = authenticationManager.getTokenManager();
        if(!tokenManager.isTokenValid() && !tokenManager.isRefreshTokenValid()){
            ready = false;
            if(context instanceof SecureActivity){
                //SecureActivity activity = (SecureActivity) context;
                //Intent intent = AuthenticationManager.getInstance().getLoginRequestIntent(activity);
                //context.startActivity(intent);
                //activity.startActivityForResult(intent, AuthenticationManager.AUTHENTICATION_RESPONSE);

            }
        }
        else if(!tokenManager.isTokenValid() && tokenManager.isRefreshTokenValid()){
            //AuthenticationAPIInterface service = AuthenticationAPIClient.getClient().create(AuthenticationAPIInterface.class);
            TokenRefreshRequest tokenRequest = authenticationManager.generateTokenRefreshRequest();
            retrofit2.Response<TokenResponse> response = apiInterface.doRefreshToken(tokenRequest.getPartMap()).execute();
            if(response.isSuccessful()) {
                TokenResponse tokenResponse = response.body();
                tokenManager.refreshToken(tokenResponse);
            }
            else{
                ready = false;
            }
        }
        if(ready) {
            String accessToken = tokenManager.getAccessToken();
            Request request = chain.request();
            if (accessToken == null)
                return chain.proceed(request);

            Request authenticatedRequest = request.newBuilder().header("Authorization", "Bearer " + accessToken).build();
            return chain.proceed(authenticatedRequest);
        }
        else
            return chain.proceed(chain.request());
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
