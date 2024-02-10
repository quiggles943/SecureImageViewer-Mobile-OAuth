package com.quigglesproductions.secureimageviewer.room.databases.paging.file.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;


@Entity(primaryKeys = {"FileId","CategoryId"}, tableName = "FileModularCategoryCrossRef",indices = {@Index("FileId"),@Index("CategoryId")})
public class RoomPagingFileCategoryCrossRef {
    @ColumnInfo(name = "FileId")
    public long fileId;
    @ColumnInfo(name = "CategoryId")
    public long categoryId;
}
