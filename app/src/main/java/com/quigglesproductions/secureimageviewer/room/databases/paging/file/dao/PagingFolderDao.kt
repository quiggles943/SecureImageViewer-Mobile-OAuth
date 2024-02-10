package com.quigglesproductions.secureimageviewer.room.databases.paging.file.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.quigglesproductions.secureimageviewer.room.databases.paging.file.entity.RoomPagingFolder
import com.quigglesproductions.secureimageviewer.room.databases.paging.file.entity.relations.RoomEmbeddedFile
import com.quigglesproductions.secureimageviewer.room.databases.paging.file.entity.relations.RoomEmbeddedFolder

@Dao
abstract class PagingFolderDao {

    /**
     * Retrieves the folders identified by the specified folderIds
     * @param folderIds an array of the ids of the folders you wish to retrieve
     * @return
     */
    @Transaction
    @Query("SELECT * FROM folders WHERE FolderId IN (:folderIds)")
    abstract fun loadAllByIds(folderIds: IntArray): List<RoomEmbeddedFolder>

    /**
     * Retrieves a folder identified by the provided folderId
     * @param folderId the id of the folder to retrieve
     * @return
     */
    @Transaction
    @Query("SELECT * from folders WHERE FolderId = :folderId")
    abstract fun loadFolderById(folderId: Long): RoomEmbeddedFolder
    @Query("SELECT * from folders WHERE FolderId = :folderId")
    abstract suspend fun loadPagingFolderById(folderId: Long): RoomPagingFolder

    /**
     * Retrieves the folder identified by the proviced online id
     * @param onlineFolderId the online id of the folder to retrieve
     * @return
     */
    @Transaction
    @Query("SELECT * FROM folders WHERE OnlineId = :onlineFolderId")
    abstract fun loadFolderByOnlineId(onlineFolderId: Long): RoomEmbeddedFolder

    /**
     * Inserts the folder into database. If a folder with the same online id exists it will update
     * it to match the provided folder
     * @param folder the folder to insert
     * @return the uid of the inserted folder
     */
    @Insert
    fun insert(folder: RoomPagingFolder): Long {
        val existingFolder = loadFolderByOnlineId(folder.onlineId.toLong())
        return if (existingFolder != null) {
            folder.uid = existingFolder.folder.uid
            _update(folder)
            existingFolder.folder.uid
        } else {
            _insert(folder)
        }
    }

    @Insert
    abstract fun _insert(folder: RoomPagingFolder): Long
    @Update
    abstract fun _update(folder: RoomPagingFolder)
    @Insert
    abstract fun insertAll(vararg folders: RoomPagingFolder)
    @Delete
    abstract fun delete(folder: RoomPagingFolder)

    @get:Query("SELECT * FROM folders")
    abstract val folders: List<RoomPagingFolder>
    fun setThumbnail(selectedFolder: RoomEmbeddedFolder, file: RoomEmbeddedFile) {
        val confirmedFolder = loadFolderById(selectedFolder.id) ?: return
        confirmedFolder.thumbnailFile = file
        confirmedFolder.folder.onlineThumbnailId = file.onlineId
        _update(confirmedFolder.folder)
    }
}
