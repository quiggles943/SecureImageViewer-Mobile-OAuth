package com.quigglesproductions.secureimageviewer.room.databases.unified.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;


@Entity(primaryKeys = {"FileId","SubjectId"}, tableName = "FileModularSubjectCrossRef",indices = {@Index("FileId"),@Index("SubjectId")})
public class RoomUnifiedFileSubjectCrossRef {
    @ColumnInfo(name = "FileId")
    @NonNull
    public Long fileId;
    @ColumnInfo(name = "SubjectId")
    @NonNull
    public Long subjectId;
}
