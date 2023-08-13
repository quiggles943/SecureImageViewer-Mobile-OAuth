package com.quigglesproductions.secureimageviewer.authentication.pogo.internal;

import com.google.gson.annotations.SerializedName;

public class InternalTwoFactorRequest {
    @SerializedName("RequestId")
    public String requestId;
    @SerializedName("TwoFactorCode")
    public String twoFactorCode;
}
