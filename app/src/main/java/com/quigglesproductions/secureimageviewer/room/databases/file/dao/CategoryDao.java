package com.quigglesproductions.secureimageviewer.room.databases.file.dao;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import com.quigglesproductions.secureimageviewer.room.databases.file.relations.CategoryWithFiles;
import com.quigglesproductions.secureimageviewer.room.databases.file.relations.SubjectWithFiles;

import java.util.List;

@Dao
public abstract class CategoryDao {

    @Transaction
    @Query("SELECT * FROM Categories")
    public abstract List<CategoryWithFiles> getAllCategoriesWithFiles();
}
