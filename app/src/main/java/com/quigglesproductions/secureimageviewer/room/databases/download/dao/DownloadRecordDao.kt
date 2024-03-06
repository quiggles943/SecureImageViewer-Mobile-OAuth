package com.quigglesproductions.secureimageviewer.room.databases.download.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.quigglesproductions.secureimageviewer.room.databases.download.entity.FileDownloadRecord
import com.quigglesproductions.secureimageviewer.room.databases.download.entity.FolderDownloadPackage
import com.quigglesproductions.secureimageviewer.room.databases.download.entity.FolderDownloadRecord

@Dao
abstract class DownloadRecordDao {
    @Query("SELECT * FROM FolderDownloadRecord")
    @Transaction
    abstract fun all(): List<FolderDownloadPackage>

    @Query("SELECT * FROM FolderDownloadRecord")
    abstract fun allFoldersLive(): LiveData<List<FolderDownloadRecord>>

    @Query("SELECT * FROM FolderDownloadRecord WHERE IsArchived = 0")
    @Transaction
    abstract fun allActiveFolderPackages(): List<FolderDownloadPackage>

    @Query("SELECT * FROM FolderDownloadRecord WHERE EndTime IS NOT NULL")
    @Transaction
    abstract fun allComplete(): List<FolderDownloadRecord>
    @Insert
    suspend fun insert(downloadRecord: FolderDownloadPackage): Long {
        val folderId = insert(downloadRecord.folderDownloadRecord)
        if (downloadRecord.fileDownloadRecords != null && downloadRecord.fileDownloadRecords.size > 0) {
            for (record in downloadRecord.fileDownloadRecords) {
                record.setFolderRecordId(folderId)
                insert(record)
            }
        }
        return folderId
    }

    @Insert
    abstract suspend fun insert(downloadRecord: FolderDownloadRecord): Long
    @Insert
    abstract fun insert(fileDownloadRecord: FileDownloadRecord): Long
    @Update
    abstract fun update(downloadRecord: FolderDownloadRecord)
    @Update
    abstract fun update(downloadRecord: FileDownloadRecord)
    fun deleteAllComplete() {
        val records = allComplete()
        deleteAll(*records.toTypedArray<FolderDownloadRecord>())
    }

    @Delete
    abstract fun deleteAll(vararg downloadRecord: FolderDownloadRecord)
}
