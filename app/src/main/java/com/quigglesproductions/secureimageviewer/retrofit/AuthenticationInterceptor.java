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
import com.quigglesproductions.secureimageviewer.retrofit.annotations.AuthenticationRequired;
import com.quigglesproductions.secureimageviewer.ui.SecureActivity;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ActivityContext;
import okhttp3.Call;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Invocation;

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
        Invocation invocation = request.tag(Invocation.class);
        boolean requiresAuthentication = false;
        if(invocation == null)
            requiresAuthentication = true;
        else if(request.tag(Invocation.class) != null && request.tag(Invocation.class).method().getAnnotation(AuthenticationRequired.class) != null)
            requiresAuthentication = true;
        if(requiresAuthentication){
            Request authenticatedRequest = request.newBuilder().header("Authorization", "Bearer " + freshAccessToken).build();
            return chain.proceed(authenticatedRequest);
        }
        else
            return chain.proceed(request);
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
