package com.quigglesproductions.secureimageviewer.room.databases.modular.file.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;


@Entity(primaryKeys = {"FileId","SubjectId"}, tableName = "FileModularSubjectCrossRef",indices = {@Index("FileId"),@Index("SubjectId")})
public class RoomModularFileSubjectCrossRef {
    @ColumnInfo(name = "FileId")
    public long fileId;
    @ColumnInfo(name = "SubjectId")
    public long subjectId;
}
