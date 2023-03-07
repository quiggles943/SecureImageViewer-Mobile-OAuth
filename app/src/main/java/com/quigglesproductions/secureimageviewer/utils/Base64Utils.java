package com.quigglesproductions.secureimageviewer.utils;

import android.util.Base64;

import java.nio.charset.StandardCharsets;

public class Base64Utils {

    public static String base64EncodeString(String value){
        byte[] bytesToEncode = value.getBytes(StandardCharsets.UTF_8);
        String result = Base64.encodeToString(bytesToEncode,Base64.DEFAULT);
        result = result.replace("\n","");
        return result+"~";
    }
}
