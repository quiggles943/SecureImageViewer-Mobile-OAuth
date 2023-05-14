package com.quigglesproductions.secureimageviewer.authentication.pogo;

import com.google.gson.annotations.SerializedName;

public class TokenResponse {
    @SerializedName("access_token")
    public String access_token;
    @SerializedName("token_type")
    public String token_type;
    @SerializedName("expires_in")
    public String expires_in;
    @SerializedName("scope")
    public String scope;
    @SerializedName("refresh_token")
    public String refresh_token;
    @SerializedName("refresh_expires_in")
    public String refresh_expires_in;
}
