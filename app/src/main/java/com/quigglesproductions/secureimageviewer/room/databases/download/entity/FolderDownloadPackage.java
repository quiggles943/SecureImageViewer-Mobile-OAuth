package com.quigglesproductions.secureimageviewer.room.databases.download.entity;

import androidx.annotation.RequiresPermission;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Relation;

import java.util.List;


public class FolderDownloadPackage {
    @Embedded
    public FolderDownloadRecord folderDownloadRecord;
    @Relation(parentColumn = "uid",entityColumn = "FolderRecordId", entity = FileDownloadRecord.class)
    public List<FileDownloadRecord> fileDownloadRecords;
}
