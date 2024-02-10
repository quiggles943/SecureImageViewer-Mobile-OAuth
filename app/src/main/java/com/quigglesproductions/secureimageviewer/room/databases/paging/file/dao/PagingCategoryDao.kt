package com.quigglesproductions.secureimageviewer.room.databases.paging.file.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.quigglesproductions.secureimageviewer.room.databases.paging.file.entity.relations.RoomEmbeddedCategory

@Dao
abstract class PagingCategoryDao {
    @get:Query("SELECT * FROM categories")
    abstract val allCategoriesWithFiles: List<RoomEmbeddedCategory>
}
