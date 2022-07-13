package com.quigglesproductions.secureimageviewer.registration;

import com.google.gson.Gson;

public class DeviceRegistrationModel {
    public String device_id;
    public String device_name;
    private static Gson gson = new Gson();

    public String toJsonString(){
        return gson.toJson(this);
    }
}
