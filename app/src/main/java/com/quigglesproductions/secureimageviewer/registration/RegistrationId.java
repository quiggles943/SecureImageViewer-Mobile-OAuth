package com.quigglesproductions.secureimageviewer.registration;

import com.google.gson.Gson;

public class RegistrationId {
    private String registrationId;
    private String deviceId;
    private String deviceName;
    private static Gson gson = new Gson();
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

    public String toJsonString(){
        return gson.toJson(this);
    }

    public static RegistrationId fromJsonString(String string){
        return gson.fromJson(string,RegistrationId.class);
    }
}
