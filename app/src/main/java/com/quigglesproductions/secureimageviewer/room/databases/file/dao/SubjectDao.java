package com.quigglesproductions.secureimageviewer.room.databases.file.dao;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import com.quigglesproductions.secureimageviewer.room.databases.file.relations.SubjectWithFiles;

import java.util.List;

@Dao
public abstract class SubjectDao {

    @Transaction
    @Query("SELECT * FROM Subjects")
    public abstract List<SubjectWithFiles> getAllSubjectsWithFiles();
}
