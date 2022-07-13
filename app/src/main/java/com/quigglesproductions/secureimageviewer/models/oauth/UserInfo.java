package com.quigglesproductions.secureimageviewer.models.oauth;

import com.google.gson.Gson;

import java.util.Date;

public class UserInfo {
    public String Id;
    public String UserId;
    public String UserName;
    public String FirstName;
    public String LastName;
    public Date DateOfBirth;
    public String EmailAddress;
    public boolean EmailVerified;
    public String PhoneNumber;
    public boolean TwoFactorEnabled;
    public boolean PhoneNumberVerified;
    private static final Gson gson = new Gson();
    public UserInfo(){

    }

    public String jsonSerializeString(){
        return gson.toJson(this);
    }
    public static UserInfo getUserInfoFromJson(String json){
        return gson.fromJson(json,UserInfo.class);
    }
}
