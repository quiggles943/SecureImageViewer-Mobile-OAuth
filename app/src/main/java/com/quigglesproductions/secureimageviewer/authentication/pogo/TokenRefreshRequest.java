package com.quigglesproductions.secureimageviewer.authentication.pogo;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class TokenRefreshRequest extends TokenRequest{
    @SerializedName("client_id")
    public String clientId;
    @SerializedName("refresh_token")
    public String refreshToken;
    @SerializedName("grant_type")
    String grantType = "refresh_token";

    public HashMap<String, RequestBody> getPartMap() {
        HashMap<String,RequestBody> result = new HashMap<>();
        result.put("client_id",RequestBody.create(MediaType.parse("text"),clientId));
        result.put("grant_type",RequestBody.create(MediaType.parse("text"),grantType));
        result.put("refresh_token",RequestBody.create(MediaType.parse("text"),refreshToken));
        return result;
    }
}
