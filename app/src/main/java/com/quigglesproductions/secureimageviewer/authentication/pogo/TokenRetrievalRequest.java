package com.quigglesproductions.secureimageviewer.authentication.pogo;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class TokenRetrievalRequest extends TokenRequest{
    @SerializedName("username")
    public String username;
    @SerializedName("password")
    public String password;
    @SerializedName("client_id")
    public String clientId;
    @SerializedName("client_secret")
    public String clientSecret;
    @SerializedName("grant_type")
    String grantType = "password";
    @SerializedName("scope")
    public String scope;
    @SerializedName("audience")
    public List<String> audience;

    public HashMap<String, RequestBody> getPartMap() {
        HashMap<String,RequestBody> result = new HashMap<>();
        result.put("username", RequestBody.create(MediaType.parse("text"),username));
        result.put("password",RequestBody.create(MediaType.parse("text"),password));
        result.put("client_id",RequestBody.create(MediaType.parse("text"),clientId));
        result.put("grant_type",RequestBody.create(MediaType.parse("text"),grantType));
        result.put("client_secret",RequestBody.create(MediaType.parse("text"),clientSecret));
        result.put("scope",RequestBody.create(MediaType.parse("text"),scope));
        return result;
    }
}
