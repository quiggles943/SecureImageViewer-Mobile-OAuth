package com.quigglesproductions.secureimageviewer.room.databases.file.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Fts4;
import androidx.room.Index;


@Entity(primaryKeys = {"FileId","CategoryId"}, tableName = "FileCategoryCrossRef",indices = {@Index("FileId"),@Index("CategoryId")})
public class RoomFileCategoryCrossRef {
    @ColumnInfo(name = "FileId")
    public long fileId;
    @ColumnInfo(name = "CategoryId")
    public long categoryId;
}
