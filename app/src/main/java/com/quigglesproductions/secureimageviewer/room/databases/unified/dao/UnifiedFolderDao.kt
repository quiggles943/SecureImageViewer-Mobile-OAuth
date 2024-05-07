package com.quigglesproductions.secureimageviewer.room.databases.unified.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.quigglesproductions.secureimageviewer.datasource.folder.IFolderDataSource
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedFolder
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.relations.RoomUnifiedEmbeddedFile
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.relations.RoomUnifiedEmbeddedFolder
import java.time.LocalDateTime

@Dao
abstract class UnifiedFolderDao {

    /**
     * Retrieves the folders identified by the specified folderIds
     * @param folderIds an array of the ids of the folders you wish to retrieve
     * @return
     */
    @Transaction
    @Query("SELECT * FROM folders WHERE FolderId IN (:folderIds)")
    abstract suspend fun loadAllByIds(folderIds: IntArray): List<RoomUnifiedEmbeddedFolder>

    /**
     * Retrieves a folder identified by the provided folderId
     * @param folderId the id of the folder to retrieve
     * @return
     */
    @Transaction
    @Query("SELECT * from folders WHERE FolderId = :folderId")
    abstract suspend fun loadFolderById(folderId: Long): RoomUnifiedEmbeddedFolder
    @Query("SELECT * from folders WHERE FolderId = :folderId")
    abstract suspend fun loadPagingFolderById(folderId: Long): RoomUnifiedFolder

    /**
     * Retrieves the folder identified by the proviced online id
     * @param onlineFolderId the online id of the folder to retrieve
     * @return
     */
    @Transaction
    @Query("SELECT * FROM folders WHERE OnlineId = :onlineFolderId")
    abstract suspend fun loadFolderByOnlineId(onlineFolderId: Long): RoomUnifiedEmbeddedFolder

    /**
     * Inserts the folder into database. If a folder with the same online id exists it will update
     * it to match the provided folder
     * @param folder the folder to insert
     * @return the uid of the inserted folder
     */
    @Insert
    suspend fun insert(folder: RoomUnifiedFolder): Long {
        val existingFolder = loadFolderByOnlineId(folder.onlineId.toLong())
        return if (existingFolder != null) {
            folder.uid = existingFolder.folder.uid
            _update(folder)
            existingFolder.folder.uid
        } else {
            folder.folderSourceType = IFolderDataSource.FolderSourceType.LOCAL
            _insert(folder)
        }
    }

    @Insert
    abstract suspend fun _insert(folder: RoomUnifiedFolder): Long
    @Update
    abstract suspend fun _update(folder: RoomUnifiedFolder)
    @Update
    abstract fun update(folder: RoomUnifiedFolder)
    @Insert
    abstract suspend fun insertAll(folders: ArrayList<RoomUnifiedFolder>)
    @Delete
    abstract suspend fun delete(folder: RoomUnifiedFolder)

    @get:Query("SELECT * FROM folders")
    abstract val folders: List<RoomUnifiedFolder>

    @Query("SELECT * FROM folders")
    abstract suspend fun getAllFolders() : List<RoomUnifiedFolder>

    @get:Query("SELECT * FROM folders")
    abstract val embeddedFolders : List<RoomUnifiedEmbeddedFolder>
    suspend fun setThumbnail(selectedFolder: RoomUnifiedEmbeddedFolder, file: RoomUnifiedEmbeddedFile) {
        val confirmedFolder = loadFolderById(selectedFolder.id) ?: return
        confirmedFolder.thumbnailFile = file
        confirmedFolder.folder.onlineThumbnailId = file.onlineId
        _update(confirmedFolder.folder)
    }
    @Query("SELECT RetrievedDate FROM Folders ORDER BY RetrievedDate DESC LIMIT 1 ")
    abstract suspend fun lastUpdated(): LocalDateTime?

    @Query("DELETE FROM Folders")
    abstract suspend fun deleteAll()

    @Transaction
    @Query("SELECT * FROM Folders")
    abstract fun folderPagingSource(): PagingSource<Int, RoomUnifiedFolder>

    @Query("SELECT COUNT(*) FROM Folders")
    abstract fun getFolderCount(): Int

    @Query("SELECT * FROM Folders ORDER BY NormalName ASC LIMIT :loadSize OFFSET :offset ")
    abstract suspend fun getFolders(offset: Int, loadSize: Int): List<RoomUnifiedFolder>
}
