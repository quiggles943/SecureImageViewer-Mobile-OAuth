package com.quigglesproductions.secureimageviewer.dagger.hilt.module;

import android.content.Context;

import androidx.room.Room;

import com.quigglesproductions.secureimageviewer.dagger.hilt.annotations.CachingDatabase;
import com.quigglesproductions.secureimageviewer.dagger.hilt.annotations.DownloadDatabase;
import com.quigglesproductions.secureimageviewer.room.databases.download.DownloadRecordDatabase;
import com.quigglesproductions.secureimageviewer.room.databases.system.SystemDatabase;
import com.quigglesproductions.secureimageviewer.room.databases.unified.UnifiedFileDatabase;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {

    @Provides
    @DownloadDatabase
    @Singleton
    public static UnifiedFileDatabase provideDownloadFileDatabase(@ApplicationContext Context context){
        return Room.databaseBuilder(context,UnifiedFileDatabase.class,"Download File Database")
                .fallbackToDestructiveMigration()
                .build();
    }

    @Provides
    @CachingDatabase
    @Singleton
    public static UnifiedFileDatabase provideCachingFileDatabase(@ApplicationContext Context context){
        return Room.databaseBuilder(context,UnifiedFileDatabase.class,"Paging File Database")
                .fallbackToDestructiveMigration()
                .build();
    }

    @Provides
    @Singleton
    public static DownloadRecordDatabase provideDownloadRecordDatabase(@ApplicationContext Context context){
        return Room.databaseBuilder(context,DownloadRecordDatabase.class,"Download Record Database")
                .fallbackToDestructiveMigration()
                .build();
    }

    @Provides
    @Singleton
    public static SystemDatabase provideSystemDatabase(@ApplicationContext Context context){
        return Room.databaseBuilder(context,SystemDatabase.class,"System Database")
                .fallbackToDestructiveMigration()
                .build();
    }
}
