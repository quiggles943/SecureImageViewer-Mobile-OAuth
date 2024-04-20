package com.quigglesproductions.secureimageviewer.room.databases.unified.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.RoomUnifiedCategory
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.relations.RoomUnifiedEmbeddedCategory
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.relations.RoomUnifiedEmbeddedFile
import com.quigglesproductions.secureimageviewer.room.enums.FileSortType

@Dao
abstract class UnifiedCategoryDao {
    @Transaction
    @Query("SELECT * FROM categories")
    abstract fun getAllCategoriesWithFiles(): List<RoomUnifiedEmbeddedCategory>

    @Query("SELECT * FROM Categories ORDER BY NormalName ASC LIMIT :loadSize OFFSET :offset ")
    abstract suspend fun getCategories(offset: Int, loadSize: Int): List<RoomUnifiedCategory>

    @Transaction
    @Query("SELECT * FROM Categories ORDER BY NormalName ASC LIMIT :loadSize OFFSET :offset ")
    abstract suspend fun getCategoriesWithFiles(offset: Int, loadSize: Int): List<RoomUnifiedEmbeddedCategory>

    suspend fun getFilesWithCategory(categoryId: Long, offset: Int, loadSize: Int,sortType: FileSortType): List<RoomUnifiedEmbeddedFile>{
        return when(sortType){
            FileSortType.NAME_ASC -> getFilesByNameAsc(categoryId,offset,loadSize)
            FileSortType.NAME_DESC -> getFilesByNameDesc(categoryId,offset,loadSize)
            FileSortType.NEWEST_FIRST -> getFilesNewestFirst(categoryId,offset,loadSize)
            FileSortType.OLDEST_FIRST -> getFilesOldestFirst(categoryId,offset,loadSize)
        }
    }
    @Query("SELECT fil.* FROM FileModularCategoryCrossRef ref LEFT JOIN Files fil on fil.FileId = ref.FileId WHERE ref.CategoryId = :categoryId ORDER BY fil.CreatedDate DESC LIMIT :loadSize OFFSET :offset  ")
    abstract suspend fun getFilesNewestFirst(categoryId: Long, offset: Int, loadSize: Int): List<RoomUnifiedEmbeddedFile>
    @Query("SELECT fil.* FROM FileModularCategoryCrossRef ref LEFT JOIN Files fil on fil.FileId = ref.FileId WHERE ref.CategoryId = :categoryId ORDER BY fil.CreatedDate ASC LIMIT :loadSize OFFSET :offset ")
    abstract suspend fun getFilesOldestFirst(categoryId: Long, offset: Int, loadSize: Int): List<RoomUnifiedEmbeddedFile>
    @Query("SELECT fil.* FROM FileModularCategoryCrossRef ref LEFT JOIN Files fil on fil.FileId = ref.FileId WHERE ref.CategoryId = :categoryId ORDER BY fil.NormalName ASC LIMIT :loadSize OFFSET :offset ")
    abstract suspend fun getFilesByNameAsc(categoryId: Long, offset: Int, loadSize: Int): List<RoomUnifiedEmbeddedFile>
    @Query("SELECT fil.* FROM FileModularCategoryCrossRef ref LEFT JOIN Files fil on fil.FileId = ref.FileId WHERE ref.CategoryId = :categoryId ORDER BY fil.NormalName DESC LIMIT :loadSize OFFSET :offset ")
    abstract suspend fun getFilesByNameDesc(categoryId: Long, offset: Int, loadSize: Int): List<RoomUnifiedEmbeddedFile>
}
