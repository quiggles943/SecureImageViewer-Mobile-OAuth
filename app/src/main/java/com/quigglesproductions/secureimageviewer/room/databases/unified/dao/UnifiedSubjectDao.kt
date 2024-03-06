package com.quigglesproductions.secureimageviewer.room.databases.unified.dao

import androidx.room.Dao
import androidx.room.Query
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.relations.RoomUnifiedEmbeddedSubject

@Dao
abstract class UnifiedSubjectDao {
    @get:Query("SELECT * FROM subjects")
    abstract val allSubjectsWithFiles: List<RoomUnifiedEmbeddedSubject>
}
