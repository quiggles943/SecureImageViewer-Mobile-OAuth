package com.quigglesproductions.secureimageviewer.ui.enhancedfileviewer.fragments;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.quigglesproductions.secureimageviewer.gson.ViewerGson;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedDatabaseFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedOnlineFile;
import com.quigglesproductions.secureimageviewer.ui.compoundcontrols.FileViewerNavigator;
import com.quigglesproductions.secureimageviewer.ui.enhancedfileviewer.EnhancedFileViewFragment;

import java.util.Locale;

public class BaseFileViewFragment extends Fragment {
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
