package com.quigglesproductions.secureimageviewer.registration;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

public class DeviceRegistrationModel {
    @SerializedName("device_id")
    public String deviceId;
    @SerializedName("device_name")
    public String deviceName;
    private static Gson gson = new Gson();

    public String toJsonString(){
        return gson.toJson(this);
    }
}
