package com.quigglesproductions.secureimageviewer.room.databases.modular.file.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.quigglesproductions.secureimageviewer.room.databases.modular.file.entity.RoomModularFolder;
import com.quigglesproductions.secureimageviewer.room.databases.modular.file.entity.relations.RoomEmbeddedFile;
import com.quigglesproductions.secureimageviewer.room.databases.modular.file.entity.relations.RoomEmbeddedFolder;

import java.util.List;

@Dao
public abstract class ModularFolderDao {

    /**
     * Retrieves all folders stored within the database
     * @return
     */
    @Transaction
    @Query("SELECT * FROM Folders")
    public abstract List<RoomEmbeddedFolder> getAll();

    /**
     * Retrieves the folders identified by the specified folderIds
     * @param folderIds an array of the ids of the folders you wish to retrieve
     * @return
     */
    @Transaction
    @Query("SELECT * FROM Folders WHERE FolderId IN (:folderIds)")
    public abstract List<RoomEmbeddedFolder> loadAllByIds(int[] folderIds);

    /**
     * Retrieves a folder identified by the provided folderId
     * @param folderId the id of the folder to retrieve
     * @return
     */
    @Transaction
    @Query("SELECT * from Folders WHERE FolderId = :folderId")
    public abstract RoomEmbeddedFolder loadFolderById(long folderId);

    /**
     * Retrieves the folder identified by the proviced online id
     * @param onlineFolderId the online id of the folder to retrieve
     * @return
     */
    @Transaction
    @Query("SELECT * FROM Folders WHERE OnlineId = :onlineFolderId")
    public abstract RoomEmbeddedFolder loadFolderByOnlineId(long onlineFolderId);

    /**
     * Inserts the folder into database. If a folder with the same online id exists it will update
     * it to match the provided folder
     * @param folder the folder to insert
     * @return the uid of the inserted folder
     */
    @Insert
    public long insert(RoomModularFolder folder){
        RoomEmbeddedFolder existingFolder = loadFolderByOnlineId(folder.onlineId);
        if(existingFolder != null){
            folder.setUid(existingFolder.folder.getUid());
            _update(folder);
            return existingFolder.folder.getUid();
        }
        else{
            return _insert(folder);
        }
    }

    @Insert
    abstract long _insert(RoomModularFolder folder);
    @Update
    abstract void _update(RoomModularFolder folder);
    @Insert
    public abstract void insertAll(RoomModularFolder... folders);

    @Delete
    public abstract void delete(RoomModularFolder folder);
    @Transaction
    @Query("SELECT * FROM Folders")
    public abstract List<RoomModularFolder> getFolders();

    public void setThumbnail(RoomEmbeddedFolder selectedFolder, RoomEmbeddedFile file) {
        RoomEmbeddedFolder confirmedFolder = loadFolderById(selectedFolder.getId());
        if(confirmedFolder == null)
            return;
        confirmedFolder.thumbnailFile = file;
        confirmedFolder.folder.onlineThumbnailId = file.getOnlineId();
        _update(confirmedFolder.folder);
    }
}
