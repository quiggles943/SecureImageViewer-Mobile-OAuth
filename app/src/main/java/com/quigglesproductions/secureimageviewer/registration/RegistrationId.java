package com.quigglesproductions.secureimageviewer.registration;

import com.google.gson.Gson;
import com.quigglesproductions.secureimageviewer.gson.ViewerGson;

import java.time.LocalDateTime;

public class RegistrationId {
    private String registrationId;
    private String deviceId;
    private String deviceName;
    private LocalDateTime nextCheckIn;
    public RegistrationId(){

    }

    public String getDeviceId(){
        return this.deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getRegistrationId() {
        return registrationId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
    }

    public void setNextCheckIn(LocalDateTime nextCheckIn) {
        this.nextCheckIn = nextCheckIn;
    }

    public LocalDateTime getNextCheckIn() {
        return nextCheckIn;
    }

    public String toJsonString(){
        return ViewerGson.getGson().toJson(this);
    }

    public static RegistrationId fromJsonString(String string){
        return ViewerGson.getGson().fromJson(string,RegistrationId.class);
    }
}
