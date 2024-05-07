package com.quigglesproductions.secureimageviewer.aurora.authentication.device;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDateTime;

public class DeviceStatus {
    @SerializedName("Id")
    public String id;
    @SerializedName("UserDeviceId")
    public String userDeviceId;
    @SerializedName("IsActive")
    public boolean isActive;
    @SerializedName("LastCheckin")
    public LocalDateTime lastCheckin;
    @SerializedName("NextRequiredCheckin")
    public LocalDateTime nextRequiredCheckin;
    @SerializedName("ResetDevice")
    public boolean resetDevice;
}
