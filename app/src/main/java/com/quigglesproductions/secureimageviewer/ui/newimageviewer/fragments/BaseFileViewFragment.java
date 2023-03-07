package com.quigglesproductions.secureimageviewer.ui.newimageviewer.fragments;

import androidx.fragment.app.Fragment;

import java.util.Locale;

public class BaseFileViewFragment extends Fragment {
    public static final String ARG_FILE_ID = "fileid";
    public static final String ARG_FILE = "file";
    public static final String ARG_FILE_SOURCE_TYPE = "sourceType";

    public enum FileSourceType{
        UNKNOWN,
        DATABASE,
        ONLINE;

        public static FileSourceType getFromKey(String key){
            FileSourceType result = UNKNOWN;
            for(FileSourceType type : FileSourceType.values()){
                if(type.toString().contentEquals(key.toUpperCase(Locale.ROOT)))
                    return type;
            }
            return UNKNOWN;
        }
    }
}
