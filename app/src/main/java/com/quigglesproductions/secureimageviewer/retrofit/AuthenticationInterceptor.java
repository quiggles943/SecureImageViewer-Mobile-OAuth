package com.quigglesproductions.secureimageviewer.retrofit;


import android.content.Context;

import androidx.annotation.NonNull;

import com.quigglesproductions.secureimageviewer.aurora.authentication.appauth.AuroraAuthenticationManager;
import com.quigglesproductions.secureimageviewer.retrofit.annotations.AuthenticationRequired;

import java.io.IOException;

import javax.inject.Inject;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Invocation;

public class AuthenticationInterceptor implements Interceptor {

    Context context;
    @Inject
    AuroraAuthenticationManager auroraAuthenticationManager;
    @Inject
    public AuthenticationInterceptor(){

    }

    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {

        Request request = chain.request();
        Invocation invocation = request.tag(Invocation.class);
        boolean requiresAuthentication = false;
        if(invocation == null)
            requiresAuthentication = true;
        else if(request.tag(Invocation.class) != null && request.tag(Invocation.class).method().getAnnotation(AuthenticationRequired.class) != null)
            requiresAuthentication = true;
        if(requiresAuthentication){
            String freshAccessToken = auroraAuthenticationManager.getFreshAccessToken(context);
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
