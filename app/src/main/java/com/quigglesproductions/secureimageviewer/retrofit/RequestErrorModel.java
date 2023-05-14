package com.quigglesproductions.secureimageviewer.retrofit;

import com.google.gson.annotations.SerializedName;

public class RequestErrorModel {
    @SerializedName("error")
    public String errorName;
    @SerializedName("message")
    public String errorMessage;
    @SerializedName("errorType")
    public int errorType;
    @SerializedName("errorCode")
    public int errorCode;
}
