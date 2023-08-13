package com.quigglesproductions.secureimageviewer.authentication.pogo.internal;

import com.google.gson.annotations.SerializedName;

public class InternalAuthError {
    @SerializedName("Code")
    public String code;
    @SerializedName("Message")
    public String message;
    @SerializedName("DetailedMessage")
    public String detailedMessage;
}
