package com.quigglesproductions.secureimageviewer.room.databases.file;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.quigglesproductions.secureimageviewer.room.Converters;
import com.quigglesproductions.secureimageviewer.room.databases.file.dao.CategoryDao;
import com.quigglesproductions.secureimageviewer.room.databases.file.dao.FileDao;
import com.quigglesproductions.secureimageviewer.room.databases.file.dao.FolderDao;
import com.quigglesproductions.secureimageviewer.room.databases.file.dao.SubjectDao;
import com.quigglesproductions.secureimageviewer.room.databases.file.entity.RoomDatabaseArtist;
import com.quigglesproductions.secureimageviewer.room.databases.file.entity.RoomDatabaseCategory;
import com.quigglesproductions.secureimageviewer.room.databases.file.entity.RoomDatabaseFile;
import com.quigglesproductions.secureimageviewer.room.databases.file.entity.RoomDatabaseFolder;
import com.quigglesproductions.secureimageviewer.room.databases.file.entity.RoomDatabaseSubject;
import com.quigglesproductions.secureimageviewer.room.databases.file.entity.RoomFileCategoryCrossRef;
import com.quigglesproductions.secureimageviewer.room.databases.file.entity.RoomFileMetadata;
import com.quigglesproductions.secureimageviewer.room.databases.file.entity.RoomFileSubjectCrossRef;

@Database(entities = {RoomDatabaseFolder.class,
        RoomDatabaseFile.class,
        RoomFileMetadata.class,
        RoomDatabaseArtist.class,
        RoomDatabaseCategory.class,
        RoomDatabaseSubject.class,
        RoomFileCategoryCrossRef.class,
        RoomFileSubjectCrossRef.class},version = 8)
@TypeConverters({Converters.class})
public abstract class FileDatabase extends RoomDatabase {
    public abstract FolderDao folderDao();
    public abstract FileDao fileDao();
    public abstract SubjectDao subjectDao();
    public abstract CategoryDao categoryDao();
}
