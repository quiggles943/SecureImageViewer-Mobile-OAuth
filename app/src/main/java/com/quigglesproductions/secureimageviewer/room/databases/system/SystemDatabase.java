package com.quigglesproductions.secureimageviewer.room.databases.system;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.quigglesproductions.secureimageviewer.room.Converters;
import com.quigglesproductions.secureimageviewer.room.databases.system.dao.SystemParameterDao;
import com.quigglesproductions.secureimageviewer.room.databases.system.entity.Parameter;

@Database(entities = {Parameter.class},version = 1)
@TypeConverters({Converters.class})
public abstract class SystemDatabase extends RoomDatabase {
    public abstract SystemParameterDao systemParameterDao();
}
