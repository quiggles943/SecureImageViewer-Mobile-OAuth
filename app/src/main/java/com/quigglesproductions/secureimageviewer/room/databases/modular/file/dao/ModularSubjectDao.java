package com.quigglesproductions.secureimageviewer.room.databases.modular.file.dao;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import com.quigglesproductions.secureimageviewer.room.databases.modular.file.entity.relations.RoomEmbeddedSubject;

import java.util.List;

@Dao
public abstract class ModularSubjectDao {

    @Transaction
    @Query("SELECT * FROM Subjects")
    public abstract List<RoomEmbeddedSubject> getAllSubjectsWithFiles();
}
