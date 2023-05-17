package com.quigglesproductions.secureimageviewer.room.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(primaryKeys = {"FileId","CategoryId"})
public class RoomFileCategoryCrossRef {
    @ColumnInfo(name = "FileId")
    public long fileId;
    @ColumnInfo(name = "CategoryId")
    public long categoryId;
}
