package com.quigglesproductions.secureimageviewer.dagger.hilt.module;

import android.content.Context;

import androidx.room.Room;

import com.quigglesproductions.secureimageviewer.room.databases.download.DownloadRecordDatabase;
import com.quigglesproductions.secureimageviewer.room.databases.file.FileDatabase;
import com.quigglesproductions.secureimageviewer.room.databases.system.SystemDatabase;

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
    @Singleton
    public static FileDatabase provideFileDatabase(@ApplicationContext Context context){
        FileDatabase fileDatabase = Room.databaseBuilder(context,FileDatabase.class,"File Database")
                                        .fallbackToDestructiveMigration()
                                        .build();
        return fileDatabase;
    }

    @Provides
    @Singleton
    public static DownloadRecordDatabase provideDownloadRecordDatabase(@ApplicationContext Context context){
        DownloadRecordDatabase downloadRecordDatabase = Room.databaseBuilder(context,DownloadRecordDatabase.class,"Download Record Database")
                .fallbackToDestructiveMigration()
                .build();
        return downloadRecordDatabase;
    }

    @Provides
    @Singleton
    public static SystemDatabase provideSystemDatabase(@ApplicationContext Context context){
        SystemDatabase systemDatabase = Room.databaseBuilder(context,SystemDatabase.class,"System Database")
                .fallbackToDestructiveMigration()
                .build();
        return systemDatabase;
    }
}
