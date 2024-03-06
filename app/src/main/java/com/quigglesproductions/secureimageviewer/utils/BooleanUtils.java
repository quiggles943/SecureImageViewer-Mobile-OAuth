package com.quigglesproductions.secureimageviewer.utils;

import java.util.Locale;

public class BooleanUtils {

    public static boolean getBoolFromInt(int iVal){
        if(iVal == 0)
            return false;
        else
            return true;
    }

    public static boolean getBoolFromString(String sVal){
        switch (sVal.toUpperCase(Locale.ROOT)){
            case "YES":
            case "Y":
            case "1":
            case "-1":
            case "TRUE":
                return true;
            case "NO":
            case "N":
            case "0":
            case "FALSE":
                return false;
            default:
                return false;
        }
    }

    public static String getStringFromBool(boolean bool){
        if(bool)
            return "True";
        else
            return "False";
    }
}
