package com.quigglesproductions.secureimageviewer.retrofit;


import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.Nullable;

import com.quigglesproductions.secureimageviewer.appauth.AuthManager;
import com.quigglesproductions.secureimageviewer.aurora.appauth.AuroraAuthenticationManager;
import com.quigglesproductions.secureimageviewer.authentication.AuthenticationManager;
import com.quigglesproductions.secureimageviewer.authentication.TokenManager;
import com.quigglesproductions.secureimageviewer.authentication.pogo.TokenRefreshRequest;
import com.quigglesproductions.secureimageviewer.authentication.pogo.TokenResponse;
import com.quigglesproductions.secureimageviewer.authentication.pogo.internal.InternalAuthResponse;
import com.quigglesproductions.secureimageviewer.authentication.pogo.internal.InternalTokenRequest;
import com.quigglesproductions.secureimageviewer.authentication.retrofit.AuthRequestService;
import com.quigglesproductions.secureimageviewer.ui.SecureActivity;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ActivityContext;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthenticationInterceptor implements Interceptor {

    Context context;
    @Inject
    AuthenticationManager authenticationManager;
    @Inject
    AuroraAuthenticationManager auroraAuthenticationManager;
    @Inject
    public AuthenticationInterceptor(){

    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        //boolean ready = true;
        String freshAccessToken = auroraAuthenticationManager.getFreshAccessToken(context);
        Request request = chain.request();
        Request authenticatedRequest = request.newBuilder().header("Authorization", "Bearer " + freshAccessToken).build();
        return chain.proceed(authenticatedRequest);
        /*TokenManager tokenManager = authenticationManager.getTokenManager();
        if(!tokenManager.isTokenValid() && !tokenManager.isRefreshTokenValid()){
            ready = false;
            if(context instanceof SecureActivity){

            }
        }
        else if(!tokenManager.isTokenValid() && tokenManager.isRefreshTokenValid()){
            InternalTokenRequest tokenRequest = authenticationManager.generateInternalTokenRequest(AuthenticationManager.TokenRequestType.REFRESH_TOKEN);

            retrofit2.Response<InternalAuthResponse> response = apiInterface.doRetrieveToken(tokenRequest).execute();
            if(response.isSuccessful()) {
                InternalAuthResponse tokenResponse = response.body();
                tokenManager.refreshToken(tokenResponse.token);
                Log.i("Token-Refresh","Token refreshed successfully");
            }
            else{
                Log.e("Token-Refresh",response.errorBody().string());
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
            return chain.proceed(chain.request());*/
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
