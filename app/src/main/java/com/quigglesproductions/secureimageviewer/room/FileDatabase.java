package com.quigglesproductions.secureimageviewer.room;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.quigglesproductions.secureimageviewer.room.dao.FolderDao;
import com.quigglesproductions.secureimageviewer.room.entity.RoomDatabaseFolder;

@Database(entities = {RoomDatabaseFolder.class},version = 1)
public abstract class FileDatabase extends RoomDatabase {
    public abstract FolderDao folderDao();
}
