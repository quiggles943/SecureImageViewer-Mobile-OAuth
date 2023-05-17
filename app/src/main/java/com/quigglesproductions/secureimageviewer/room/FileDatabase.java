package com.quigglesproductions.secureimageviewer.room;

import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.quigglesproductions.secureimageviewer.room.dao.FileDao;
import com.quigglesproductions.secureimageviewer.room.dao.FolderDao;
import com.quigglesproductions.secureimageviewer.room.entity.RoomDatabaseArtist;
import com.quigglesproductions.secureimageviewer.room.entity.RoomDatabaseCategory;
import com.quigglesproductions.secureimageviewer.room.entity.RoomDatabaseFile;
import com.quigglesproductions.secureimageviewer.room.entity.RoomDatabaseFolder;
import com.quigglesproductions.secureimageviewer.room.entity.RoomDatabaseSubject;
import com.quigglesproductions.secureimageviewer.room.entity.RoomFileCategoryCrossRef;
import com.quigglesproductions.secureimageviewer.room.entity.RoomFileMetadata;
import com.quigglesproductions.secureimageviewer.room.entity.RoomFileSubjectCrossRef;

@Database(entities = {RoomDatabaseFolder.class,
        RoomDatabaseFile.class,
        RoomFileMetadata.class,
        RoomDatabaseArtist.class,
        RoomDatabaseCategory.class,
        RoomDatabaseSubject.class,
        RoomFileCategoryCrossRef.class,
        RoomFileSubjectCrossRef.class},version = 4)
@TypeConverters({Converters.class})
public abstract class FileDatabase extends RoomDatabase {
    public abstract FolderDao folderDao();
    public abstract FileDao fileDao();
}
