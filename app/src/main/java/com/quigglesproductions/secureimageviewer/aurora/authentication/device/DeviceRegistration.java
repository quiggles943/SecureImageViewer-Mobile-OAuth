package com.quigglesproductions.secureimageviewer.aurora.authentication.device;

import com.google.gson.annotations.SerializedName;

public class DeviceRegistration {
    @SerializedName("Id")
    public String id;
    @SerializedName("DeviceId")
    public String deviceId;
    @SerializedName("DeviceName")
    public String deviceName;
    @SerializedName("DeviceStatus")
    public DeviceStatus deviceStatus;
}
