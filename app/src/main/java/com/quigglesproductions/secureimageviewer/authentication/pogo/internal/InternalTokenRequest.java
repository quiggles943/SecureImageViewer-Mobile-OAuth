package com.quigglesproductions.secureimageviewer.authentication.pogo.internal;

import com.google.gson.annotations.SerializedName;

public class InternalTokenRequest {
    @SerializedName("RequestId")
    public String requestId;
    @SerializedName("GrantType")
    public String grantType;
    @SerializedName("ClientId")
    public String clientId;
    @SerializedName("Code")
    public String authorizationCode;
    @SerializedName("RefreshToken")
    public String refreshToken;
}
