package com.quigglesproductions.secureimageviewer.room.databases.download.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
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

    @Query("SELECT * FROM FolderDownloadRecord")
    public abstract List<FolderDownloadRecord> getAllFolderRecords();
    @Query("SELECT * FROM FolderDownloadRecord WHERE IsArchived = 0")
    public abstract List<FolderDownloadRecord> getAllActiveFolderRecords();
    @Transaction
    @Query("SELECT * FROM FolderDownloadRecord WHERE IsArchived = 0")
    public abstract List<FolderDownloadPackage> getAllActiveFolderPackages();

    @Transaction
    @Query("SELECT * FROM FolderDownloadRecord WHERE EndTime IS NULL")
    public abstract List<FolderDownloadPackage> getAllNotComplete();

    @Transaction
    @Query("SELECT * FROM FolderDownloadRecord WHERE EndTime IS NOT NULL")
    public abstract List<FolderDownloadRecord> getAllComplete();
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

    public void archiveAll(){
        List<FolderDownloadPackage> folderDownloadRecords = getAllActiveFolderPackages();
        for(FolderDownloadPackage record : folderDownloadRecords){
            record.folderDownloadRecord.isArchived = true;
            update(record.folderDownloadRecord);
            for(FileDownloadRecord fileDownloadRecord : record.fileDownloadRecords){
                fileDownloadRecord.isArchived = true;
                update(fileDownloadRecord);
            }
        }

    }

    public void deleteAllComplete(){
        List<FolderDownloadRecord> records = getAllComplete();
        deleteAll(records.toArray(new FolderDownloadRecord[0]));
    }

    @Delete
    public abstract void deleteAll(FolderDownloadRecord... downloadRecord);
}
