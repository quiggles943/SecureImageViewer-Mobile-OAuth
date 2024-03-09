package com.quigglesproductions.secureimageviewer.room.databases.system;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.quigglesproductions.secureimageviewer.room.Converters;
import com.quigglesproductions.secureimageviewer.room.databases.system.dao.DeviceRegistrationDao;
import com.quigglesproductions.secureimageviewer.room.databases.system.dao.FolderDownloadWorkerStatusDao;
import com.quigglesproductions.secureimageviewer.room.databases.system.dao.SystemParameterDao;
import com.quigglesproductions.secureimageviewer.room.databases.system.entity.DeviceRegistrationInfo;
import com.quigglesproductions.secureimageviewer.room.databases.system.entity.FolderDownloadWorkerStatus;
import com.quigglesproductions.secureimageviewer.room.databases.system.entity.Parameter;

@Database(entities = {Parameter.class, DeviceRegistrationInfo.class, FolderDownloadWorkerStatus.class},version = 6)
@TypeConverters({Converters.class})
public abstract class SystemDatabase extends RoomDatabase {
    public abstract SystemParameterDao systemParameterDao();
    public abstract DeviceRegistrationDao deviceRegistrationDao();
    public abstract FolderDownloadWorkerStatusDao folderDownloadWorkerStatusDao();
}
