package com.quigglesproductions.secureimageviewer.models;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDateTime;

public class DeviceStatus {
    @SerializedName("Id")
    public String statusId;
    @SerializedName("UserDeviceId")
    public String userDeviceId;
    @SerializedName("IsActive")
    public boolean isActive;
    @SerializedName("LastCheckin")
    public LocalDateTime previousCheckin;
    @SerializedName("ResetDevice")
    public boolean resetDevice;
    @SerializedName("NextRequiredCheckin")
    public LocalDateTime nextRequiredCheckin;
}
