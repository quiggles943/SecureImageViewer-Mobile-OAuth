package com.quigglesproductions.secureimageviewer.dagger.hilt.module;

import android.content.Context;

import com.quigglesproductions.secureimageviewer.database.enhanced.EnhancedDatabaseHandler;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class DownloadModule {

    @Provides
    public static DownloadManager providesDownloadManager(@ApplicationContext Context context, EnhancedDatabaseHandler databaseHandler){
        DownloadManager downloadManager = new DownloadManager(context,databaseHandler);

        return downloadManager;
    }
}
