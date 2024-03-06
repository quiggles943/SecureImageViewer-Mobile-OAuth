package com.quigglesproductions.secureimageviewer.aurora.authentication;


import com.quigglesproductions.secureimageviewer.aurora.authentication.appauth.AuroraJsonUtil;
import com.quigglesproductions.secureimageviewer.utils.BooleanUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class AuroraUser {
    public String userId;
    public String userName;
    public String emailAddress;
    public boolean emailVerified;
    public boolean authenticated;

    public String jsonSerializeString(){
        return jsonSerialize().toString();
    }

    public JSONObject jsonSerialize(){
        JSONObject json = new JSONObject();
        AuroraJsonUtil.putIfNotNull(json, "userId", userId);
        AuroraJsonUtil.putIfNotNull(json, "userName", userName);
        AuroraJsonUtil.putIfNotNull(json, "emailAddress", emailAddress);
        AuroraJsonUtil.putIfNotNull(json, "emailVerified", BooleanUtils.getStringFromBool(emailVerified));

        return json;
    }

    public static AuroraUser getFromJson(String jsonString) throws JSONException{
        return getFromJson(new JSONObject(jsonString));
    }

    public static AuroraUser getFromJson(JSONObject json) throws JSONException {
        AuroraUser user = new AuroraUser();
        if (json.has("userId")) {
            user.userId = json.getString("userId");
        }
        if (json.has("userName")) {
            user.userName = json.getString("userName");
        }
        if (json.has("emailAddress")) {
            user.emailAddress = json.getString("emailAddress");
        }
        if (json.has("emailVerified")) {
            user.emailVerified = BooleanUtils.getBoolFromString(json.getString("emailVerified"));
        }
        user.authenticated = false;
        return user;
    }
}
