package com.quigglesproductions.secureimageviewer.room.databases.modular.file;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.quigglesproductions.secureimageviewer.room.Converters;
import com.quigglesproductions.secureimageviewer.room.databases.modular.file.dao.ModularCategoryDao;
import com.quigglesproductions.secureimageviewer.room.databases.modular.file.dao.ModularFileDao;
import com.quigglesproductions.secureimageviewer.room.databases.modular.file.dao.ModularFolderDao;
import com.quigglesproductions.secureimageviewer.room.databases.modular.file.dao.ModularSubjectDao;
import com.quigglesproductions.secureimageviewer.room.databases.modular.file.entity.RoomModularArtist;
import com.quigglesproductions.secureimageviewer.room.databases.modular.file.entity.RoomModularCategory;
import com.quigglesproductions.secureimageviewer.room.databases.modular.file.entity.RoomModularFile;
import com.quigglesproductions.secureimageviewer.room.databases.modular.file.entity.RoomModularFileCategoryCrossRef;
import com.quigglesproductions.secureimageviewer.room.databases.modular.file.entity.RoomModularFileSubjectCrossRef;
import com.quigglesproductions.secureimageviewer.room.databases.modular.file.entity.RoomModularFolder;
import com.quigglesproductions.secureimageviewer.room.databases.modular.file.entity.RoomModularMetadata;
import com.quigglesproductions.secureimageviewer.room.databases.modular.file.entity.RoomModularSubject;

@Database(entities = {RoomModularFolder.class,
        RoomModularFile.class,
        RoomModularMetadata.class,
        RoomModularArtist.class,
        RoomModularCategory.class,
        RoomModularSubject.class,
        RoomModularFileCategoryCrossRef.class,
        RoomModularFileSubjectCrossRef.class},version = 18)
@TypeConverters({Converters.class})
public abstract class ModularFileDatabase extends RoomDatabase {
    public abstract ModularFolderDao folderDao();
    public abstract ModularFileDao fileDao();
    public abstract ModularSubjectDao subjectDao();
    public abstract ModularCategoryDao categoryDao();
}
