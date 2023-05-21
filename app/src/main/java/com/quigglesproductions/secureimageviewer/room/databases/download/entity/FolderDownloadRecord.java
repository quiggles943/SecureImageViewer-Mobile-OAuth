package com.quigglesproductions.secureimageviewer.room.databases.download.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.time.LocalDateTime;

@Entity(tableName = "FolderDownloadRecord")
public class FolderDownloadRecord {
    @PrimaryKey(autoGenerate = true)
    public long uid;
    @ColumnInfo(name = "WorkerId")
    public String workerId;
    @ColumnInfo(name = "FolderName")
    public String folderName;
    @ColumnInfo(name = "DownloadProgress")
    public int progress;
    @ColumnInfo(name = "DownloadTotal")
    public int fileCount;
    @ColumnInfo(name = "InitiationTime")
    public LocalDateTime initiationTime;
    @ColumnInfo(name = "EndTime")
    public LocalDateTime endTime;
    @ColumnInfo(name = "WasSuccessful")
    public boolean wasSuccessful;

    public void setUid(long uid){
        this.uid = uid;
    }
}
