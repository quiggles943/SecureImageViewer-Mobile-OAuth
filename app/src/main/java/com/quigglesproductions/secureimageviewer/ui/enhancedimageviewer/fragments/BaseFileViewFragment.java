package com.quigglesproductions.secureimageviewer.ui.enhancedimageviewer.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.quigglesproductions.secureimageviewer.gson.ViewerGson;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedDatabaseFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedOnlineFile;
import com.quigglesproductions.secureimageviewer.ui.IFileViewer;
import com.quigglesproductions.secureimageviewer.ui.compoundcontrols.FileViewerNavigator;

import java.util.Locale;

public class BaseFileViewFragment extends Fragment {
    public static final String ARG_FILE_ID = "fileid";
    public static final String ARG_FILE = "file";
    public static final String ARG_FILE_SOURCE_TYPE = "sourceType";

    public EnhancedFile getFile(){
        Bundle args = getArguments();
        FileSourceType sourceType = FileSourceType.getFromKey(args.getString(ARG_FILE_SOURCE_TYPE));
        EnhancedFile file;
        switch (sourceType){
            case ONLINE:
                file = ViewerGson.getGson().fromJson(args.getString(ARG_FILE), EnhancedOnlineFile.class);
                break;
            case DATABASE:
                file = ViewerGson.getGson().fromJson(args.getString(ARG_FILE), EnhancedDatabaseFile.class);
                break;
            default:
                file = null;
                break;
        }
        return file;
    }

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
