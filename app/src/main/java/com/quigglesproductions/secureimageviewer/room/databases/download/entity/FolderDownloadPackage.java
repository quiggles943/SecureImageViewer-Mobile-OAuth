package com.quigglesproductions.secureimageviewer.room.databases.download.entity;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;


public class FolderDownloadPackage {
    @Embedded
    public FolderDownloadRecord folderDownloadRecord;
    @Relation(parentColumn = "uid",entityColumn = "FolderRecordId", entity = FileDownloadRecord.class)
    public List<FileDownloadRecord> fileDownloadRecords;
}
