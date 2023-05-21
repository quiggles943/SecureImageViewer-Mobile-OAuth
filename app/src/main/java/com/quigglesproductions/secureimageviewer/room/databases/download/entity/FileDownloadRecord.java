package com.quigglesproductions.secureimageviewer.room.databases.download.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.time.LocalDateTime;

@Entity(tableName = "FileDownloadRecord")
public class FileDownloadRecord {
    @PrimaryKey(autoGenerate = true)
    public long uid;
    @ColumnInfo(name = "FolderRecordId")
    public long folderRecordId;
    @ColumnInfo(name = "WorkerId")
    public String workerId;
    @ColumnInfo(name = "FileName")
    public String fileName;
    @ColumnInfo(name = "InitiationTime")
    public LocalDateTime initiationTime;
    @ColumnInfo(name = "EndTime")
    public LocalDateTime endTime;
    @ColumnInfo(name = "WasSuccessful")
    public boolean wasSuccessful;

    public void setFolderRecordId(long folderRecordId){
        this.folderRecordId = folderRecordId;
    }
    public void setUid(long uid){
        this.uid = uid;
    }
}
