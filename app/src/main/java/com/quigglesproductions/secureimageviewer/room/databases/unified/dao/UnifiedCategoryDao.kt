package com.quigglesproductions.secureimageviewer.room.databases.unified.dao

import androidx.room.Dao
import androidx.room.Query
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.relations.RoomUnifiedEmbeddedCategory

@Dao
abstract class UnifiedCategoryDao {
    @get:Query("SELECT * FROM categories")
    abstract val allCategoriesWithFiles: List<RoomUnifiedEmbeddedCategory>
}
