package com.quigglesproductions.secureimageviewer.room.databases.system.enums;

import com.quigglesproductions.secureimageviewer.ui.enhancedfileviewer.fragments.BaseFileViewFragment;

import java.util.Locale;

public enum SystemParameter {
    UNKNOWN,

    LAST_UPDATE_TIME,
    LAST_ONLINE_SYNC_TIME,
    OFFLINE_FILE_GROUP_BY;

    public static SystemParameter getFromKey(String key){
        for(SystemParameter type : SystemParameter.values()){
            if(type.toString().contentEquals(key.toUpperCase(Locale.ROOT)))
                return type;
        }
        return UNKNOWN;
    }
}
