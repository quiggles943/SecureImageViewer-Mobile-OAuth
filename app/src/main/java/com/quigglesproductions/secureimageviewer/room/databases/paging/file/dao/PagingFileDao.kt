package com.quigglesproductions.secureimageviewer.room.databases.paging.file.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.quigglesproductions.secureimageviewer.room.databases.paging.file.entity.RoomPagingArtist
import com.quigglesproductions.secureimageviewer.room.databases.paging.file.entity.RoomPagingCategory
import com.quigglesproductions.secureimageviewer.room.databases.paging.file.entity.RoomPagingFile
import com.quigglesproductions.secureimageviewer.room.databases.paging.file.entity.RoomPagingFileCategoryCrossRef
import com.quigglesproductions.secureimageviewer.room.databases.paging.file.entity.RoomPagingFileSubjectCrossRef
import com.quigglesproductions.secureimageviewer.room.databases.paging.file.entity.RoomPagingFolder
import com.quigglesproductions.secureimageviewer.room.databases.paging.file.entity.RoomPagingMetadata
import com.quigglesproductions.secureimageviewer.room.databases.paging.file.entity.RoomPagingSubject
import com.quigglesproductions.secureimageviewer.room.databases.paging.file.entity.relations.RoomEmbeddedFile
import com.quigglesproductions.secureimageviewer.room.databases.paging.file.entity.relations.RoomEmbeddedMetadata
import com.quigglesproductions.secureimageviewer.room.exceptions.DatabaseInsertionException
import com.quigglesproductions.secureimageviewer.room.exceptions.NotInDatabaseException
import java.time.LocalDateTime

@Dao
public abstract class PagingFileDao {

    /**
     * Retrieves the files identified by the provided array of file ids
     * @param fileIds an array of file ids
     * @return
     */
    @Transaction
    @Query("SELECT * FROM Files WHERE FileId IN (:fileIds)")
    abstract suspend fun loadAllByIds(fileIds: IntArray): List<RoomEmbeddedFile>

    /**
     * Retrieves all the files which are part of the folder identifed by the provided folder id
     * @param folderId the id of the folder for which all files are to be retrieved
     * @return
     */
    @Transaction
    @Query("SELECT * FROM Files WHERE FolderId IN (:folderId)")
    abstract suspend fun loadAllInFolder(folderId: Int): List<RoomEmbeddedFile>

    /**
     * Retrieves all the files which are part of the folder identifed by the provided online folder
     * id
     * @param onlineFolderId the online id of the folder for which all files are to be retrieved
     * @return
     */
    @Transaction
    @Query("SELECT * FROM Files WHERE OnlineFolderId IN (:onlineFolderId)")
    abstract suspend fun loadAllInOnlineFolder(onlineFolderId: Int): List<RoomEmbeddedFile>

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
    suspend fun insert(folder: RoomPagingFolder, file: RoomEmbeddedFile): Long {
        if (file != null) {
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
                    val categoryCrossRef = RoomPagingFileCategoryCrossRef()
                    categoryCrossRef.categoryId = categoryId
                    categoryCrossRef.fileId = fileId
                    insert(categoryCrossRef)
                }
            }
            if (file.subjects != null) {
                for (subject in file.subjects) {
                    val subjectId = insert(subject)
                    val subjectCrossRef = RoomPagingFileSubjectCrossRef()
                    subjectCrossRef.subjectId = subjectId
                    subjectCrossRef.fileId = fileId
                    insert(subjectCrossRef)
                }
            }
            insert(file.metadata.metadata)
            return fileId
        }
        return 0
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
    suspend fun insertAll(folder: RoomPagingFolder, files: ArrayList<RoomEmbeddedFile>) {
        if (files.size > 0) {
            for (file in files) {
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
                        val categoryCrossRef = RoomPagingFileCategoryCrossRef()
                        categoryCrossRef.categoryId = categoryId
                        categoryCrossRef.fileId = fileId
                        insert(categoryCrossRef)
                    }
                }
                if (file.subjects != null) {
                    for (subject in file.subjects) {
                        val subjectId = insert(subject)
                        val subjectCrossRef = RoomPagingFileSubjectCrossRef()
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
    suspend fun insertAll(folderId: Int, files: ArrayList<RoomEmbeddedFile>) {
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
                        val categoryCrossRef = RoomPagingFileCategoryCrossRef()
                        categoryCrossRef.categoryId = categoryId
                        categoryCrossRef.fileId = fileId
                        insert(categoryCrossRef)
                    }
                }
                if (file.subjects != null) {
                    for (subject in file.subjects) {
                        val subjectId = insert(subject)
                        val subjectCrossRef = RoomPagingFileSubjectCrossRef()
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
    suspend fun insert(artist: RoomPagingArtist): Long {
        val existingArtist = getArtistByOnlineId(artist.onlineId)
        return if (existingArtist != null) {
            artist.uid = existingArtist.getUid()
            _update(artist)
            existingArtist.getUid()
        } else {
            _insert(artist)
        }
    }

    @Insert
    abstract suspend fun _insert(artist: RoomPagingArtist): Long
    @Query("SELECT * FROM Artists WHERE onlineId = :onlineId")
    abstract suspend fun getArtistByOnlineId(onlineId: Long): RoomPagingArtist
    @Update
    abstract suspend fun _update(artist: RoomPagingArtist)
    @Insert
    suspend fun insert(category: RoomPagingCategory): Long {
        val existingCategory = getCategoryByOnlineId(category.onlineId)
        return if (existingCategory != null) {
            category.uid = existingCategory.getUid()
            _update(category)
            existingCategory.getUid()
        } else {
            _insert(category)
        }
    }

    @Insert
    abstract suspend fun _insert(category: RoomPagingCategory): Long
    @Query("SELECT * FROM categories WHERE onlineId = :onlineId")
    abstract suspend fun getCategoryByOnlineId(onlineId: Long): RoomPagingCategory
    @Update
    abstract suspend fun _update(category: RoomPagingCategory)
    @Insert
    suspend fun insert(subject: RoomPagingSubject): Long {
        val existingSubject = getSubjectByOnlineId(subject.onlineId)
        return if (existingSubject != null) {
            subject.uid = existingSubject.getUid()
            _update(subject)
            existingSubject.getUid()
        } else {
            _insert(subject)
        }
    }

    @Insert
    abstract suspend fun _insert(category: RoomPagingSubject): Long
    @Query("SELECT * FROM subjects WHERE onlineId = :onlineId")
    abstract suspend fun getSubjectByOnlineId(onlineId: Long): RoomPagingSubject
    @Update
    abstract suspend fun update(file: RoomPagingFile)
    @Update
    abstract suspend fun update(metadata: RoomPagingMetadata)
    @Update
    abstract suspend fun _update(category: RoomPagingSubject)
    @Insert
    abstract suspend fun insert(subjectCrossRef: RoomPagingFileSubjectCrossRef)
    @Insert
    abstract suspend fun insert(categoryCrossRef: RoomPagingFileCategoryCrossRef)
    @Delete
    abstract suspend fun delete(file: RoomPagingFile)
    @Delete
    suspend fun delete(metadata: RoomEmbeddedMetadata) {
        if (metadata == null) return
        delete(metadata.metadata)
    }

    @Delete
    abstract suspend fun delete(metadata: RoomPagingMetadata)
    @Query("DELETE FROM filemodularcategorycrossref WHERE FileId = :fileId")
    abstract suspend fun deleteCategoryReferences(fileId: Long)
    @Query("DELETE FROM filemodularsubjectcrossref WHERE FileId = :fileId")
    abstract suspend fun deleteSubjectReferences(fileId: Long)
    @Delete
    abstract suspend fun deleteAll(vararg files: RoomPagingFile)
    @Insert
    abstract suspend fun insert(file: RoomPagingFile): Long
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(metadata: RoomPagingMetadata): Long
    @Insert
    abstract suspend fun insertAll(vararg categories: RoomPagingCategory)
    @Insert
    abstract suspend fun insertAll(vararg subjects: RoomPagingSubject)
    suspend fun delete(file: RoomEmbeddedFile) {
        if (file == null) return
        deleteCategoryReferences(file.id)
        deleteSubjectReferences(file.id)
        if (file.metadata != null && file.metadata.metadata != null) delete(file.metadata.metadata)
        delete(file.file)
    }

    suspend fun deleteAll(vararg files: RoomEmbeddedFile) {
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
    abstract operator fun get(fileId: Long): RoomEmbeddedFile
    @Transaction
    @Query("DELETE FROM files WHERE FolderId = :folderId")
    abstract suspend fun deleteAllInFolder(folderId: Int)
    /*suspend fun insertAll(folder: RoomPagingFolder, files: ArrayList<RoomEmbeddedFile>) {
        insertAll(folder, files)
    }*/

    @Transaction
    @Query("SELECT * FROM files WHERE FolderId = :folderId")
    abstract fun folderPagingSource(folderId: Int): PagingSource<Int, RoomEmbeddedFile>
    @Query("SELECT RetrievedDate FROM files WHERE FolderId = :folderId ORDER BY RetrievedDate DESC LIMIT 1 ")
    abstract suspend fun lastUpdated(folderId: Int): LocalDateTime
}
