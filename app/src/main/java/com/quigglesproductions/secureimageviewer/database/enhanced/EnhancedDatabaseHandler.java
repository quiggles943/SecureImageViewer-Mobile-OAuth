package com.quigglesproductions.secureimageviewer.database.enhanced;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.quigglesproductions.secureimageviewer.enums.DeviceInfoKey;
import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedArtist;
import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedCategory;
import com.quigglesproductions.secureimageviewer.models.enhanced.EnhancedSubject;
import com.quigglesproductions.secureimageviewer.models.enhanced.metadata.FileMetadata;
import com.quigglesproductions.secureimageviewer.models.enhanced.file.EnhancedDatabaseFile;
import com.quigglesproductions.secureimageviewer.models.enhanced.folder.EnhancedDatabaseFolder;
import com.quigglesproductions.secureimageviewer.models.enhanced.metadata.ImageMetadata;
import com.quigglesproductions.secureimageviewer.models.enhanced.metadata.VideoMetadata;
import com.quigglesproductions.secureimageviewer.utils.ListUtils;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class EnhancedDatabaseHandler {
    Context context;
    SQLiteDatabase database;
    private EnhancedDatabaseHandler(){

    }
    public EnhancedDatabaseHandler(Context context) {
        this.context = context;
        this.database = new EnhancedDatabaseBuilder(context).getWritableDatabase();
    }
    public void setFolderThumbnail(EnhancedDatabaseFolder folder, EnhancedDatabaseFile file) {
        ContentValues values;
        values = new ContentValues();
        values.put(EnhancedDatabaseBuilder.Folders._ID,folder.getId());
        values.put(EnhancedDatabaseBuilder.Folders.THUMBNAIL_ID,file.getOnlineId());
        database.update(EnhancedDatabaseBuilder.Folders.TABLE_NAME, values, "_id=?", new String[]{folder.getId() + ""});  // number 1 is the _id here, update to variable for your code
    }

}
