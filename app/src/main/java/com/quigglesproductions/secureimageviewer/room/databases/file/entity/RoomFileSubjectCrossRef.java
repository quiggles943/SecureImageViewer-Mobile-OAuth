package com.quigglesproductions.secureimageviewer.room.databases.file.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Fts4;
import androidx.room.Index;


@Entity(primaryKeys = {"FileId","SubjectId"}, tableName = "FileSubjectCrossRef",indices = {@Index("FileId"),@Index("SubjectId")})
public class RoomFileSubjectCrossRef {
    @ColumnInfo(name = "FileId")
    public long fileId;
    @ColumnInfo(name = "SubjectId")
    public long subjectId;
}
