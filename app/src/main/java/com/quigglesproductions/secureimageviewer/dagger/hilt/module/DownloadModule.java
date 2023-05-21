package com.quigglesproductions.secureimageviewer.dagger.hilt.module;

import android.content.Context;

import com.quigglesproductions.secureimageviewer.database.enhanced.EnhancedDatabaseHandler;
import com.quigglesproductions.secureimageviewer.room.databases.download.DownloadRecordDatabase;
import com.quigglesproductions.secureimageviewer.room.databases.file.FileDatabase;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class DownloadModule {

    @Provides
    public static DownloadManager providesDownloadManager(@ApplicationContext Context context, FileDatabase fileDatabase, DownloadRecordDatabase recordDatabase){
        DownloadManager downloadManager = new DownloadManager(context,recordDatabase);
        downloadManager.setFileDatabase(fileDatabase);

        return downloadManager;
    }
}
