package com.quigglesproductions.secureimageviewer.authentication.pogo.internal;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.RequestBody;

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

    public HashMap<String, RequestBody> getPartMap() {
        HashMap<String,RequestBody> result = new HashMap<>();
        result.put("ClientId",RequestBody.create(MediaType.parse("text"),clientId));
        result.put("GrantType",RequestBody.create(MediaType.parse("text"),grantType));
        result.put("RefreshToken",RequestBody.create(MediaType.parse("text"),refreshToken));
        result.put("RequestId",RequestBody.create(MediaType.parse("text"),requestId));
        return result;
    }
}
