package com.quigglesproductions.secureimageviewer.room.databases.download.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.quigglesproductions.secureimageviewer.room.databases.download.entity.FileDownloadRecord;
import com.quigglesproductions.secureimageviewer.room.databases.download.entity.FolderDownloadPackage;
import com.quigglesproductions.secureimageviewer.room.databases.download.entity.FolderDownloadRecord;

import java.util.List;

@Dao
public abstract class DownloadRecordDao {

    @Transaction
    @Query("SELECT * FROM FolderDownloadRecord")
    public abstract List<FolderDownloadPackage> getAll();

    @Transaction
    @Query("SELECT * FROM FolderDownloadRecord")
    public abstract LiveData<List<FolderDownloadPackage>> getAllLiveData();

    @Query("SELECT * FROM FolderDownloadRecord")
    public abstract LiveData<List<FolderDownloadRecord>> getAllFoldersLive();

    @Transaction
    @Query("SELECT * FROM FolderDownloadRecord WHERE EndTime IS NULL")
    public abstract List<FolderDownloadPackage> getAllNotComplete();

    @Insert
    public long insert(FolderDownloadPackage downloadRecord){
        long folderId = insert(downloadRecord.folderDownloadRecord);
        if(downloadRecord.fileDownloadRecords != null && downloadRecord.fileDownloadRecords.size()>0){
            for(FileDownloadRecord record : downloadRecord.fileDownloadRecords){
                record.setFolderRecordId(folderId);
                insert(record);
            }
        }
        return folderId;
    }

    @Insert
    public abstract void insertAll(FolderDownloadRecord... downloadRecord);
    @Insert
    public abstract long insert(FolderDownloadRecord downloadRecord);
    @Insert
    public abstract long insert(FileDownloadRecord fileDownloadRecord);
    @Update
    public abstract void update(FolderDownloadRecord downloadRecord);
    @Update
    public abstract void update(FileDownloadRecord downloadRecord);
}
