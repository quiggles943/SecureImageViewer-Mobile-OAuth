package com.quigglesproductions.secureimageviewer.authentication.pogo.internal;

import com.google.gson.annotations.SerializedName;

public class InternalAuthState {
    @SerializedName("Stage")
    public String authStage;
    @SerializedName("State")
    public String authState;
    @SerializedName("Code")
    public String code;
}
