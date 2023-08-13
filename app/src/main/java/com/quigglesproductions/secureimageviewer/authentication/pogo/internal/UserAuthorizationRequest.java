package com.quigglesproductions.secureimageviewer.authentication.pogo.internal;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UserAuthorizationRequest {
    @SerializedName("Username")
    public String username;
    @SerializedName("Password")
    public String password;
    @SerializedName("ClientId")
    public String clientId;

    @SerializedName("Scope")
    public String scope;
    @SerializedName("Audience")
    public String audience;
    @SerializedName("Location")
    public UserLocation location;
}
