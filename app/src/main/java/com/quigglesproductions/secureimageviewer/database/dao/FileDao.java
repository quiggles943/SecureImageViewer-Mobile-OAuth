package com.quigglesproductions.secureimageviewer.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Index;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.quigglesproductions.secureimageviewer.database.models.DatabaseFile;

import java.util.List;

@Dao
public interface FileDao {
    @Query("Select * from Files")
    List<DatabaseFile> getFiles();

    @Query("SELECT * FROM Files WHERE id IN (:fileIds)")
    List<DatabaseFile> loadAllByIds(int[] fileIds);
    @Query("SELECT * FROM Files WHERE id = :fileId LIMIT 1")
    DatabaseFile findById(int fileId);
    @Query("SELECT * FROM Files WHERE onlineId = :onlineFileId LIMIT 1")
    DatabaseFile findByOnlineId(int onlineFileId);
    @Insert
    void insertFile(DatabaseFile databaseFile);
    @Update
    void updateFile(DatabaseFile databaseFile);
    @Delete
    void deleteFile(DatabaseFile databaseFile);
}
