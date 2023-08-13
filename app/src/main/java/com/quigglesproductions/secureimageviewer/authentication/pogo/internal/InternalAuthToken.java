package com.quigglesproductions.secureimageviewer.authentication.pogo.internal;

import com.google.gson.annotations.SerializedName;

public class InternalAuthToken {
    @SerializedName("AccessToken")
    public String accessToken;
    @SerializedName("TokenType")
    public String tokenType;
    @SerializedName("ExpiresIn")
    public String expiresIn;
    @SerializedName("Scope")
    public String scope;
    @SerializedName("RefreshToken")
    public String refreshToken;
    @SerializedName("RefreshExpiresIn")
    public String refreshExpiresIn;
}
