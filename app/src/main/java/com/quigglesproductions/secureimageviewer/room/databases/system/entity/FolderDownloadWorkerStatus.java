package com.quigglesproductions.secureimageviewer.room.databases.system.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.work.WorkInfo;

import com.quigglesproductions.secureimageviewer.downloader.DownloadState;

import java.util.UUID;

@Entity(tableName = "FolderDownloadWorkerStatus")
public class FolderDownloadWorkerStatus {
    @PrimaryKey(autoGenerate = true)
    public long uid;
    @ColumnInfo(name = "WorkerId")
    public UUID workerId;
    @ColumnInfo(name = "WorkerName")
    public String workerName;
    @ColumnInfo(name = "FolderId")
    public long folderId;
    @ColumnInfo(name = "FolderName")
    public String folderName;
    @ColumnInfo(name = "DownloadState")
    public DownloadState downloadState;
    @ColumnInfo(name = "WorkManagerState")
    public WorkInfo.State workManagerState;
    @ColumnInfo(name = "FileCount")
    public int fileCount;
    @ColumnInfo(name = "DownloadedFileCount")
    public int downloadedFileCount;
    @ColumnInfo(name = "ErrorFileCount")
    public int errorFileCount;
    @ColumnInfo(name = "IsComplete")
    public boolean isComplete;
    @ColumnInfo(name = "IsSuccessful")
    public boolean isSuccessful;
}
