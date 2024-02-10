package com.quigglesproductions.secureimageviewer.authentication.pogo.internal;

import com.google.gson.annotations.SerializedName;
import com.quigglesproductions.secureimageviewer.ui.internallogin.data.AuthenticationState;

public class InternalAuthResponse {
    @SerializedName("Id")
    public String id;
    @SerializedName("StatusCode")
    public String statusCode;
    @SerializedName("State")
    public InternalAuthState authState;
    @SerializedName("Error")
    public InternalAuthError error;
    @SerializedName("Message")
    public String message;
    @SerializedName("Token")
    public InternalAuthToken token;

    public AuthenticationState getAuthenticationState(){
        if(authState != null)
            return AuthenticationState.valueOf(authState.authState);
        else
            return AuthenticationState.ERROR;
    }
}
