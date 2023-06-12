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
    @ColumnInfo(name = "FolderId")
    public long folderId;
    @ColumnInfo(name = "FolderName")
    public String folderName;
    @ColumnInfo(name = "DownloadProgress")
    public int progress;
    @ColumnInfo(name = "DownloadTotal")
    public int fileTotalCount;
    @ColumnInfo(name = "InitiationTime")
    public LocalDateTime initiationTime;
    @ColumnInfo(name = "EndTime")
    public LocalDateTime endTime;
    @ColumnInfo(name = "WasSuccessful")
    public boolean wasSuccessful;
    @ColumnInfo(name = "IsArchived")
    public boolean isArchived;

    public void setUid(long uid){
        this.uid = uid;
    }

    public String getStatus(){
        if(endTime == null)
            return "Downloading";
        else if(!wasSuccessful){
            return "Downloaded with errors";
        }
        else {
            return "Downloaded";
        }
    }
}
