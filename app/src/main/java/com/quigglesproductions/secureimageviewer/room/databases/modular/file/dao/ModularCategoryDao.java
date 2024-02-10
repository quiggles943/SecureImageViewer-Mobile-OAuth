package com.quigglesproductions.secureimageviewer.room.databases.modular.file.dao;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import com.quigglesproductions.secureimageviewer.room.databases.modular.file.entity.relations.RoomEmbeddedCategory;

import java.util.List;

@Dao
public abstract class ModularCategoryDao {

    @Transaction
    @Query("SELECT * FROM Categories")
    public abstract List<RoomEmbeddedCategory> getAllCategoriesWithFiles();
}
