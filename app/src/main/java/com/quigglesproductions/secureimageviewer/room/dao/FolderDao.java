package com.quigglesproductions.secureimageviewer.room.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.quigglesproductions.secureimageviewer.room.entity.RoomDatabaseFolder;
import com.quigglesproductions.secureimageviewer.room.relations.FolderWithFiles;

import java.util.List;

@Dao
public interface FolderDao {

    @Query("SELECT * FROM Folders")
    List<FolderWithFiles> getAll();

    @Query("SELECT * FROM Folders WHERE FolderId IN (:folderIds)")
    List<FolderWithFiles> loadAllByIds(int[] folderIds);

    @Insert
    long insert(RoomDatabaseFolder folder);
    @Insert
    void insertAll(RoomDatabaseFolder... folders);

    @Delete
    void delete(RoomDatabaseFolder folder);
}
