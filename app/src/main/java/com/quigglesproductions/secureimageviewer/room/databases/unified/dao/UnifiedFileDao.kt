package com.quigglesproductions.secureimageviewer.room.databases.unified.dao

import androidx.annotation.NonNull
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteQuery
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.*
import com.quigglesproductions.secureimageviewer.room.databases.unified.entity.relations.*
import com.quigglesproductions.secureimageviewer.room.enums.FileSortType
import com.quigglesproductions.secureimageviewer.room.exceptions.DatabaseInsertionException
import com.quigglesproductions.secureimageviewer.room.exceptions.NotInDatabaseException
import java.time.LocalDateTime

@Dao
public abstract class UnifiedFileDao {

    @get:Query("SELECT * FROM Files")
    abstract val files: List<RoomUnifiedFile>

    /**
     * Retrieves the files identified by the provided array of file ids
     * @param fileIds an array of file ids
     * @return
     */
    @Transaction
    @Query("SELECT * FROM Files WHERE FileId IN (:fileIds)")
    abstract suspend fun loadAllByIds(fileIds: IntArray): List<RoomUnifiedEmbeddedFile>

    @Transaction
    @Query("SELECT * FROM Files WHERE isFavourite = 1")
    abstract suspend fun loadAllFavouritedFiles(): List<RoomUnifiedEmbeddedFile>

    /**
     * Retrieves all the files which are part of the folder identifed by the provided folder id
     * @param folderId the id of the folder for which all files are to be retrieved
     * @return
     */
    @Transaction
    @Query("SELECT * FROM Files WHERE FolderId IN (:folderId)")
    abstract suspend fun loadAllInFolder(folderId: Int): List<RoomUnifiedEmbeddedFile>

    /**
     * Retrieves all the files which are part of the folder identifed by the provided online folder
     * id
     * @param onlineFolderId the online id of the folder for which all files are to be retrieved
     * @return
     */
    @Transaction
    @Query("SELECT * FROM Files WHERE OnlineFolderId IN (:onlineFolderId)")
    abstract suspend fun loadAllInOnlineFolder(onlineFolderId: Int): List<RoomUnifiedEmbeddedFile>

    /**
     * Inserts the provided file into the database and links it to the provided folder. If any
     * subjects or categories are listed, or if the artist is specified, they will be either added
     * or updated to the database in their respective tables and linked to this file through a many
     * to many relationship. If the file already exists on the device then it will be updated to match
     * the file provided
     * @param folder The folder to link the file to
     * @param file The file to insert to the database
     * @return the uid of the inserted file
     * @throws DatabaseInsertionException
     */
    @Transaction
    @Insert
    @Throws(DatabaseInsertionException::class)
    suspend fun insert(folder: RoomUnifiedFolder, @NonNull file: RoomUnifiedEmbeddedFile): Long {
        if (folder.uid == 0L) throw DatabaseInsertionException(NotInDatabaseException())
        file.file.folderId = folder.uid
        val fileId = insert(file.file)
        file.metadata.metadata.uid = fileId
        if (file.metadata.artist != null) {
            val artistId = insert(file.metadata.artist)
            file.metadata.metadata.artistId = artistId
            file.metadata.metadata.onlineArtistId = file.metadata.artist.onlineId
        }
        if (file.categories != null) {
            for (category in file.categories) {
                val categoryId = insert(category)
                val categoryCrossRef = RoomUnifiedFileCategoryCrossRef()
                categoryCrossRef.categoryId = categoryId
                categoryCrossRef.fileId = fileId
                insert(categoryCrossRef)
            }
        }
        if (file.subjects != null) {
            for (subject in file.subjects) {
                val subjectId = insert(subject)
                val subjectCrossRef = RoomUnifiedFileSubjectCrossRef()
                subjectCrossRef.subjectId = subjectId
                subjectCrossRef.fileId = fileId
                insert(subjectCrossRef)
            }
        }
        insert(file.metadata.metadata)
        return fileId
    }

    /**
     * Inserts the provided files into the database and links it to the provided folder. If any
     * subjects or categories are listed, or if the artist is specified, they will be either added
     * or updated to the database in their respective tables and linked to this file through a many
     * to many relationship. If the file already exists on the device then it will be updated to match
     * the file provided
     * @param folder The folder to link the file to
     * @param files The files to insert to the database
     * @throws DatabaseInsertionException
     */
    @Transaction
    @Insert
    @Throws(DatabaseInsertionException::class)
    suspend fun insertAll(folder: RoomUnifiedFolder, files: ArrayList<RoomUnifiedEmbeddedFile>) {
        if (files.size > 0) {
            for (file in files) {
                if (folder.uid == 0L) throw DatabaseInsertionException(NotInDatabaseException())
                file.file.folderId = folder.uid!!
                val fileId = insert(file.file)
                file.metadata.metadata.uid = fileId
                if (file.metadata.artist != null) {
                    val artistId = insert(file.metadata.artist)
                    file.metadata.metadata.artistId = artistId
                    file.metadata.metadata.onlineArtistId = file.metadata.artist.onlineId
                }
                if (file.categories != null) {
                    for (category in file.categories) {
                        val categoryId = insert(category)
                        val categoryCrossRef = RoomUnifiedFileCategoryCrossRef()
                        categoryCrossRef.categoryId = categoryId
                        categoryCrossRef.fileId = fileId
                        insert(categoryCrossRef)
                    }
                }
                if (file.subjects != null) {
                    for (subject in file.subjects) {
                        val subjectId = insert(subject)
                        val subjectCrossRef = RoomUnifiedFileSubjectCrossRef()
                        subjectCrossRef.subjectId = subjectId
                        subjectCrossRef.fileId = fileId
                        insert(subjectCrossRef)
                    }
                }
                insert(file.metadata.metadata)
            }
        }
    }
    @Transaction
    @Insert
    @Throws(DatabaseInsertionException::class)
    suspend fun insertAll(folderId: Int, files: ArrayList<RoomUnifiedEmbeddedFile>) {
        if (files.size > 0) {
            for (file in files) {
                file.file.folderId = folderId.toLong()
                val fileId = insert(file.file)
                file.metadata.metadata.uid = fileId
                if (file.metadata.artist != null) {
                    val artistId = insert(file.metadata.artist)
                    file.metadata.metadata.artistId = artistId
                    file.metadata.metadata.onlineArtistId = file.metadata.artist.onlineId
                }
                if (file.categories != null) {
                    for (category in file.categories) {
                        val categoryId = insert(category)
                        val categoryCrossRef = RoomUnifiedFileCategoryCrossRef()
                        categoryCrossRef.categoryId = categoryId
                        categoryCrossRef.fileId = fileId
                        insert(categoryCrossRef)
                    }
                }
                if (file.subjects != null) {
                    for (subject in file.subjects) {
                        val subjectId = insert(subject)
                        val subjectCrossRef = RoomUnifiedFileSubjectCrossRef()
                        subjectCrossRef.subjectId = subjectId
                        subjectCrossRef.fileId = fileId
                        insert(subjectCrossRef)
                    }
                }
                insert(file.metadata.metadata)
            }
        }
    }

    /**
     * Inserts the provided artist into the database. If the artist already exists then it updates
     * the existing artist with the details of the provided one
     * @param artist the artist to insert to the database
     * @return the uid of the artist
     */
    @Insert
    suspend fun insert(artist: RoomUnifiedArtist): Long {
        val existingArtist = getArtistByOnlineId(artist.onlineId)
        return if (existingArtist != null) {
            artist.uid = existingArtist.getUid()
            _update(artist)
            existingArtist.getUid()
        } else {
            _insert(artist)
        }
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun _insert(artist: RoomUnifiedArtist): Long
    @Query("SELECT * FROM Artists WHERE onlineId = :onlineId")
    abstract suspend fun getArtistByOnlineId(onlineId: Long): RoomUnifiedArtist
    @Update
    abstract suspend fun _update(artist: RoomUnifiedArtist)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: RoomUnifiedCategory): Long {
        val existingCategory = getCategoryByOnlineId(category.onlineCategoryId)
        return if (existingCategory != null) {
            category.categoryId = existingCategory.getUid()
            _update(category)
            existingCategory.getUid()
        } else {
            _insert(category)
        }
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun _insert(category: RoomUnifiedCategory): Long
    @Query("SELECT * FROM Categories WHERE OnlineId = :onlineId")
    abstract suspend fun getCategoryByOnlineId(onlineId: Long): RoomUnifiedCategory
    @Update
    abstract suspend fun _update(category: RoomUnifiedCategory)
    @Insert
    suspend fun insert(subject: RoomUnifiedSubject): Long {
        val existingSubject = getSubjectByOnlineId(subject.subjectOnlineId)
        return if (existingSubject != null) {
            subject.subjectId = existingSubject.subjectId
            _update(subject)
            existingSubject.subjectId
        } else {
            _insert(subject)
        }
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun _insert(category: RoomUnifiedSubject): Long
    @Query("SELECT * FROM subjects WHERE OnlineId = :onlineId")
    abstract suspend fun getSubjectByOnlineId(onlineId: Long): RoomUnifiedSubject
    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun update(file: RoomUnifiedFile)

    suspend fun update(file: RoomUnifiedEmbeddedFile){
        if (file.file.uid == 0L) throw DatabaseInsertionException(NotInDatabaseException())
        val fileId = file.file.uid!!
        update(file.file)
        if (file.metadata.artist != null) {
            val artistId = insert(file.metadata.artist)
            file.metadata.metadata.artistId = artistId
            file.metadata.metadata.onlineArtistId = file.metadata.artist.onlineId
        }
        if (file.categories != null) {
            for (category in file.categories) {
                val categoryId = insert(category)
                val categoryCrossRef = RoomUnifiedFileCategoryCrossRef()
                categoryCrossRef.categoryId = categoryId
                categoryCrossRef.fileId = fileId
                insert(categoryCrossRef)
            }
        }
        if (file.subjects != null) {
            for (subject in file.subjects) {
                val subjectId = insert(subject)
                val subjectCrossRef = RoomUnifiedFileSubjectCrossRef()
                subjectCrossRef.subjectId = subjectId
                subjectCrossRef.fileId = fileId
                insert(subjectCrossRef)
            }
        }
        insert(file.metadata.metadata)
    }
    @Update
    abstract suspend fun update(metadata: RoomUnifiedMetadata)
    @Update
    abstract suspend fun _update(category: RoomUnifiedSubject)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insert(subjectCrossRef: RoomUnifiedFileSubjectCrossRef)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insert(categoryCrossRef: RoomUnifiedFileCategoryCrossRef)
    @Delete
    abstract suspend fun delete(file: RoomUnifiedFile)
    @Delete
    suspend fun delete(metadata: RoomUnifiedEmbeddedMetadata) {
        if (metadata == null) return
        delete(metadata.metadata)
    }

    @Delete
    abstract suspend fun delete(metadata: RoomUnifiedMetadata)
    @Query("DELETE FROM filemodularcategorycrossref WHERE FileId = :fileId")
    abstract suspend fun deleteCategoryReferences(fileId: Long)
    @Query("DELETE FROM FileModularSubjectCrossRef WHERE FileId = :fileId")
    abstract suspend fun deleteSubjectReferences(fileId: Long)
    @Delete
    abstract suspend fun deleteAll(vararg files: RoomUnifiedFile)
    @Insert
    abstract suspend fun insert(file: RoomUnifiedFile): Long
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(metadata: RoomUnifiedMetadata): Long
    @Insert
    abstract suspend fun insertAll(vararg categories: RoomUnifiedCategory)
    @Insert
    abstract suspend fun insertAll(vararg subjects: RoomUnifiedSubject)
    suspend fun delete(file: RoomUnifiedEmbeddedFile) {
        if (file == null) return
        deleteCategoryReferences(file.id)
        deleteSubjectReferences(file.id)
        if (file.metadata != null && file.metadata.metadata != null) delete(file.metadata.metadata)
        delete(file.file)
    }

    suspend fun deleteAll(vararg files: RoomUnifiedEmbeddedFile) {
        if (files == null) return
        for (file in files) {
            deleteCategoryReferences(file.id)
            deleteSubjectReferences(file.id)
            delete(file.metadata.metadata)
            delete(file.file)
        }
    }

    @Transaction
    @Query("SELECT * FROM files WHERE FileId = :fileId")
    abstract operator fun get(fileId: Long): RoomUnifiedEmbeddedFile

    @Transaction
    @Query("DELETE FROM files WHERE FolderId = :folderId")
    abstract suspend fun deleteAllInFolder(folderId: Int)

    @Transaction
    @Query("DELETE FROM files WHERE FolderId = :folderId AND IsDownloaded = 0")
    abstract suspend fun deleteAllCachedInFolder(folderId: Int)
    /*suspend fun insertAll(folder: RoomUnifiedFolder, files: ArrayList<RoomUnifiedEmbeddedFile>) {
        insertAll(folder, files)
    }*/

    @Transaction
    @Query("SELECT * FROM files WHERE FolderId = :folderId")
    abstract fun folderPagingSource(folderId: Int): PagingSource<Int, RoomUnifiedEmbeddedFile>
    @Transaction
    @Query("SELECT * FROM files WHERE FolderId = :folderId ORDER BY :sortColumn")
    abstract fun folderPagingSourceSorted(folderId: Int,sortColumn: String): PagingSource<Int, RoomUnifiedEmbeddedFile>
    fun getFilesPaging(folderId: Long,sortType: FileSortType): PagingSource<Int, RoomUnifiedEmbeddedFile>{
        return when(sortType){
            FileSortType.NAME_ASC -> getPagingFilesByNameAsc(folderId)
            FileSortType.NAME_DESC -> getPagingFilesByNameDesc(folderId)
            FileSortType.NEWEST_FIRST -> getPagingFilesNewestFirst(folderId)
            FileSortType.OLDEST_FIRST -> getPagingFilesOldestFirst(folderId)
        }
    }
    @Transaction
    @Query("SELECT * FROM files WHERE FolderId = :folderId ORDER BY CreatedDate DESC ")
    abstract fun getPagingFilesNewestFirst(folderId: Long): PagingSource<Int, RoomUnifiedEmbeddedFile>
    @Transaction
    @Query("SELECT * FROM Files WHERE FolderId = :folderId ORDER BY CreatedDate ASC")
    abstract fun getPagingFilesOldestFirst(folderId: Long): PagingSource<Int, RoomUnifiedEmbeddedFile>
    @Transaction
    @Query("SELECT * FROM Files WHERE FolderId = :folderId ORDER BY NormalName ASC")
    abstract fun getPagingFilesByNameAsc(folderId: Long): PagingSource<Int, RoomUnifiedEmbeddedFile>
    @Transaction
    @Query("SELECT * FROM Files WHERE FolderId = :folderId ORDER BY NormalName DESC")
    abstract fun getPagingFilesByNameDesc(folderId: Long): PagingSource<Int, RoomUnifiedEmbeddedFile>
    @Query("SELECT RetrievedDate FROM files WHERE FolderId = :folderId ORDER BY RetrievedDate DESC LIMIT 1 ")
    abstract suspend fun lastUpdated(folderId: Int): LocalDateTime
    @Query("SELECT EXISTS(SELECT * FROM files WHERE OnlineId = :onlineId)")
    abstract suspend fun exists(onlineId: Int): Boolean

    @Query("SELECT COUNT(*) FROM Files")
    abstract fun getFileCount(): Int
    /*@Query("SELECT * FROM Files WHERE FolderId = :folderId ORDER BY :sortType LIMIT :loadSize OFFSET :offset ")
    abstract suspend fun getFiles(folderId: Long, offset: Int, loadSize: Int,sortType:String): List<RoomUnifiedEmbeddedFile>*/

    suspend fun getFiles(folderId: Long, offset: Int, loadSize: Int,sortType:FileSortType): List<RoomUnifiedEmbeddedFile>{
        return when(sortType){
            FileSortType.NAME_ASC -> getFilesByNameAsc(folderId,offset,loadSize)
            FileSortType.NAME_DESC -> getFilesByNameDesc(folderId,offset,loadSize)
            FileSortType.NEWEST_FIRST -> getFilesNewestFirst(folderId,offset,loadSize)
            FileSortType.OLDEST_FIRST -> getFilesOldestFirst(folderId,offset,loadSize)
        }
    }
    @Query("SELECT * FROM Files WHERE FolderId = :folderId ORDER BY CreatedDate DESC LIMIT :loadSize OFFSET :offset ")
    abstract suspend fun getFilesNewestFirst(folderId: Long, offset: Int, loadSize: Int): List<RoomUnifiedEmbeddedFile>
    @Query("SELECT * FROM Files WHERE FolderId = :folderId ORDER BY CreatedDate ASC LIMIT :loadSize OFFSET :offset ")
    abstract suspend fun getFilesOldestFirst(folderId: Long, offset: Int, loadSize: Int): List<RoomUnifiedEmbeddedFile>
    @Query("SELECT * FROM Files WHERE FolderId = :folderId ORDER BY NormalName ASC LIMIT :loadSize OFFSET :offset ")
    abstract suspend fun getFilesByNameAsc(folderId: Long, offset: Int, loadSize: Int): List<RoomUnifiedEmbeddedFile>
    @Query("SELECT * FROM Files WHERE FolderId = :folderId ORDER BY NormalName DESC LIMIT :loadSize OFFSET :offset ")
    abstract suspend fun getFilesByNameDesc(folderId: Long, offset: Int, loadSize: Int): List<RoomUnifiedEmbeddedFile>

    suspend fun getFavouriteFiles(offset: Int, loadSize: Int,sortType:FileSortType): List<RoomUnifiedEmbeddedFile>{
        return when(sortType){
            FileSortType.NAME_ASC -> getFavouriteFilesByNameAsc(offset,loadSize)
            FileSortType.NAME_DESC -> getFavouriteFilesByNameDesc(offset,loadSize)
            FileSortType.NEWEST_FIRST -> getFavouriteFilesNewestFirst(offset,loadSize)
            FileSortType.OLDEST_FIRST -> getFavouriteFilesOldestFirst(offset,loadSize)
        }
    }

    @Query("SELECT * FROM Files WHERE isFavourite = 1 ORDER BY CachedFolderName ASC LIMIT :loadSize OFFSET :offset ")
    abstract suspend fun getFavouriteFiles(offset: Int, loadSize: Int): List<RoomUnifiedEmbeddedFile>

    @Query("SELECT * FROM Files WHERE isFavourite = 1 ORDER BY CachedFolderName ASC, CreatedDate DESC LIMIT :loadSize OFFSET :offset ")
    abstract suspend fun getFavouriteFilesNewestFirst(offset: Int, loadSize: Int): List<RoomUnifiedEmbeddedFile>
    @Query("SELECT * FROM Files WHERE isFavourite = 1 ORDER BY CachedFolderName ASC, CreatedDate ASC LIMIT :loadSize OFFSET :offset ")
    abstract suspend fun getFavouriteFilesOldestFirst(offset: Int, loadSize: Int): List<RoomUnifiedEmbeddedFile>
    @Query("SELECT * FROM Files WHERE isFavourite = 1 ORDER BY CachedFolderName ASC, NormalName ASC LIMIT :loadSize OFFSET :offset ")
    abstract suspend fun getFavouriteFilesByNameAsc(offset: Int, loadSize: Int): List<RoomUnifiedEmbeddedFile>
    @Query("SELECT * FROM Files WHERE isFavourite = 1 ORDER BY CachedFolderName ASC, NormalName DESC LIMIT :loadSize OFFSET :offset ")
    abstract suspend fun getFavouriteFilesByNameDesc(offset: Int, loadSize: Int): List<RoomUnifiedEmbeddedFile>
    @RawQuery
    abstract suspend fun getFilesViaQuery(query: SupportSQLiteQuery): List<RoomUnifiedEmbeddedFile>
    @Transaction
    @Query("SELECT * FROM Files WHERE OnlineId = :onlineId LIMIT 1")
    abstract suspend fun getByOnlineId(onlineId: Int): RoomUnifiedEmbeddedFile?
    @Query("SELECT fil.* FROM FileModularCategoryCrossRef ref LEFT JOIN Files fil on fil.FileId = ref.FileId WHERE ref.CategoryId = :uid LIMIT 1")
    abstract suspend fun getThumbnailForCategory(uid: Long):RoomUnifiedFile?

    @Query("SELECT fil.* FROM FileModularSubjectCrossRef ref LEFT JOIN Files fil on fil.FileId = ref.FileId WHERE ref.SubjectId = :uid LIMIT 1")
    abstract suspend fun getThumbnailForSubject(uid: Long):RoomUnifiedFile?

    @Query("SELECT * FROM Files WHERE OnlineId = :fileId LIMIT 1")
    abstract suspend fun loadFileByOnlineId(fileId: Long): RoomUnifiedEmbeddedFile

}
