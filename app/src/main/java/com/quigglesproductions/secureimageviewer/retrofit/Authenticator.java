package com.quigglesproductions.secureimageviewer.retrofit;

import android.content.Context;

import com.quigglesproductions.secureimageviewer.authentication.AuthenticationManager;
import com.quigglesproductions.secureimageviewer.authentication.AuthenticationState;
import com.quigglesproductions.secureimageviewer.authentication.TokenManager;
import com.quigglesproductions.secureimageviewer.authentication.pogo.TokenRefreshRequest;
import com.quigglesproductions.secureimageviewer.authentication.pogo.TokenResponse;
import com.quigglesproductions.secureimageviewer.authentication.retrofit.AuthRequestService;
import com.quigglesproductions.secureimageviewer.authentication.retrofit.AuthenticationAPIClient;

import java.io.IOException;

import javax.inject.Inject;

import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

public class Authenticator implements okhttp3.Authenticator {
    Context context;
    @Inject
    AuthenticationManager authenticationManager;
    public Authenticator(Context context){
        this.context = context;
    }
    @Override
    public Request authenticate(Route route, Response response) throws IOException {
        TokenManager tokenManager = authenticationManager.getTokenManager();
        AuthenticationState authenticationState = tokenManager.getAuthenticationState();
        if(authenticationState == null){
            return null;
        }
        else{
            AuthRequestService service = AuthenticationAPIClient.getClient().create(AuthRequestService.class);
            TokenRefreshRequest tokenRequest = new TokenRefreshRequest();
            tokenRequest.refreshToken = authenticationState.getRefreshToken();
            TokenResponse tokenResponse = service.doRefreshToken(tokenRequest.getPartMap()).execute().body();
            AuthenticationState newAuthState = tokenManager.refreshToken(tokenResponse);
            return response.request().newBuilder().addHeader("Authorization","Bearer "+newAuthState.getAccessToken()).build();

        }
    }
}
