package com.quigglesproductions.secureimageviewer.ui.enhancedfileviewer.fragments;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedDatabaseFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedOnlineFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.IDisplayFile;
import com.quigglesproductions.secureimageviewer.room.databases.file.relations.FileWithMetadata;
import com.quigglesproductions.secureimageviewer.ui.SecureFragment;
import com.quigglesproductions.secureimageviewer.ui.compoundcontrols.FileViewerNavigator;
import com.quigglesproductions.secureimageviewer.ui.enhancedfileviewer.EnhancedFileViewFragment;

import java.util.Locale;

public class BaseFileViewFragment extends SecureFragment {
    public static final String ARG_FILE_ID = "fileid";
    public static final String ARG_FILE = "file";
    public static final String ARG_FILE_SOURCE_TYPE = "sourceType";
    private FileViewerNavigator viewerNavigator;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if(getParentFragment() instanceof EnhancedFileViewFragment)
            registerViewerNavigator(((EnhancedFileViewFragment)getParentFragment()).getNavigator());
        super.onViewCreated(view, savedInstanceState);
    }

    void registerViewerNavigator(FileViewerNavigator navigator){
        viewerNavigator = navigator;
    }
    FileViewerNavigator getViewerNavigator(){
        return viewerNavigator;
    }
    public IDisplayFile getFile(){
        Bundle args = getArguments();
        FileSourceType sourceType = FileSourceType.getFromKey(args.getString(ARG_FILE_SOURCE_TYPE));
        IDisplayFile file;
        switch (sourceType){
            case ONLINE:
                file = (IDisplayFile) getGson().fromJson(args.getString(ARG_FILE), EnhancedOnlineFile.class);
                break;
            case DATABASE:
                file = (IDisplayFile) getGson().fromJson(args.getString(ARG_FILE), EnhancedDatabaseFile.class);
                break;
            case ROOM:
                file = (IDisplayFile) getGson().fromJson(args.getString(ARG_FILE), FileWithMetadata.class);
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
        ROOM,
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
