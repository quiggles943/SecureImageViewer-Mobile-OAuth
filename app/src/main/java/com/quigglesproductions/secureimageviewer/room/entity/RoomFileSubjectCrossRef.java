package com.quigglesproductions.secureimageviewer.room.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(primaryKeys = {"FileId","SubjectId"})
public class RoomFileSubjectCrossRef {
    @ColumnInfo(name = "FileId")
    public long fileId;
    @ColumnInfo(name = "SubjectId")
    public long subjectId;
}
