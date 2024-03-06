package com.quigglesproductions.secureimageviewer.dagger.hilt.module;

import android.content.Context;

import com.quigglesproductions.secureimageviewer.dagger.hilt.annotations.DownloadDatabase;
import com.quigglesproductions.secureimageviewer.downloader.PagedFolderDownloader;
import com.quigglesproductions.secureimageviewer.retrofit.DownloadService;
import com.quigglesproductions.secureimageviewer.room.databases.download.DownloadRecordDatabase;
import com.quigglesproductions.secureimageviewer.room.databases.unified.UnifiedFileDatabase;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class DownloadModule {

    @Provides
    public static DownloadManager providesDownloadManager(@ApplicationContext Context context, @DownloadDatabase UnifiedFileDatabase fileDatabase, DownloadRecordDatabase recordDatabase){
        DownloadManager downloadManager = new DownloadManager(context,recordDatabase);
        downloadManager.setFileDatabase(fileDatabase);

        return downloadManager;
    }

    @Provides
    public static PagedFolderDownloader providesPagedFolderDownloader(@ApplicationContext Context context, @DownloadDatabase UnifiedFileDatabase fileDatabase, DownloadService downloadService){
        PagedFolderDownloader downloadManager = new PagedFolderDownloader(context,downloadService,fileDatabase);
        return downloadManager;
    }
}
