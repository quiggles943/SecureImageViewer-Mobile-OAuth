package com.quigglesproductions.secureimageviewer.room.databases.paging.file.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.quigglesproductions.secureimageviewer.room.databases.paging.file.entity.relations.RoomEmbeddedSubject

@Dao
abstract class PagingSubjectDao {
    @get:Query("SELECT * FROM subjects")
    abstract val allSubjectsWithFiles: List<RoomEmbeddedSubject>
}
