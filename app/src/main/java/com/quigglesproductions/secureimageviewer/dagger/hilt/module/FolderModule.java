package com.quigglesproductions.secureimageviewer.dagger.hilt.module;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Room;

import com.quigglesproductions.secureimageviewer.dagger.hilt.annotations.CachingDatabase;
import com.quigglesproductions.secureimageviewer.dagger.hilt.annotations.DownloadDatabase;
import com.quigglesproductions.secureimageviewer.managers.FolderManager;
import com.quigglesproductions.secureimageviewer.room.databases.download.DownloadRecordDatabase;
import com.quigglesproductions.secureimageviewer.room.databases.system.SystemDatabase;
import com.quigglesproductions.secureimageviewer.room.databases.unified.UnifiedFileDatabase;

import org.jetbrains.annotations.Contract;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class FolderModule {

    @NonNull
    @Provides
    @Singleton
    public static FolderManager provideFolderManager(@ApplicationContext Context context){
        return new FolderManager(context);
    }
}
