package com.quigglesproductions.secureimageviewer.ui.enhancedfileviewer.fragments;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.quigglesproductions.secureimageviewer.datasource.file.RetrofitFileDataSource;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedDatabaseFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.IDisplayFile;
import com.quigglesproductions.secureimageviewer.models.modular.file.ModularOnlineFile;
import com.quigglesproductions.secureimageviewer.room.databases.file.relations.FileWithMetadata;
import com.quigglesproductions.secureimageviewer.room.databases.modular.file.entity.relations.RoomEmbeddedFile;
import com.quigglesproductions.secureimageviewer.ui.SecureFragment;
import com.quigglesproductions.secureimageviewer.ui.compoundcontrols.FileViewerNavigator;
import com.quigglesproductions.secureimageviewer.ui.data.Result;
import com.quigglesproductions.secureimageviewer.ui.enhancedfileviewer.EnhancedFileViewFragment;

import java.util.Locale;

public class BaseFileViewFragment extends SecureFragment {
    public static final String ARG_FILE_ID = "fileid";
    public static final String ARG_FILE = "file";
    public static final String ARG_FILE_POSITION = "position";
    public static final String ARG_FILE_SOURCE_TYPE = "sourceType";
    private FileViewerNavigator viewerNavigator;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if(getParentFragment() instanceof EnhancedFileViewFragment)
            registerViewerNavigator(((EnhancedFileViewFragment)getParentFragment()).getNavigator());
        setRetainInstance(true);
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
                file = getGson().fromJson(args.getString(ARG_FILE), ModularOnlineFile.class);
                file.setDataSource(new RetrofitFileDataSource(file,requiresAuroraAuthenticationManager()));
                break;
            case DATABASE:
                file = getGson().fromJson(args.getString(ARG_FILE), EnhancedDatabaseFile.class);
                break;
            case ROOM:
                file = getGson().fromJson(args.getString(ARG_FILE), FileWithMetadata.class);
                break;
            case MODULAR:
                file = getGson().fromJson(args.getString(ARG_FILE), RoomEmbeddedFile.class);
                break;
            case PAGING:
                file = getGson().fromJson(args.getString(ARG_FILE), com.quigglesproductions.secureimageviewer.room.databases.paging.file.entity.relations.RoomEmbeddedFile.class);
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
        MODULAR,
        PAGING,
        ONLINE;

        public static FileSourceType getFromKey(String key){
            FileSourceType result = UNKNOWN;
            if(key != null) {
                for (FileSourceType type : FileSourceType.values()) {
                    if (type.toString().contentEquals(key.toUpperCase(Locale.ROOT)))
                        return type;
                }
            }
            return UNKNOWN;
        }
    }
}
