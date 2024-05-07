package com.quigglesproductions.secureimageviewer.room.databases.system.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.quigglesproductions.secureimageviewer.downloader.DownloadState
import com.quigglesproductions.secureimageviewer.room.databases.system.entity.FolderDownloadWorkerStatus

@Dao
abstract class FolderDownloadWorkerStatusDao {

    @Query("SELECT * FROM FolderDownloadWorkerStatus where WorkerId = :workerId")
    abstract suspend fun getFolderDownloadWorkerStatus(workerId: String): FolderDownloadWorkerStatus

    @Insert
    suspend fun insert(folderDownloadWorkerStatus: FolderDownloadWorkerStatus): Long {
        return _insert(folderDownloadWorkerStatus)
    }

    @Insert
    abstract suspend fun _insert(parameter: FolderDownloadWorkerStatus): Long
    @Delete
    abstract suspend fun _delete(info: FolderDownloadWorkerStatus)
    @Update
    abstract suspend fun update(deviceRegistrationInfo: FolderDownloadWorkerStatus)
    @Query("SELECT * FROM FolderDownloadWorkerStatus where DownloadState = :state")
    abstract suspend fun getDownloadWorkersByState(state: String): List<FolderDownloadWorkerStatus>
}
