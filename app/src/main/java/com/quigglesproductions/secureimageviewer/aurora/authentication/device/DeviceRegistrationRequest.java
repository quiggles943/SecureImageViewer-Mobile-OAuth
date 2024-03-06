package com.quigglesproductions.secureimageviewer.aurora.authentication.device;

import com.google.gson.annotations.SerializedName;

public class DeviceRegistrationRequest {
    @SerializedName("device_id")
    String deviceId;
    @SerializedName("device_name")
    String deviceName;

    public DeviceRegistrationRequest(String id, String deviceName){
        deviceId = id;
        this.deviceName = deviceName;
    }
}
