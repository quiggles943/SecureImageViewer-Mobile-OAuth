package com.quigglesproductions.secureimageviewer.room.databases.unified.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedSubject
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.relations.RoomUnifiedEmbeddedFile
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.relations.RoomUnifiedEmbeddedSubject
import com.quigglesproductions.secureimageviewer.room.enums.FileSortType

@Dao
abstract class UnifiedSubjectDao {

    @Transaction
    @Query("SELECT * FROM subjects")
    abstract fun getAllSubjectsWithFiles(): List<RoomUnifiedEmbeddedSubject>

    @Query("SELECT * FROM subjects ORDER BY NormalName ASC LIMIT :loadSize OFFSET :offset ")
    abstract suspend fun getSubjects(offset: Int, loadSize: Int): List<RoomUnifiedSubject>
    @Transaction
    @Query("SELECT * FROM subjects ORDER BY NormalName ASC LIMIT :loadSize OFFSET :offset ")
    abstract suspend fun getSubjectsWithFiles(offset: Int, loadSize: Int): List<RoomUnifiedEmbeddedSubject>
    suspend fun getFilesWithSubject(subjectId: Long, offset: Int, loadSize: Int, sortType: FileSortType): List<RoomUnifiedEmbeddedFile>{
        return when(sortType){
            FileSortType.NAME_ASC -> getFilesByNameAsc(subjectId,offset,loadSize)
            FileSortType.NAME_DESC -> getFilesByNameDesc(subjectId,offset,loadSize)
            FileSortType.NEWEST_FIRST -> getFilesNewestFirst(subjectId,offset,loadSize)
            FileSortType.OLDEST_FIRST -> getFilesOldestFirst(subjectId,offset,loadSize)
        }
    }
    @Query("SELECT fil.* FROM FileModularSubjectCrossRef ref LEFT JOIN Files fil on fil.FileId = ref.FileId WHERE ref.SubjectId = :subjectId ORDER BY fil.CreatedDate DESC LIMIT :loadSize OFFSET :offset  ")
    abstract suspend fun getFilesNewestFirst(subjectId: Long, offset: Int, loadSize: Int): List<RoomUnifiedEmbeddedFile>
    @Query("SELECT fil.* FROM FileModularSubjectCrossRef ref LEFT JOIN Files fil on fil.FileId = ref.FileId WHERE ref.SubjectId = :subjectId ORDER BY fil.CreatedDate ASC LIMIT :loadSize OFFSET :offset ")
    abstract suspend fun getFilesOldestFirst(subjectId: Long, offset: Int, loadSize: Int): List<RoomUnifiedEmbeddedFile>
    @Query("SELECT fil.* FROM FileModularSubjectCrossRef ref LEFT JOIN Files fil on fil.FileId = ref.FileId WHERE ref.SubjectId = :subjectId ORDER BY fil.NormalName ASC LIMIT :loadSize OFFSET :offset ")
    abstract suspend fun getFilesByNameAsc(subjectId: Long, offset: Int, loadSize: Int): List<RoomUnifiedEmbeddedFile>
    @Query("SELECT fil.* FROM FileModularSubjectCrossRef ref LEFT JOIN Files fil on fil.FileId = ref.FileId WHERE ref.SubjectId = :subjectId ORDER BY fil.NormalName DESC LIMIT :loadSize OFFSET :offset ")
    abstract suspend fun getFilesByNameDesc(subjectId: Long, offset: Int, loadSize: Int): List<RoomUnifiedEmbeddedFile>
}
