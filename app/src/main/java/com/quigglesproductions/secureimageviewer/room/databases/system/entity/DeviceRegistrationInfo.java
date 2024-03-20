package com.quigglesproductions.secureimageviewer.room.databases.system.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.quigglesproductions.secureimageviewer.aurora.authentication.device.DeviceRegistration;

import java.time.LocalDateTime;

@Entity(tableName = "DeviceRegistrationInfo")
public class DeviceRegistrationInfo {
    @PrimaryKey(autoGenerate = true)
    public long uid;
    @ColumnInfo(name = "OnlineId")
    public String onlineId;
    @ColumnInfo(name = "DeviceId")
    public String deviceId;
    @ColumnInfo(name = "DeviceName")
    public String deviceName;
    @ColumnInfo(name = "LastCheckin")
    public LocalDateTime lastCheckin;
    @ColumnInfo(name = "NextRequiredCheckin")
    public LocalDateTime nextRequiredCheckin;
    @ColumnInfo(name = "IsActive")
    public boolean isActive;

    public static DeviceRegistrationInfo fromDeviceRegistration(DeviceRegistration registration){
        DeviceRegistrationInfo info = new DeviceRegistrationInfo();
        info.onlineId = registration.id;
        info.deviceId = registration.deviceId;
        info.deviceName = registration.deviceName;
        info.lastCheckin = registration.deviceStatus.lastCheckin;
        info.nextRequiredCheckin = registration.deviceStatus.nextRequiredCheckin;
        return info;
    }
}
