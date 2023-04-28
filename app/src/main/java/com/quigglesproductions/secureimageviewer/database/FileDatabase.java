package com.quigglesproductions.secureimageviewer.database;

import android.content.Context;
import android.provider.ContactsContract;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.quigglesproductions.secureimageviewer.database.dao.FileDao;
import com.quigglesproductions.secureimageviewer.database.models.DatabaseFile;

@Database(entities = {DatabaseFile.class},version = 1,exportSchema = false)
public abstract class FileDatabase extends RoomDatabase {
    private static final String DB_NAME = "file_db";
    private static FileDatabase instance;

    public static synchronized FileDatabase getInstance(Context context){
        if(instance == null){
            instance = Room.databaseBuilder(context.getApplicationContext(),FileDatabase.class,DB_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }

    public abstract FileDao fileDao();
}
