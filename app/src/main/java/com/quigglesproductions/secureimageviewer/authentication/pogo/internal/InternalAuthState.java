package com.quigglesproductions.secureimageviewer.authentication.pogo.internal;

import com.google.gson.annotations.SerializedName;

public class InternalAuthState {
    @SerializedName("Stage")
    public int authStage;
    @SerializedName("State")
    public int authState;
    @SerializedName("Code")
    public String code;
}
