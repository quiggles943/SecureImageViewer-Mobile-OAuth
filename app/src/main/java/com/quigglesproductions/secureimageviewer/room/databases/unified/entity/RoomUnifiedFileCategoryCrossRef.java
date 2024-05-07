package com.quigglesproductions.secureimageviewer.room.databases.unified.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;


@Entity(primaryKeys = {"FileId","CategoryId"}, tableName = "FileModularCategoryCrossRef",indices = {@Index("FileId"),@Index("CategoryId")})
public class RoomUnifiedFileCategoryCrossRef {
    @ColumnInfo(name = "FileId")
    @NonNull
    public Long fileId;
    @ColumnInfo(name = "CategoryId")
    @NonNull
    public Long categoryId;
}
